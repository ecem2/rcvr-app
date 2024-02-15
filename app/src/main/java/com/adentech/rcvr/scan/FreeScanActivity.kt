package com.adentech.rcvr.scan


import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.adentech.rcvr.BuildConfig
import com.adentech.rcvr.R
import com.adentech.rcvr.billing.RecoveryApplication
import com.adentech.rcvr.common.ArgumentKey
import com.adentech.rcvr.common.Constants
import com.adentech.rcvr.common.Constants.FROM_SUBSCRIPTION
import com.adentech.rcvr.common.Constants.IS_FREE_SCAN
import com.adentech.rcvr.activities.BaseActivity
import com.adentech.rcvr.common.Resource
import com.adentech.rcvr.common.Status
import com.adentech.rcvr.model.FileModel
import com.adentech.rcvr.databinding.ActivityFreeScanBinding
import com.adentech.rcvr.databinding.DialogPermissionBinding
import com.adentech.rcvr.extensions.observe
import com.adentech.rcvr.extensions.withDelay
import com.adentech.rcvr.home.MainViewModel
import com.adentech.rcvr.home.ImagesActivity
import com.adentech.rcvr.fullscreen.DeletedImageActivity
import com.adentech.rcvr.subscription.SubscriptionActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

@AndroidEntryPoint
class FreeScanActivity : BaseActivity<MainViewModel, ActivityFreeScanBinding>() {

    private val freeScanAdapter by lazy {
        FreeScanAdapter(
            hasReward = false,
            isSubsClosed = false,
            context = this@FreeScanActivity,
            onItemClicked = ::onItemClicked
        )
    }
    private var rewardedCount = 0
    private var mInterstitialAd: InterstitialAd? = null
    private var mRewardedAd: RewardedAd? = null
    private val currentList: ArrayList<FileModel> = ArrayList()
    private var rewardedList: List<Int>? = null
    private var mUserEarnedReward = false
    private var elapsedTime: Long = 0
    private var twentyFourHoursInMillis: Int = 0

    override fun viewDataBindingClass() = ActivityFreeScanBinding::class.java
    override fun viewModelClass() = MainViewModel::class.java

    @SuppressLint("NotifyDataSetChanged")
    override fun onInitDataBinding() {
        handleBackPressed()
        setupRecyclerView()
        requestForStoragePermissions()
        observe(isStoragePermissionGranted, ::getStoragePermission)

        rewardedCount = viewModel.preferences.getRewardCount()
        if (rewardedCount > 0) {
            freeScanAdapter.hasReward = true
            freeScanAdapter.notifyDataSetChanged()
        }

        MobileAds.initialize(this@FreeScanActivity) {
            if (rewardedCount > 0) {
                loadRewardedAd()
            } else {
                loadInterAd()
            }
        }
    }

    private fun getStoragePermission(isGranted: Boolean) {
        if (isGranted) {
            setupImages()
        } else {
            if (isStoragePermissionGranted.value == false) {
                showPermissionDialog()
            }
        }
    }

