package com.adentech.rcvr.ui.images

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.adentech.rcvr.R
import com.adentech.rcvr.data.billing.RecoveryApplication
import com.adentech.rcvr.core.common.Constants
import com.adentech.rcvr.core.common.Resource
import com.adentech.rcvr.core.common.Status
import com.adentech.rcvr.core.fragments.BaseFragment
import com.adentech.rcvr.data.model.FileModel
import com.adentech.rcvr.data.model.MainFileModel
import com.adentech.rcvr.databinding.FragmentDeletedImagesBinding
import com.adentech.rcvr.extensions.observe
import com.adentech.rcvr.extensions.withDelay
import com.adentech.rcvr.ui.home.ImagesActivity
import com.adentech.rcvr.ui.fullscreen.DeletedImageActivity
import com.adentech.rcvr.scan.ScanViewModel
import com.adentech.rcvr.ui.subscription.SubscriptionActivity
import com.adentech.rcvr.utils.FilesFetcher
import com.adentech.rcvr.utils.ImagesFilesCollector
import com.adentech.rcvr.utils.SearchAllFile
import com.adentech.rcvr.view.decoration.SpacesItemDecoration
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

@AndroidEntryPoint
class DeletedImagesFragment : BaseFragment<ScanViewModel, FragmentDeletedImagesBinding>() {

    private val deletedImagesAdapter by lazy {

        DeletedImagesAdapter(
            hasReward = false,
            context = requireContext(),
            onItemClicked = ::onItemClick
        )
    }

    private var myMotor: SearchAllFile? = null
    private var volums: ArrayList<String> = ArrayList()
    private var rewardedList: List<Int>? = null
    private val _isStoragePermissionGranted: MutableLiveData<Boolean> = MutableLiveData()
    private var rewardedCount = 0
    private var fileModel: FileModel? = null

    override fun getResourceLayoutId() = R.layout.fragment_deleted_images

