package com.sthagios.stopmotion.list

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import java.util.*

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   30.04.16
 */
class ImageListAdapter : RecyclerView.Adapter<ImageListAdapter.ViewHolder>() {


    var items : ArrayList<String> = ArrayList()

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        throw UnsupportedOperationException()
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder? {
        throw UnsupportedOperationException()
    }

    class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

    }

}