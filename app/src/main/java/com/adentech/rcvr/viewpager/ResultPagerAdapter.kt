package com.adentech.rcvr.viewpager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.adentech.rcvr.files.FilesFragment
import com.adentech.rcvr.images.DeletedImagesFragment

class ResultPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DeletedImagesFragment()
            1 -> FilesFragment() //GalleryImagesFragment()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}