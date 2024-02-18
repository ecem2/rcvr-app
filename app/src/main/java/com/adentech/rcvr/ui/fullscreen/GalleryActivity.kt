package com.adentech.rcvr.ui.fullscreen

import android.app.Activity
import android.app.Dialog
import android.app.RecoverableSecurityException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.adentech.rcvr.R
import com.adentech.rcvr.core.common.Constants.DATE_FORMAT
import com.adentech.rcvr.core.common.Constants.IMAGE_DELETED
import com.adentech.rcvr.core.common.Constants.IMAGE_PATH
import com.adentech.rcvr.core.activities.BaseActivity
import com.adentech.rcvr.data.model.FileModel
import com.adentech.rcvr.databinding.ActivityGalleryBinding
import com.adentech.rcvr.databinding.DialogDeleteBinding
import com.adentech.rcvr.extensions.parcelable
import com.adentech.rcvr.scan.ScanViewModel
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

@AndroidEntryPoint
class GalleryActivity : BaseActivity<ScanViewModel, ActivityGalleryBinding>() {

    private lateinit var willDeleteImage: FileModel
    private var imageClicked: Boolean = false

    override fun viewModelClass() = ScanViewModel::class.java

    override fun viewDataBindingClass() = ActivityGalleryBinding::class.java

    override fun onInitDataBinding() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )
        viewBinding.clContainer.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        willDeleteImage = intent.parcelable<FileModel>(IMAGE_PATH)!!
        setupUI()
        clickListeners()
    }

    private fun clickListeners() {
        hideShowToolbar()
        navigateBack()
        onDeleteClicked()
    }

    private fun onDeleteClicked() {
        viewBinding.clDelete.setOnClickListener {
            showDeletePopup()
        }
    }

    private fun hideShowToolbar() {
        viewBinding.apply {
            ivBigImage.setOnClickListener {
                if (imageClicked) {
                    imageClicked = false
                    clToolbar.visibility = View.VISIBLE
                    clDelete.visibility = View.VISIBLE
                } else {
                    imageClicked = true
                    clToolbar.visibility = View.GONE
                    clDelete.visibility = View.GONE
                }
            }
        }
    }

    private fun navigateBack() {
        viewBinding.ivBackButton.setOnClickListener {
            returnBack(false)
        }
    }

    private fun setupUI() {
        val creationDate = willDeleteImage.creationDate?.let { Date(it) }
        val format = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        val formattedDate = creationDate?.let { format.format(it) }
        viewBinding.tvPhotoInfo.text = formattedDate.toString()
        Glide.with(this@GalleryActivity)
            .load(willDeleteImage.imageUri)
            .into(viewBinding.ivBigImage)
    }

    private fun showDeletePopup() {
        val dialogBuilder = Dialog(this@GalleryActivity, R.style.CustomDialog)
        val dialogBinding = DialogDeleteBinding.inflate(layoutInflater)
        dialogBuilder.setContentView(dialogBinding.root)
        dialogBuilder.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialogBinding.apply {
            cancelButton.setOnClickListener {
                dialogBuilder.cancel()
            }

            deleteButton.setOnClickListener {
                launchDeleteProcess()
                dialogBuilder.cancel()
            }
        }

        dialogBuilder.show()
    }

    private fun launchDeleteProcess() {
        val service = Executors.newSingleThreadExecutor()
        service.execute {
            runOnUiThread {
                viewModel.deleteImage(willDeleteImage)
                launchGalleryDelete(arrayListOf(willDeleteImage))
            }
        }
    }

    private var deleteResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.deleteImage(willDeleteImage)
            returnBack(true)
        } else {
            Toast.makeText(
                this@GalleryActivity,
                getString(R.string.could_not_delete),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun launchGalleryDelete(selectedImages: ArrayList<FileModel>) {
        val imageUriList: ArrayList<Uri> = ArrayList()
        selectedImages.forEach {
            it.imageUri?.let { it1 -> imageUriList.add(it1) }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intentSender =
                MediaStore.createDeleteRequest(contentResolver, imageUriList).intentSender
            val senderRequest = IntentSenderRequest.Builder(intentSender)
                .setFillInIntent(null)
                .setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION, 0)
                .build()
            deleteResultLauncher.launch(senderRequest)
        } else {
            try {
                imageUriList.forEach {
                    contentResolver.delete(it, null, null)
                }
                returnBack(true)
            } catch (securityException: SecurityException) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val recoverableSecurityException =
                        securityException as? RecoverableSecurityException
                            ?: throw securityException
                    val senderRequest = IntentSenderRequest.Builder(
                        recoverableSecurityException.userAction.actionIntent.intentSender
                    ).build()
                    deleteResultLauncher.launch(senderRequest)
                }
            }
        }
    }

    private fun returnBack(isDeleted: Boolean) {
        val intent = Intent()
        intent.putExtra(IMAGE_DELETED, isDeleted)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}