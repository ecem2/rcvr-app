package com.adentech.rcvr.ui.subscription

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.adentech.rcvr.databinding.LayoutViewPagerBinding
import com.adentech.rcvr.extensions.getString
import com.adentech.rcvr.view.viewpager.PagerModel


class ViewPagerAdapter(private val context: Context) : RecyclerView.Adapter<ViewPagerAdapter.ViewHolder>() {

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
                    tvPremiumTitle.text = context.getString(items[position].title)
                    tvPremiumDescription.text = context.getString(items[position].desc)
                }
            }
        }
    }

    override fun getItemCount() = 3
}