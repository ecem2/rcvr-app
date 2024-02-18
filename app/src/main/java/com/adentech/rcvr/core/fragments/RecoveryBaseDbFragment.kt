package com.adentech.rcvr.core.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

abstract class RecoveryBaseDbFragment<DB : ViewDataBinding> : RecoveryBaseFragment() {

    protected lateinit var viewBinding: DB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = DataBindingUtil.inflate(inflater, getResourceLayoutId(), container, false)
        viewBinding.lifecycleOwner = viewLifecycleOwner
        return viewBinding.root
    }

}