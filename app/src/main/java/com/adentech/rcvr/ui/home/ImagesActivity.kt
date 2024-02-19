package com.adentech.rcvr.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.adentech.rcvr.R
import com.adentech.rcvr.core.common.ArgumentKey
import com.adentech.rcvr.core.activities.BaseActivity
import com.adentech.rcvr.data.model.StorageInfo
import com.adentech.rcvr.databinding.ActivityImagesBinding
import com.adentech.rcvr.databinding.DialogPermissionBinding
import com.adentech.rcvr.ui.audio.DeletedAudioFragment
import com.adentech.rcvr.ui.images.DeletedImagesFragment
import com.adentech.rcvr.ui.files.FilesFragment
import com.adentech.rcvr.ui.videos.DeletedVideosFragment
import com.adentech.rcvr.view.viewpager.ResultViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ImagesActivity : BaseActivity<ResultViewModel, ActivityImagesBinding>() {

    private var isStoragePermissionGranted = false

    override fun viewModelClass() = ResultViewModel::class.java

    override fun viewDataBindingClass() = ActivityImagesBinding::class.java

    override fun onInitDataBinding() {
        setupUI()
        //handleBackPressed()
        isStoragePermissionGranted = checkStoragePermissions()

        if (isStoragePermissionGranted) {
            viewBinding.llButtons.visibility = View.VISIBLE
            viewBinding.llPermissionError.visibility = View.GONE
        } else {
            showPermissionDialog()
        }
    }

    private fun setupUI() {
        val firstWord = SpannableString(getString(R.string.internal))
        firstWord.setSpan(AbsoluteSizeSpan(14, true), 0, firstWord.length, 0)

        val secondWord = SpannableString("\n"+ getString(R.string.storage))
        secondWord.setSpan(AbsoluteSizeSpan(28, true), 0, secondWord.length, 0)

        val finalText = SpannableStringBuilder()
            .append(firstWord)
            .append(" ")
            .append(secondWord)

        viewBinding.internalText.text = finalText

        val autoTextMinSizeFirstWord = 12
        val autoTextMaxSizeFirstWord = 16

        val autoTextMinSizeSecondWord = 26
        val autoTextMaxSizeSecondWord = 30

        setAutoSizeText(viewBinding.internalText, firstWord.length, autoTextMinSizeFirstWord, autoTextMaxSizeFirstWord)
        setAutoSizeText(viewBinding.internalText, finalText.length, autoTextMinSizeSecondWord, autoTextMaxSizeSecondWord)
    }

    private fun setAutoSizeText(textView: TextView, length: Int, minSize: Int, maxSize: Int) {
        val screenWidth = resources.displayMetrics.widthPixels
        val padding = textView.paddingStart + textView.paddingEnd

        // Calculate the available width for text
        val availableWidth = screenWidth - padding

        // Calculate the maximum number of characters that can fit in the available width
        val maxCharacters = (availableWidth / (maxSize.toFloat() / length)).toInt()

        // Calculate the adjusted text size based on the available width
        val adjustedTextSize = minOf(maxSize.toFloat(), (availableWidth / maxCharacters).toFloat())

        // Set the text size
        textView.textSize = if (adjustedTextSize < minSize) minSize.toFloat() else adjustedTextSize

        showStorageStatus()

        viewBinding.videoCardView.setOnClickListener {
            showDeletedVideosFragment()
            viewBinding.imageCardView.visibility = View.GONE
            viewBinding.videoCardView.visibility = View.GONE
            viewBinding.audioCardView.visibility = View.GONE
            viewBinding.fileCardView.visibility = View.GONE
            viewBinding.storageCardView.visibility = View.GONE
            viewBinding.recoveryText.visibility = View.GONE
            viewBinding.clearText.visibility = View.GONE
        }
        viewBinding.imageCardView.setOnClickListener {
            showDeletedImagesFragment()
            viewBinding.imageCardView.visibility = View.GONE
            viewBinding.videoCardView.visibility = View.GONE
            viewBinding.audioCardView.visibility = View.GONE
            viewBinding.fileCardView.visibility = View.GONE
            viewBinding.storageCardView.visibility = View.GONE
            viewBinding.recoveryText.visibility = View.GONE
            viewBinding.clearText.visibility = View.GONE
        }
        viewBinding.fileCardView.setOnClickListener {
            showDeletedFilesFragment()
            viewBinding.imageCardView.visibility = View.GONE
            viewBinding.videoCardView.visibility = View.GONE
            viewBinding.audioCardView.visibility = View.GONE
            viewBinding.fileCardView.visibility = View.GONE
            viewBinding.storageCardView.visibility = View.GONE
            viewBinding.recoveryText.visibility = View.GONE
            viewBinding.clearText.visibility = View.GONE
        }
        viewBinding.audioCardView.setOnClickListener {
            showDeletedAudiosFragment()
            viewBinding.imageCardView.visibility = View.GONE
            viewBinding.videoCardView.visibility = View.GONE
            viewBinding.audioCardView.visibility = View.GONE
            viewBinding.fileCardView.visibility = View.GONE
            viewBinding.storageCardView.visibility = View.GONE
            viewBinding.recoveryText.visibility = View.GONE
            viewBinding.clearText.visibility = View.GONE
        }
    }
    private fun showDeletedVideosFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.cl_container, DeletedVideosFragment())
        transaction.commit()
    }
    private fun showDeletedImagesFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.cl_container, DeletedImagesFragment())
        transaction.commit()
    }
    private fun showDeletedFilesFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.cl_container, FilesFragment())
        transaction.commit()
    }
    private fun showDeletedAudiosFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.cl_container, DeletedAudioFragment())
        transaction.commit()
    }

    private fun handleBackPressed() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAffinity()
            }
        }
        onBackPressedDispatcher.addCallback(this@ImagesActivity, callback)
    }

    @SuppressLint("ResourceType")
    private fun showStorageStatus() {
        val (totalSpace, freeSpace) = getStorageInfo()

        val totalGB = totalSpace / (1024 * 1024 * 1024).toFloat()
        val freeGB = freeSpace / (1024 * 1024 * 1024).toFloat()

        val usedSpace = totalSpace - freeSpace
        val usedGB = usedSpace / (1024 * 1024 * 1024).toFloat()

        val progressStatus = viewBinding.storageTextView

        val usedPercentage = ((usedSpace.toFloat() / totalSpace.toFloat()) * 100).toInt()

        progressStatus.text = "$usedPercentage%"

        viewBinding.gbTv.text = "${String.format("%.2f", usedGB)} GB/${String.format("%.2f", totalGB)} GB"
        viewBinding.progressBar.progress = usedPercentage

        val transparentBackgroundDrawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.TRANSPARENT)
            viewBinding.progressBar.progress = usedPercentage
        }


        val progressDrawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setStroke(50, ContextCompat.getColor(this@ImagesActivity, R.color.transparent40))

        }

        val layerDrawable = LayerDrawable(arrayOf(transparentBackgroundDrawable, progressDrawable))
        viewBinding.transparentBackground.background = layerDrawable
    }

    private fun getStorageInfo(): StorageInfo {
        val storageDirectory = Environment.getExternalStorageDirectory()
        val stat = StatFs(storageDirectory.path)

        val totalBytes = stat.totalBytes
        val freeBytes = stat.availableBytes

        return StorageInfo(totalBytes, freeBytes)
    }

    private fun showPermissionDialog() {
        val dialogBuilder = Dialog(this@ImagesActivity, R.style.CustomDialog)
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
                viewBinding.llButtons.visibility = View.GONE
                viewBinding.llPermissionError.visibility = View.VISIBLE

                viewBinding.dialogButtonSettings.setOnClickListener {
                    requestForStoragePermissions()
                }
                dialogBuilder.cancel()
            }
        }
        dialogBuilder.show()
    }

    override fun onResume() {
        super.onResume()
        if (isStoragePermissionGranted) {
            viewBinding.llButtons.visibility = View.VISIBLE
            viewBinding.llPermissionError.visibility = View.GONE
        } else {
            viewBinding.llButtons.visibility = View.GONE
            //viewBinding.llPermissionError.visibility = View.VISIBLE
        }
    }

    fun checkStoragePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //Android is 11 (R) or above
            Environment.isExternalStorageManager()
        } else {
            //Below android 11
            val write =
                ContextCompat.checkSelfPermission(this@ImagesActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read =
                ContextCompat.checkSelfPermission(this@ImagesActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
            read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestForStoragePermissions() {
        //Android is 11 (R) or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package", this.packageName, null)
                intent.data = uri
                storageActivityResultLauncher.launch(intent)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                storageActivityResultLauncher.launch(intent)
            }
        } else {
            //Below android 11
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                STORAGE_PERMISSION_CODE
            )
        }
    }

    private val storageActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                //Android is 11 (R) or above
                isStoragePermissionGranted = Environment.isExternalStorageManager()
                if (!isStoragePermissionGranted) {
                    viewBinding.llPermissionError.visibility = View.VISIBLE
                }
            }
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty()) {
                val write = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val read = grantResults[1] == PackageManager.PERMISSION_GRANTED
                isStoragePermissionGranted = read && write
            }
        }
    }

    companion object {
        const val STORAGE_PERMISSION_CODE = 23

        fun newIntent(context: Context, returnScreen: String? = null) =
            Intent(context, ImagesActivity::class.java).apply {
                putExtra(ArgumentKey.IMAGES_SCREEN, returnScreen)
            }
    }
}





