package com.adentech.rcvr.fullscreen

import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.adentech.rcvr.R
import com.adentech.rcvr.common.Constants
import com.adentech.rcvr.activities.BaseActivity
import com.adentech.rcvr.model.FileModel
import com.adentech.rcvr.databinding.ActivityDeletedVideoBinding
import com.adentech.rcvr.databinding.DialogRecoverVideoBinding
import com.adentech.rcvr.videos.DeletedVideosFragment
import com.adentech.rcvr.scan.ScanViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

@AndroidEntryPoint
class DeletedVideoActivity : BaseActivity<ScanViewModel, ActivityDeletedVideoBinding>() {

    private var videoClicked: Boolean = false
    val videoFile: FileModel? = intent?.getParcelableExtra(Constants.VIDEO_PATH)

    override fun viewModelClass() = ScanViewModel::class.java

    override fun viewDataBindingClass() = ActivityDeletedVideoBinding::class.java

    override fun onInitDataBinding() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )
        viewBinding.clDeletedContainer.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        getVideoData()
        setupUI()
        clickListeners()
    }

    private fun getVideoData() {

        if (videoFile != null) {
            val creationDate = videoFile.creationDate?.let { Date(it) }
            val format = SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault())
            val formattedDate = creationDate?.let { format.format(it) }
            viewBinding.tvVideoInfo.text = formattedDate.toString()
        } else {
            Log.e("getVideoData", "Video file is null")
            finish()
        }
    }
    private fun clickListeners() {
        viewBinding.apply {
            ivBigVideo.setOnClickListener {
                if (videoClicked) {
                    videoClicked = false
                    clToolbar.visibility = View.VISIBLE
                    buttonRestore.visibility = View.VISIBLE
                } else {
                    videoClicked = true
                    clToolbar.visibility = View.GONE
                    buttonRestore.visibility = View.GONE
                }
            }

            buttonRestore.setOnClickListener {
                launchRecoverProcess()
            }

            ivBackButton.setOnClickListener {
                navigateToFragment()
                viewBinding.ivBigVideo.visibility = View.GONE
                viewBinding.videoProgressBar.visibility = View.GONE
                viewBinding.clToolbar.visibility = View.GONE
                viewBinding.buttonRestore.visibility = View.GONE
            }
        }
    }

    private fun setupUI() {
        val requestListener = object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                viewBinding.videoProgressBar.visibility = View.GONE
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                viewBinding.videoProgressBar.visibility = View.GONE
                return false
            }
        }

        Glide.with(this@DeletedVideoActivity)
            .load(videoFile?.videoUri)
            .listener(requestListener)
            .into(viewBinding.ivBigVideo)
    }

    private fun launchRecoverProcess() {
        val singleThreadedExecutor = Executors.newSingleThreadExecutor()
        singleThreadedExecutor.execute {
            runOnUiThread {
                onBtnSaveVideo()
                showRecoverPopup()
            }
        }
    }
    private fun navigateToFragment(){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val fragment = DeletedVideosFragment()
        fragmentTransaction.replace(R.id.cl_deleted_container, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }
    private fun showRecoverPopup() {
        val dialogBuilder = Dialog(this@DeletedVideoActivity, R.style.CustomDialog)
        val dialogBinding = DialogRecoverVideoBinding.inflate(layoutInflater)
        dialogBuilder.setContentView(dialogBinding.root)
        dialogBuilder.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialogBinding.apply {
            checkButton.setOnClickListener {
                openFolder()
                dialogBuilder.cancel()
                finish()
            }

            cancelButton.setOnClickListener {
                dialogBuilder.cancel()
            }
        }

        dialogBuilder.show()
    }

    private fun onBtnSaveVideo() {
        try {
            val fileName: String = System.currentTimeMillis().toString() + Constants.MP4_EXTENSION
            val values = ContentValues()
            values.put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
            values.put(MediaStore.Video.Media.MIME_TYPE, Constants.VIDEO_MP4)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Constants.DCIM_PATH)
            } else {
                val directory =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                val file = File(directory, fileName)
                values.put(MediaStore.MediaColumns.DATA, file.absolutePath)
            }

            val uri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
            contentResolver.openOutputStream(uri!!).use { output ->
                val inputStream = videoFile?.imageUri?.let { contentResolver.openInputStream(it) }

                if (inputStream != null) {
                    val buffer = ByteArray(4 * 1024)
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        output?.write(buffer, 0, read)
                    }
                    inputStream.close()
                } else {
                    viewBinding.buttonRestore.isClickable = false
                    viewBinding.buttonRestore.isEnabled = false
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this@DeletedVideoActivity, e.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }
    private fun openFolder() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        val uri = Uri.parse(
            StringBuilder(Environment.getExternalStorageDirectory().path).append(Constants.DCIM_PATH)
                .toString()
        )
        intent.setDataAndType(uri, "*/*")
        startActivity(Intent.createChooser(intent, "Open folder"))
    }
}