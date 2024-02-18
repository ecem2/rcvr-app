package com.adentech.rcvr.ui.fullscreen


import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import com.adentech.rcvr.R
import com.adentech.rcvr.core.common.Constants
import com.adentech.rcvr.core.common.Constants.DATE_FORMAT
import com.adentech.rcvr.core.common.Constants.DCIM_PATH
import com.adentech.rcvr.core.common.Constants.IMAGE_JPG
import com.adentech.rcvr.core.common.Constants.JPG_EXTENSION
import com.adentech.rcvr.core.activities.BaseActivity
import com.adentech.rcvr.data.model.FileModel
import com.adentech.rcvr.databinding.ActivityDeletedImageBinding
import com.adentech.rcvr.databinding.DialogRecoverBinding
import com.adentech.rcvr.extensions.parcelable
import com.adentech.rcvr.ui.images.DeletedImagesFragment
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
class DeletedImageActivity : BaseActivity<ScanViewModel, ActivityDeletedImageBinding>() {

    private var imageClicked: Boolean = false
    private var imagePath: FileModel? = null

    override fun viewModelClass() = ScanViewModel::class.java

    override fun viewDataBindingClass() = ActivityDeletedImageBinding::class.java

    override fun onInitDataBinding() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )
        viewBinding.clDeletedContainer.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        imagePath = intent?.parcelable(Constants.IMAGE_PATH)
        getImageData()
        setupUI()
        clickListeners()
    }

    private fun getImageData() {

        if (imagePath != null) {
            this.imagePath = imagePath
            val creationDate = imagePath?.creationDate?.let { Date(it) }
            val format = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            val formattedDate = creationDate?.let { format.format(it) }
            viewBinding.tvPhotoInfo.text = formattedDate.toString()
        } else {
            finish()
        }
    }

    private fun clickListeners() {
        viewBinding.apply {
            ivBigImage.setOnClickListener {
                if (imageClicked) {
                    imageClicked = false
                    clToolbar.visibility = View.VISIBLE
                    buttonRestore.visibility = View.VISIBLE
                } else {
                    imageClicked = true
                    clToolbar.visibility = View.GONE
                    buttonRestore.visibility = View.GONE
                }
            }

            buttonRestore.setOnClickListener {
                launchRecoverProcess()
            }

            ivBackButton.setOnClickListener {
                navigateToFragment()
                viewBinding.ivBigImage.visibility = View.GONE
                viewBinding.imageProgressBar.visibility = View.GONE
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
                viewBinding.imageProgressBar.visibility = View.GONE
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                viewBinding.imageProgressBar.visibility = View.GONE
                return false
            }
        }

        Glide.with(this@DeletedImageActivity)
            .load(imagePath?.imageUri)
            .listener(requestListener)
            .into(viewBinding.ivBigImage)
    }

    private fun launchRecoverProcess() {
        val singleThreadedExecutor = Executors.newSingleThreadExecutor()
        singleThreadedExecutor.execute {
            runOnUiThread {
                onBtnSavePng()
                showRecoverPopup()
            }
        }
    }

    private fun showRecoverPopup() {
        val dialogBuilder = Dialog(this@DeletedImageActivity, R.style.CustomDialog)
        val dialogBinding = DialogRecoverBinding.inflate(layoutInflater)
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
    private fun navigateToFragment(){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val fragment = DeletedImagesFragment()
        fragmentTransaction.replace(R.id.cl_deleted_container, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }



    private fun onBtnSavePng() {
        try {
            val fileName: String = System.currentTimeMillis().toString() + JPG_EXTENSION
            val values = ContentValues()
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            values.put(MediaStore.Images.Media.MIME_TYPE, IMAGE_JPG)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, DCIM_PATH)
            } else {
                val directory =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                val file = File(directory, fileName)
                values.put(MediaStore.MediaColumns.DATA, file.absolutePath)
            }

            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            contentResolver.openOutputStream(uri!!).use { output ->
                if (viewBinding.ivBigImage.drawable != null) {
                    viewBinding.buttonRestore.isClickable = true
                    viewBinding.buttonRestore.isEnabled = true
                    val bm: Bitmap = viewBinding.ivBigImage.drawable.toBitmap()
                    viewBinding.ivBigImage.buildDrawingCache()
                    if (output != null) {
                        bm.compress(Bitmap.CompressFormat.JPEG, 100, output)
                    }
                } else {
                    viewBinding.buttonRestore.isClickable = false
                    viewBinding.buttonRestore.isEnabled = false
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this@DeletedImageActivity, e.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }

    private fun openFolder() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        val uri = Uri.parse(
            StringBuilder(Environment.getExternalStorageDirectory().path).append(DCIM_PATH)
                .toString()
        )
        intent.setDataAndType(uri, "*/*")
        startActivity(Intent.createChooser(intent, "Open folder"))
    }
}