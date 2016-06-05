package com.sthagios.stopmotion.list

import android.content.Context
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.sthagios.stopmotion.R
import com.sthagios.stopmotion.image.database.Gif
import com.sthagios.stopmotion.share.shareGif
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import kotlinx.android.synthetic.main.image_list_item.view.*

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   30.04.16
 */
class ImageListAdapter(private val mContext: Context, data: OrderedRealmCollection<Gif>) : RealmRecyclerViewAdapter<Gif, ImageListAdapter.ViewHolder>(
        mContext, data, true) {


    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val gif = data[position]
        holder!!.mImageText.text = gif.name
        val uri = Uri.parse(gif.fileUriString)
        Glide.with(mContext).load(uri).into(holder.mImageView)
        holder.mShareButton.setOnClickListener({ mContext.shareGif(gif.shareUriString) })
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder? {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.image_list_item, parent,
                false);

        return ViewHolder(view)
    }

    class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        var mImageView = itemView!!.image_view
        var mImageText = itemView!!.image_text
        var mShareButton = itemView!!.share_button
    }
}