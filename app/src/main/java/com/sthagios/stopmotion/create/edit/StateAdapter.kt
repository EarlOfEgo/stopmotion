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
class StateAdapter(val mContext: Context, var imageList: ArrayList<String>, val itemClick: (String) -> Unit, val onEmptyClick: () -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun remove(position: Int): String {
        val item = imageList[position]
        imageList.remove(item)
        notifyItemRemoved(position)
        return item
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is NormalViewHolder) {
            with(imageList[position]) {
                Glide.with(mContext).load(this).into(holder.mImageView)
                holder.mImageView.setOnClickListener {
                    itemClick.invoke(this)
                    mSelectedItemId = this.hashCode().toLong()
                }
            }
        } else if (holder is EmptyViewHolder) {
            holder.mCardView.setOnClickListener {
                onEmptyClick.invoke()
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return imageList[position].hashCode().toLong()
    }

    private val EMPTY_VIEW = 1

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {

        if (viewType == EMPTY_VIEW) {
            val view = LayoutInflater.from(parent!!.context).inflate(R.layout.image_list_item_empty,
                    parent, false)
            return EmptyViewHolder(view)
        }
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.image_list_item_new,
                parent, false)
        return NormalViewHolder(view)
    }

    fun isEmptyView(viewHolder: RecyclerView.ViewHolder?) = viewHolder is EmptyViewHolder

    override fun getItemCount(): Int {
        if (imageList.size == 0) {
            return 1
        }
        return imageList.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        if (position == imageList.size)
            return EMPTY_VIEW
        return super.getItemViewType(position)
    }

    class NormalViewHolder(itemView: View?) : RecyclerView.ViewHolder(
            itemView) {
        var mImageView = itemView!!.image_view
        var mCardView = itemView!!.card_view
    }

    class EmptyViewHolder(itemView: View?) : RecyclerView.ViewHolder(
            itemView) {
        var mCardView = itemView!!.card_view
    }

    fun addItem(pos: Int, toAdd: String) {
        imageList.add(pos, toAdd)
        notifyItemInserted(pos)
    }

    var mSelectedItemId: Long = getItemId(0)
}