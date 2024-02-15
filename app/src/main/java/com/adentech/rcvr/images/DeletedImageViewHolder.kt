package com.adentech.rcvr.images


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.adentech.rcvr.adapter.RecoveryBaseViewHolder
import com.adentech.rcvr.model.FileModel
import com.adentech.rcvr.databinding.ItemDeletedImageBinding
import com.adentech.rcvr.extensions.executeAfter

class DeletedImageViewHolder(
    parent: ViewGroup, inflater: LayoutInflater
) : RecoveryBaseViewHolder<ItemDeletedImageBinding>(
    binding = ItemDeletedImageBinding.inflate(inflater, parent, false)
) {
    fun bind(item: FileModel, onItemClicked: ((item: FileModel) -> Unit)? = null) {
        binding.executeAfter {
            this.item = item
            root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClicked?.invoke(item)
                }
            }
        }
    }
}