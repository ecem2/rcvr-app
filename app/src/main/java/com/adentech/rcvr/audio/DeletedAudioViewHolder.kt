package com.adentech.rcvr.audio

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.adentech.rcvr.adapter.RecoveryBaseViewHolder
import com.adentech.rcvr.databinding.ItemDeletedAudioBinding
import com.adentech.rcvr.extensions.executeAfter
import com.adentech.rcvr.model.FileModel


class DeletedAudioViewHolder(
    parent: ViewGroup, inflater: LayoutInflater
) : RecoveryBaseViewHolder<ItemDeletedAudioBinding>(
    binding = ItemDeletedAudioBinding.inflate(inflater, parent, false)
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