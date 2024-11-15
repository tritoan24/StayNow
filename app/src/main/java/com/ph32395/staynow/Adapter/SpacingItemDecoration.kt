package com.ph32395.staynow.Adapter

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SpacingItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view) // Vị trí của item
        val spanCount = (parent.layoutManager as? GridLayoutManager)?.spanCount ?: 1
        val itemCount = parent.adapter?.itemCount ?: 0
        // Khoảng cách giữa các item
        // Khoảng cách trên/dưới
        outRect.top = space
        outRect.bottom = space

        // Khoảng cách trái/phải
        val column = position % spanCount // Vị trí cột
        outRect.left = space - column * space / spanCount
        outRect.right = (column + 1) * space / spanCount

        // Không cần khoảng cách dưới cho item cuối
        if (position >= itemCount - spanCount) {
            outRect.bottom = 0
        }
    }
}