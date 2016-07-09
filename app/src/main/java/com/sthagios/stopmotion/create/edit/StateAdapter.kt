package com.sthagios.stopmotion.create.edit

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.sthagios.stopmotion.R
import kotlinx.android.synthetic.main.image_list_item_new.view.*
import java.util.*

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   09.07.16
 */
class StateAdapter(val mContext: Context, var imageList: ArrayList<String>, val itemClick: (String) -> Unit) : RecyclerView.Adapter<StateAdapter.ViewHolder>() {

    fun remove(position: Int): String {
        val item = imageList[position]
        imageList.remove(item)
        notifyItemRemoved(position)
        return item
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        with(imageList[position]) {
            Glide.with(mContext).load(this).into(holder!!.mImageView)
            holder.mImageView.setOnClickListener {
                itemClick.invoke(this)
                mSelectedItemId = this.hashCode().toLong()
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return imageList[position].hashCode().toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder? {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.image_list_item_new,
                parent,
                false);
        return StateAdapter.ViewHolder(view, itemClick)
    }

    override fun getItemCount() = imageList.size

    class ViewHolder(itemView: View?, val itemClick: (String) -> Unit) : RecyclerView.ViewHolder(
            itemView) {
        var mImageView = itemView!!.image_view
        var mCardView = itemView!!.card_view
    }

    fun addItem(pos: Int, toAdd: String) {
        imageList.add(pos, toAdd)
        notifyItemInserted(pos)
    }

    var mSelectedItemId: Long = getItemId(0)
}