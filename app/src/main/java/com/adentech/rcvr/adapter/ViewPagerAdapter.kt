package com.adentech.rcvr.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.adentech.rcvr.databinding.LayoutViewPagerBinding
import com.adentech.rcvr.viewpager.PagerModel


class ViewPagerAdapter() : RecyclerView.Adapter<ViewPagerAdapter.ViewHolder>() {

    private var items: List<PagerModel> = ArrayList()

    class ViewHolder(val binding: LayoutViewPagerBinding) : RecyclerView.ViewHolder(binding.root) {

    }
    fun setPagerAdapter(newItems: List<PagerModel>) {
        items = newItems
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutViewPagerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            binding.apply {
                if (items.isNotEmpty() && position < items.size) {

                    ivPremium.setImageResource(items[position].image)
                    tvPremiumTitle.text = items[position].title
                    tvPremiumDescription.text = items[position].desc
                }else{

                }
            }
        }
    }

    override fun getItemCount() = 3
}