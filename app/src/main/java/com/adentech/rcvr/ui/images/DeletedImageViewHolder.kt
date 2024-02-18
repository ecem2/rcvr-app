package com.adentech.rcvr.ui.images


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.adentech.rcvr.core.adapter.RecoveryBaseViewHolder
import com.adentech.rcvr.data.model.FileModel
import com.adentech.rcvr.databinding.ItemDeletedImageBinding
import com.adentech.rcvr.databinding.ItemFreeScanImageBinding
import com.adentech.rcvr.extensions.executeAfter

class DeletedImageViewHolder(
    parent: ViewGroup, inflater: LayoutInflater
) : RecoveryBaseViewHolder<ItemFreeScanImageBinding>(
    binding = ItemFreeScanImageBinding.inflate(inflater, parent, false)
) {

    fun bind(item: FileModel, onItemClicked: ((item: FileModel, position: Int) -> Unit)? = null) {
        binding.executeAfter {
            this.item = item
            root.setOnClickListener {
                if (absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                    onItemClicked?.invoke(item, absoluteAdapterPosition)
                }
            }
        }
    }
}