    override fun viewModelClass() = ScanViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (RecoveryApplication.isPremium) {
            getFileList()
        } else {
            adjustImages()
            getFileList()
        }
    }

    override fun onInitDataBinding() {
        if (RecoveryApplication.isPremium) {
            getFileList()
            viewBinding.cardScanProgress.visibility = View.GONE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                viewModel.getAllTrashedFiles()
                observe(viewModel.allTrashedFilesList, ::getTrashedList)

            }
        } else {
            adjustImages()
            viewBinding.cardScanProgress.visibility = View.VISIBLE
            viewBinding.llPermissionError.visibility = View.GONE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                viewModel.getAllGalleryImages()
                observe(viewModel.imageList, ::getImagesList)
            } else {
                getFileList()

            }
        }
        initRecyclerView()
        viewBinding.buttonRestoreNow.setOnClickListener {
            if (RecoveryApplication.isPremium) {
                navigateToDeletedImageActivity(fileModel)
            } else {
                navigateToSubscription()
            }
        }
        setupBackButton()
    }

    private fun initRecyclerView() {
        viewBinding.rvDeletedImages.apply {
            adapter = deletedImagesAdapter
            layoutManager = GridLayoutManager(requireContext(), 3)
            val spacingInPixels = resources.getDimensionPixelSize(R.dimen.margin_6)
            addItemDecoration(SpacesItemDecoration(spacingInPixels))
            setHasFixedSize(true)
        }
    }

    private fun getFileList() {
        val newList = ImagesFilesCollector.foundImagesList.toList()
        deletedImagesAdapter.submitList(newList)
        Log.d("aaasss", "$newList")
        volums = FilesFetcher(requireContext()).getStorageVolumes()
        if (volums.isNotEmpty()) {
            myMotor = SearchAllFile(volums, requireActivity(), requireContext())
            myMotor?.execute(*arrayOfNulls(0))
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showDialog() {
        deletedImagesAdapter.rewardedList = rewardedList
        deletedImagesAdapter.notifyDataSetChanged()

        viewBinding.cardScanProgress.visibility = View.GONE
        viewBinding.clResultDialog.visibility = View.VISIBLE
        val dialogText = getString(R.string.deleted_photos_were_found)
        val color = ContextCompat.getColor(requireContext(), R.color.dialog_red_text)
        val coloredText = changeColor(dialogText, color, color)
        viewBinding.dialogTitle.text = coloredText
    }

    fun onItemClick(fileModel: FileModel, i: Int) {
        if (RecoveryApplication.isPremium) {
            navigateToDeletedImageActivity(fileModel)
        } else {

            if (fileModel?.isSelected == true) {
                navigateToDeletedImageActivity(fileModel)
            } else {
                if (rewardedCount > 0) {
                    if (fileModel?.isRewarded == true) {
                        fileModel?.isSelected = true
                    } else {
                        fileModel?.isSelected = false
                        navigateToSubscription()
                    }
                } else {
                    navigateToSubscription()
                }
            }
        }
    }

    private fun getImagesList(resource: Resource<ArrayList<FileModel>>) {
        when (resource.status) {
            Status.SUCCESS -> {
                resource.data?.let { }
            }

            Status.ERROR -> {
                viewBinding.cardScanProgress.visibility = View.GONE
                if (resource.data.isNullOrEmpty()) {
                    viewBinding.llEmptyFolder.visibility = View.VISIBLE
                }
            }

            Status.LOADING -> {
                viewBinding.progressBarLL.visibility = View.GONE
                viewBinding.progressBarLL.visibility = View.VISIBLE
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

    private fun adjustImages() {
        val min = ImagesFilesCollector.foundImagesList.size / 5
        val max = ImagesFilesCollector.foundImagesList.size / 2
        val count = if (ImagesFilesCollector.foundImagesList.size / 50 > 3) {
            160
        } else {
            ImagesFilesCollector.foundImagesList.size / 10 + 1
        }

        val indexesArray: ArrayList<Int> = ArrayList()
        val currentList: ArrayList<FileModel> = ArrayList()
        lifecycleScope.launch {
            if (ImagesFilesCollector.foundImagesList.isNotEmpty()) {
                viewBinding.llEmptyFolder.visibility = View.GONE
                viewBinding.tvProgressBar.text = getString(R.string.photos_recovered)

                while (indexesArray.size < count) {
                    val randomNumber = Random.nextInt(min, max + 1)
                    if (randomNumber !in indexesArray) {
                        indexesArray.add(randomNumber)
                    }
                }

                if (ImagesFilesCollector.foundImagesList.isNotEmpty()) {
                    for (img in 1..indexesArray.size) {
                        val randomTime = Random.nextLong(120, 230)
                        delay(randomTime)
                        withContext(Dispatchers.Main) {
                            val chosenImage: FileModel =
                                ImagesFilesCollector.foundImagesList[indexesArray[img - 1]]
                            currentList.add(0, chosenImage)
                            currentList.add(chosenImage)
                        }
                    }

                    if (!isSameList(currentList, deletedImagesAdapter.currentList)) {
                        val newList: ArrayList<FileModel> = ArrayList()
                        newList.addAll(currentList)
                        deletedImagesAdapter.submitList(currentList.toList())
                        deletedImagesAdapter.notifyDataSetChanged()
                        rewardedList = getThreeRandomPositions(newList)
                        viewBinding.rvDeletedImages.scrollToPosition(0)
                    }

                    withDelay(2000) {
                        showDialog()
                        viewBinding.clResultDialog.visibility = View.VISIBLE
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        viewBinding.cardScanProgress.visibility = View.GONE
                        viewBinding.llEmptyFolder.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun isSameList(list1: List<FileModel>?, list2: List<FileModel>?): Boolean {
        return list1 == list2 || (list1 != null && list2 != null && list1.size == list2.size && list1.containsAll(
            list2
        ))
    }

    private fun changeColor(
        string: String,
        firstText: Int,
        secondText: Int,
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

    private fun getTrashedList(resource: Resource<MainFileModel>) {
        when (resource.status) {
            Status.SUCCESS -> {
                if (resource.data?.images?.size == 0 || resource.data?.images.isNullOrEmpty()) {
                    viewBinding.llEmptyFolder.visibility = View.VISIBLE
                } else {
                    viewBinding.llEmptyFolder.visibility = View.GONE
                    if (!isSameList(deletedImagesAdapter.currentList, resource.data?.images)) {
                        // deletedImagesAdapter.submitList(resource.data?.images)
                    }
                    resource.data?.images?.let { checkProgressBar(it) }
                }
                viewBinding.progressBar.visibility = View.GONE
            }

            Status.ERROR -> {
                viewBinding.progressBar.visibility = View.GONE
            }

            Status.LOADING -> {
                if (RecoveryApplication.isPremium) {
                    viewBinding.progressLl.visibility = View.GONE
                } else {
                    viewBinding.progressLl.visibility = View.VISIBLE
                }
            }
        }
    }


    private fun checkProgressBar(list: ArrayList<FileModel>) {
        if (list.size.compareTo(0) != 0) {
            viewBinding.progressBar.visibility = View.GONE
            if (activity != null && context != null) {
                askRatings()
            } else {
                return
            }
        }
    }

    private fun navigateToSubscription() {
        startActivity(Intent(requireContext(), SubscriptionActivity::class.java))
    }

    private fun navigateToDeletedImageActivity(imagePath: FileModel?) {
        try {
            val intent = Intent(requireContext(), DeletedImageActivity::class.java)
            intent.putExtra(Constants.IMAGE_PATH, imagePath)
            startActivity(intent)
        }catch (e: java.lang.Exception){
            Log.d("aaffaa","$e")
        }

    }

    private fun setupBackButton() {
        viewBinding.backButton.setOnClickListener {
            val intent = Intent(requireContext(), ImagesActivity::class.java)
            startActivity(intent)
        }
    }

    private fun askRatings() {
        val manager: ReviewManager = ReviewManagerFactory.create(requireContext())
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo: ReviewInfo = task.result
                val flow = manager.launchReviewFlow(requireActivity(), reviewInfo)
                flow.addOnCompleteListener {

                }.addOnFailureListener {

                }
            }
        }
    }

}