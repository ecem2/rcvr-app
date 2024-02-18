package com.adentech.rcvr.core.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.adentech.rcvr.core.common.ViewBindingUtil
import com.adentech.rcvr.databinding.ActivityBaseBinding
import com.adentech.rcvr.core.viewmodel.BaseViewModel
import com.google.android.gms.ads.interstitial.InterstitialAd

abstract class BaseActivity<VM : BaseViewModel, DB : ViewDataBinding> :
    RecoveryBaseVmDbActivity<VM, DB>() {

    private lateinit var baseViewBinding: ViewDataBinding
    private var mInterstitialAd: InterstitialAd? = null

    private val _isStoragePermissionGranted: MutableLiveData<Boolean> = MutableLiveData()
    val isStoragePermissionGranted: LiveData<Boolean> = _isStoragePermissionGranted

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        if (checkStoragePermissions()) {
            _isStoragePermissionGranted.postValue(true)
        } else {
            _isStoragePermissionGranted.postValue(false)
        }
    }

    override fun setResourceViewBinding(): View {
        baseViewBinding =
            ViewBindingUtil.inflate<ActivityBaseBinding>(layoutInflater)
        viewBinding = ViewBindingUtil.inflate(
            layoutInflater,
            (baseViewBinding as ActivityBaseBinding).baseContentFrame,
            true,
            viewDataBindingClass()
        )
        viewBinding.lifecycleOwner = this
        return baseViewBinding.root
    }

    fun checkStoragePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //Android is 11 (R) or above
            Environment.isExternalStorageManager()
        } else {
            //Below android 11
            val write =
                ContextCompat.checkSelfPermission(this@BaseActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read =
                ContextCompat.checkSelfPermission(this@BaseActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
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
                _isStoragePermissionGranted.postValue(Environment.isExternalStorageManager())
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
                _isStoragePermissionGranted.postValue(read && write)
            }
        }
    }

    fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    companion object {
        const val STORAGE_PERMISSION_CODE = 23
    }
}