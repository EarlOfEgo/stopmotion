package com.sthagios.stopmotion.list

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.sthagios.stopmotion.R
import java.util.*

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   30.04.16
 */
class ImageListAdapter(private val mContext: Context) : RecyclerView.Adapter<ImageListAdapter.ViewHolder>() {


    var items: ArrayList<String> = ArrayList()

    fun addItem(item: String) {
        items.add(item)
    }


    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val text = items.get(position)
        holder!!.mImageText.text = text
        //        Glide.with(mContext).load(Uri.parse("file://android_asset/placeholder.gif")).into(holder.mImageView)
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder? {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.image_list_item, parent, false);

        return ViewHolder(view)
    }

    class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        var mImageView = itemView!!.findViewById(R.id.image_view) as ImageView
        var mImageText = itemView!!.findViewById(R.id.image_text) as TextView
    }
}