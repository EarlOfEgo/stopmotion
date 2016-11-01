package com.sthagios.stopmotion.list

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget
import com.sthagios.stopmotion.R
import com.sthagios.stopmotion.image.database.Gif
import com.sthagios.stopmotion.settings.getSettingsPreferences
import com.sthagios.stopmotion.share.shareGif
import com.sthagios.stopmotion.show.ShowGifActivity
import com.sthagios.stopmotion.utils.startActivity
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

    var useThumbs: Boolean

    init {
        useThumbs = mContext.getSettingsPreferences().getBoolean("THUMBS_IN_LIST", false)

        mContext.getSettingsPreferences().registerOnSharedPreferenceChangeListener { sharedPreferences, s ->
            useThumbs = sharedPreferences.getBoolean("THUMBS_IN_LIST", useThumbs)
            notifyDataSetChanged()
        }
    }


    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val gif = data[position]
        if (gif.isValid) {
            holder?.mImageText?.text = gif.name
            if (useThumbs) {
                val uri = Uri.parse(gif.thumbnailUriString)
                Glide.with(mContext).load(uri).into(holder?.mImageView)
            } else {
                val uri = Uri.parse(gif.fileUriString)
                val target = GlideDrawableImageViewTarget(holder?.mImageView)
                Glide.with(mContext).load(uri).into(target)

            }
            holder?.mShareButton?.setOnClickListener(
                    { mContext.shareGif(gif.shareUriString, gif.name) })
            holder?.mImageView?.setOnClickListener({
                val p1: Pair<View, String> = Pair.create(holder.mImageView, "shared_image")
                val p2: Pair<View, String> = Pair.create(holder.mImageText, "shared_text")
                val trans = ActivityOptionsCompat.makeSceneTransitionAnimation(mContext as Activity,
                        p1, p2)
                mContext.startActivity<ShowGifActivity>(gif.id, 1, trans.toBundle())
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder? {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.image_list_item, parent,
                false)

        return ViewHolder(view)
    }

    class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        var mImageView = itemView?.image_view
        var mImageText = itemView?.image_text
        var mShareButton = itemView?.share_button
    }
}