package com.sthagios.stopmotion.create.edit

import android.graphics.Canvas
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import java.util.*

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   09.07.16
 */
class ItemTouchHelperCallback(val mAdapter: StateAdapter, val onRemove: (Int) -> Unit) : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN,
        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
    override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?,
            target: RecyclerView.ViewHolder?): Boolean {

        if (target!!.adapterPosition >= mAdapter.imageList.size)
            return false

        Collections.swap(mAdapter.imageList, viewHolder!!.adapterPosition, target.adapterPosition)
        mAdapter.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition);
        return true;
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
        val pos = viewHolder!!.adapterPosition
        onRemove.invoke(pos)
    }

    override fun getMovementFlags(recyclerView: RecyclerView?,
            viewHolder: RecyclerView.ViewHolder?): Int {
        if (mAdapter.isEmptyView(viewHolder))
            return 0
        return makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE,
                ItemTouchHelper.DOWN or ItemTouchHelper.UP) or makeFlag(
                ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.START or ItemTouchHelper.END)
    }

    override fun clearView(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?) {
        getDefaultUIUtil().clearView((viewHolder as StateAdapter.NormalViewHolder).mCardView);
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        if (viewHolder != null && actionState == ItemTouchHelper.ACTION_STATE_SWIPE)
            getDefaultUIUtil().onSelected((viewHolder as StateAdapter.NormalViewHolder).mCardView)
        else
            super.onSelectedChanged(viewHolder, actionState)
    }

    override fun onChildDraw(c: Canvas?, recyclerView: RecyclerView?,
            viewHolder: RecyclerView.ViewHolder?, dX: Float, dY: Float, actionState: Int,
            isCurrentlyActive: Boolean) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE)
            getDefaultUIUtil().onDraw(c, recyclerView,
                    (viewHolder as StateAdapter.NormalViewHolder).mCardView, dX, dY, actionState,
                    isCurrentlyActive)
        else
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState,
                    isCurrentlyActive)
    }

    override fun onChildDrawOver(c: Canvas?, recyclerView: RecyclerView?,
            viewHolder: RecyclerView.ViewHolder?, dX: Float, dY: Float, actionState: Int,
            isCurrentlyActive: Boolean) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE)
            getDefaultUIUtil().onDrawOver(c, recyclerView,
                    (viewHolder as StateAdapter.NormalViewHolder).mCardView, dX, dY, actionState,
                    isCurrentlyActive)
        else
            super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState,
                    isCurrentlyActive)
    }

}