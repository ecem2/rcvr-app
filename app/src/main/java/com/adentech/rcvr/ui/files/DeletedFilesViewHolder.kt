package com.adentech.rcvr.ui.files

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.adentech.rcvr.core.adapter.RecoveryBaseViewHolder
import com.adentech.rcvr.databinding.ItemDeletedFilesBinding
import com.adentech.rcvr.extensions.executeAfter
import com.adentech.rcvr.data.model.FileModel


class DeletedFilesViewHolder(
    parent: ViewGroup, inflater: LayoutInflater
) : RecoveryBaseViewHolder<ItemDeletedFilesBinding>(
    binding = ItemDeletedFilesBinding.inflate(inflater, parent, false)
) {
    fun bind(item: FileModel, onItemClicked: ((item: FileModel) -> Unit)? = null) {
        binding.executeAfter {
            this.item = item
            root.setOnClickListener {
                if (absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                    onItemClicked?.invoke(item)
                }
            }
        }
    }
}