package com.adentech.rcvr.files

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.adentech.rcvr.adapter.RecoveryBaseViewHolder
import com.adentech.rcvr.databinding.ItemDeletedFilesBinding
import com.adentech.rcvr.extensions.executeAfter
import com.adentech.rcvr.model.FileModel


class DeletedFilesViewHolder(
    parent: ViewGroup, inflater: LayoutInflater
) : RecoveryBaseViewHolder<ItemDeletedFilesBinding>(
    binding = ItemDeletedFilesBinding.inflate(inflater, parent, false)
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