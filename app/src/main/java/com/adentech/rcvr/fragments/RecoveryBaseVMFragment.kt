package com.adentech.rcvr.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.adentech.rcvr.viewmodel.RecoveryBaseViewModel

abstract class RecoveryBaseVMFragment<VM : RecoveryBaseViewModel> :
    RecoveryBaseFragment() {

    protected lateinit var viewModel: VM

    abstract fun viewModelClass(): Class<VM>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = createViewModel()
    }

    private fun createViewModel(): VM {
        return ViewModelProvider(this)[viewModelClass()]
    }
}