    private fun changeColor(
        string: String,
        firstText: Int,
        secondText: Int
    ): SpannableStringBuilder {
        val words = string.split(" ")
        val firstWord = words.getOrElse(0) { "" }
        val secondWord = words.getOrElse(1) { "" }

        val builder = SpannableStringBuilder(string)
        builder.setSpan(ForegroundColorSpan(firstText), 0, firstWord.length, 0)
        builder.setSpan(
            ForegroundColorSpan(secondText),
            firstWord.length + 1,
            firstWord.length + secondWord.length + 1,
            0
        )

        return builder
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showDialog() {
        viewBinding.cardScanProgress.visibility = View.GONE
        viewBinding.clResultDialog.visibility = View.VISIBLE
        freeScanAdapter.rewardedList = rewardedList
        freeScanAdapter.notifyDataSetChanged()
        val dialogText = getString(R.string.deleted_photos_were_found)
        val color = ContextCompat.getColor(this@FreeScanActivity, R.color.dialog_red_text)
        val coloredText = changeColor(dialogText, color, color)
        viewBinding.dialogTitle.text = coloredText
        viewBinding.buttonRestoreNow.setOnClickListener {
            viewModel.isProgressDone = true
            navigateToSubscription()
            freeScanAdapter.isSubsClosed = true
            freeScanAdapter.notifyDataSetChanged()
        }

        viewBinding.apply {
            if (rewardedCount > 0) {
                buttonWatchAds.background =
                    ContextCompat.getDrawable(this@FreeScanActivity, R.drawable.bg_ads_button)
                buttonWatchAds.setOnClickListener {
                    viewModel.isProgressDone = true
                    viewBinding.clResultDialog.visibility = View.GONE
                    freeScanAdapter.isSubsClosed = true
                    freeScanAdapter.notifyDataSetChanged()
                }
            } else {
                recoverButtonTitleOne.text = getString(R.string.watch_ads_exhausted)
                recoverButtonTitleOne.setTextColor(
                    ContextCompat.getColor(
                        this@FreeScanActivity,
                        R.color.white
                    )
                )
                premiumIconOne.visibility = View.GONE
            }
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupRecyclerView() {
        freeScanAdapter.submitList(ArrayList())
        viewBinding.rvDeletedImages.apply {
            adapter = freeScanAdapter
            itemAnimator = null
            layoutManager = GridLayoutManager(this@FreeScanActivity, 3)
            setHasFixedSize(true)
            setOnTouchListener { _, _ -> !viewModel.isProgressDone }
        }
    }

    private fun getImagesList(resource: Resource<ArrayList<FileModel>>) {
        when (resource.status) {
            Status.SUCCESS -> {
                resource.data?.let { adjustImages(it) }
            }

            Status.ERROR -> {
                viewBinding.cardScanProgress.visibility = View.GONE
                if (resource.data.isNullOrEmpty()) {
                    viewBinding.llEmptyFolder.visibility = View.VISIBLE
                }
            }

            Status.LOADING -> {
                viewBinding.cardScanProgress.visibility = View.VISIBLE
            }
        }
    }

    private fun adjustImages(imageList: ArrayList<FileModel>) {
        val min = imageList.size / 5
        val max = imageList.size / 2
        val count = if (imageList.size / 50 > 3) {
            if (BuildConfig.DEBUG) 10 else 160
        } else {
            imageList.size / 10 + 1
        }

        val indexesArray: ArrayList<Int> = ArrayList()
        lifecycleScope.launch {
            if (imageList.isNotEmpty()) {
                viewBinding.llEmptyFolder.visibility = View.GONE
                viewBinding.tvProgressBar.text = getString(R.string.photos_recovered)

                while (indexesArray.size < count) {
                    val randomNumber = Random.nextInt(min, max + 1)
                    if (randomNumber !in indexesArray) {
                        indexesArray.add(randomNumber)
                    }
                }
                for (img in 1..indexesArray.size) {
                    val randomTime = Random.nextLong(120, 230)
                    delay(randomTime)
                    withContext(Dispatchers.Main) {
                        val chosenImage: FileModel = imageList[indexesArray[img - 1]]
                        currentList.add(0, chosenImage)
                        val newList: ArrayList<FileModel> = ArrayList()
                        newList.addAll(currentList)
                        freeScanAdapter.submitList(newList)
                        rewardedList = getThreeRandomPositions(newList)
                        viewBinding.rvDeletedImages.scrollToPosition(0)
                    }
                }
                withDelay(2000, ::showDialog)
            } else {
                withContext(Dispatchers.Main) {
                    viewBinding.cardScanProgress.visibility = View.GONE
                    viewBinding.llEmptyFolder.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun navigateToSubscription() {
        val intent = Intent(this@FreeScanActivity, SubscriptionActivity::class.java)
            .putExtra(IS_FREE_SCAN, true)
        startForResult.launch(intent)
    }

    private fun onItemClicked(fileModel: FileModel, i: Int) {
        if (viewModel.isProgressDone) {
            if (fileModel.isSelected == true) {
                navigateToImageActivity(fileModel)
            } else {
                if (rewardedCount > 0) {
                    if (fileModel.isRewarded == true) {
                        fileModel.isSelected = true
                        showRewardedAds(fileModel, i)
                    } else {
                        fileModel.isSelected = false
                        navigateToSubscription()
                    }
                } else {
                    navigateToSubscription()
                }
            }
        }
    }

    private fun getThreeRandomPositions(list: List<Any>): List<Int> {
        val random = Random
        val positions = mutableListOf<Int>()
        val count = viewModel.preferences.getRewardCount()
        val adCount = list.size / 3
        while (positions.size < adCount && list.size >= adCount && count > 0) {
            val randomPosition = random.nextInt(list.size)
            if (!positions.contains(randomPosition)) {
                positions.add(randomPosition)
            }
        }

        return positions
    }

    private fun navigateToImageActivity(image: FileModel) {
        val intent = Intent(this@FreeScanActivity, DeletedImageActivity::class.java)
        intent.putExtra(Constants.IMAGE_PATH, image)
        intent.putExtra(Constants.IS_DELETED, true)
        startActivity(intent)
    }

    private fun showPermissionDialog() {
        viewBinding.cardScanProgress.visibility = View.GONE
        val dialogBuilder = Dialog(this@FreeScanActivity, R.style.CustomDialog)
        val dialogBinding = DialogPermissionBinding.inflate(layoutInflater)
        dialogBuilder.setContentView(dialogBinding.root)
        dialogBuilder.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialogBinding.apply {
            buttonSettings.setOnClickListener {
                requestForStoragePermissions()
                dialogBuilder.cancel()
            }

            cancelButton.setOnClickListener {
                showPermissionError()
                dialogBuilder.cancel()
            }
        }
        dialogBuilder.show()
    }

    private fun showPermissionError() {
        viewBinding.llPermissionError.visibility = View.VISIBLE
        viewBinding.buttonSettings.setOnClickListener {
            requestForStoragePermissions()
        }
    }

    private fun setupImages() {
        viewBinding.llPermissionError.visibility = View.GONE
        viewModel.getAllGalleryImages()
        observe(viewModel.imageList, ::getImagesList)
    }

//    private fun showInterAds() {
//        if (mInterstitialAd != null) {
//            mInterstitialAd?.show(this)
//        } else {
//            return
//        }
//    }

    private fun loadInterAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this,
            BuildConfig.INTERSTITIAL_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                }
            })
    }

    private fun showRewardedAds(fileModel: FileModel, position: Int) {
        if (mRewardedAd != null) {
            mRewardedAd?.show(this@FreeScanActivity) { p0 ->
                mUserEarnedReward = p0 != null
            }

            mRewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                @SuppressLint("NotifyDataSetChanged")
                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    mRewardedAd = null
                    loadRewardedAd()
                    if (mUserEarnedReward) {
                        rewardedCount--
                        viewModel.preferences.setRewardCount(rewardedCount)
                        Handler(Looper.getMainLooper()).postDelayed({
                            fileModel.isSelected = true
                            freeScanAdapter.notifyItemChanged(position)
                        }, 1000)
                        mUserEarnedReward = false

                        if (rewardedCount <= 0) {
                            freeScanAdapter.hasReward = false
                            freeScanAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
        } else {
            Toast.makeText(
                this@FreeScanActivity,
                "The rewarded ad wasn't ready yet.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun loadRewardedAd() {
        val adRequest: AdRequest = AdRequest.Builder().build()
        if (mRewardedAd == null) {
            RewardedAd.load(
                this@FreeScanActivity,
                BuildConfig.REWARDED_ID,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        super.onAdFailedToLoad(adError)
                        mRewardedAd = null
                    }

                    override fun onAdLoaded(rewardedAd: RewardedAd) {
                        super.onAdLoaded(rewardedAd)
                        mRewardedAd = rewardedAd
                    }
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()

        val lastSaveTime = viewModel.preferences.getLastResetDate()
        val currentTime = System.currentTimeMillis()
        elapsedTime = currentTime - lastSaveTime
        twentyFourHoursInMillis = 24 * 60 * 60 * 1000

        if (elapsedTime >= twentyFourHoursInMillis) {
            viewModel.preferences.resetRewardData()
            viewModel.preferences.setLastResetDate(System.currentTimeMillis())
            viewModel.preferences.setRewardCount(3)
            rewardedCount = viewModel.preferences.getRewardCount()
        }
    }

    private var startForResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val parameterFromSecondActivity = data?.getBooleanExtra(FROM_SUBSCRIPTION, false)

                if (parameterFromSecondActivity == true) {
                    if (RecoveryApplication.hasSubscription) {
                        startActivity(Intent(ImagesActivity.newIntent(this@FreeScanActivity))).also {
                            finish()
                        }
                    }
                }
            }
        }

    private fun handleBackPressed() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAffinity()
            }
        }
        onBackPressedDispatcher.addCallback(this@FreeScanActivity, callback)
    }

    companion object {
        fun newIntent(context: Context, returnScreen: String? = null) =
            Intent(context, FreeScanActivity::class.java).apply {
                putExtra(ArgumentKey.FREE_SCAN_SCREEN, returnScreen)
            }
    }
}