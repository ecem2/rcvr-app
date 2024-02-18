package com.adentech.rcvr.view.viewpager

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.adentech.rcvr.R

class PagerThirdActivity: Activity() {

    fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_pager_third, container, false)
        return view
    }

}