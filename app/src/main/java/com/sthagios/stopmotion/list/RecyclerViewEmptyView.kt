package com.sthagios.stopmotion.list

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.TextView

/**
 * Stopmotion

 * @author stephan
 * *
 * @since 12.06.16
 */
class RecyclerViewEmptyView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
        defStyle: Int = 0) : RecyclerView(context, attrs, defStyle) {

    var mEmptyView: View? = null
    private var mEmptyTextView: TextView? = null


    private val mEmptyObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            super.onChanged()
            checkForEmptyDataSet()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            super.onItemRangeInserted(positionStart, itemCount)
            checkForEmptyDataSet()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            super.onItemRangeRemoved(positionStart, itemCount)
            checkForEmptyDataSet()
        }
    }

    fun setAdapter(adapter: Adapter<*>, view: View, textView: TextView) {
        mEmptyView = view
        mEmptyTextView = textView
        adapter.registerAdapterDataObserver(mEmptyObserver)
        super.setAdapter(adapter)
        checkForEmptyDataSet()
    }

    private fun checkForEmptyDataSet() {
        visibility = if (isEmpty()) View.GONE else View.VISIBLE

        mEmptyView!!.visibility = if (isEmpty()) View.VISIBLE else View.GONE
        mEmptyTextView!!.visibility = if (isEmpty()) View.VISIBLE else View.GONE
    }

    private fun isEmpty() = adapter == null || adapter.itemCount <= 0

}
