package com.adentech.rcvr.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class SpacesItemDecoration(private val space: Int) : ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        outRect.left = space
        outRect.right = space
        outRect.bottom = space

        if (parent.getChildLayoutPosition(view) == 0) {
            outRect.top = 0
        } else {
            outRect.top = 0
        }
    }
}