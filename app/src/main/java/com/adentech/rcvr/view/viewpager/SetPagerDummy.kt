package com.adentech.rcvr.view.viewpager

import com.adentech.rcvr.R

class SetPagerDummy {
    companion object{
        fun setDataPager(): ArrayList<PagerModel>{
            val list = ArrayList<PagerModel>()
            list.add(
                PagerModel(R.string.image_recovery, R.string.photo_recovery_description, R.mipmap.ic_photo_subs)
            )
            list.add(
                PagerModel(R.string.video_recovery,R.string.video_recovery_description, R.mipmap.ic_video_subs)
            )
            list.add(
                PagerModel(R.string.audio_recovery,R.string.audio_recovery_description, R.mipmap.ic_audio_subs)
            )
            return list
        }
    }
}