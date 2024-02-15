package com.adentech.rcvr.activities

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.adentech.rcvr.viewmodel.RecoveryBaseViewModel

abstract class RecoveryBaseVmActivity<VM : RecoveryBaseViewModel> : RecoveryBaseActivity() {

    protected lateinit var viewModel: VM

    abstract fun viewModelClass(): Class<VM>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[viewModelClass()]
    }
}