package com.adentech.rcvr.fragments

import android.os.Bundle
import android.view.View
import androidx.databinding.ViewDataBinding
import com.adentech.rcvr.BR
import com.adentech.rcvr.viewmodel.BaseViewModel

abstract class BaseFragment<VM : BaseViewModel, DB : ViewDataBinding> :
    RecoveryBaseVmDbFragment<VM, DB>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.setVariable(BR.viewModel, viewModel)
        onInitDataBinding()
    }

    abstract fun onInitDataBinding()
}