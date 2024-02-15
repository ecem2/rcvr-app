package com.adentech.rcvr.files

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.adentech.rcvr.model.FileModel
import com.adentech.rcvr.recyclerview.RecoveryListAdapter


class DeletedFilesAdapter(
    val context: Context,
    private val onItemClicked: ((item: FileModel) -> Unit)? = null
) : RecoveryListAdapter<FileModel>(
    itemsSame = { old, new -> old == new },
    contentsSame = { old, new -> old == new }
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        inflater: LayoutInflater,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return DeletedFilesViewHolder(parent, inflater)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DeletedFilesViewHolder) {
            val item = getItem(position)
            holder.bind(item, onItemClicked)
        }
    }
}