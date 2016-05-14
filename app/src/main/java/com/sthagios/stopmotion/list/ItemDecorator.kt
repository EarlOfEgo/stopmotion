package com.sthagios.stopmotion.list

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   14.05.16
 */
class ItemDecorator : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.bottom = 8
        outRect.top = 8
        outRect.left = 8
        outRect.right = 8
    }
}