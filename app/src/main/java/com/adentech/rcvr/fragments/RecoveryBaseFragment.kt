package com.adentech.rcvr.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

abstract class RecoveryBaseFragment : Fragment() {

    @LayoutRes
    protected abstract fun getResourceLayoutId(): Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(getResourceLayoutId(), container, false)
    }

    open fun getFragmentTag(): String? {
        return this.javaClass.simpleName
    }
}