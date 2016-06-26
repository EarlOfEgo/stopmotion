package com.sthagios.stopmotion.create.edit

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.sthagios.stopmotion.R
import com.sthagios.stopmotion.utils.retrieveStringListParameter
import kotlinx.android.synthetic.main.activity_edit_images.*
import kotlinx.android.synthetic.main.state_list_item.view.*
import java.util.*

class EditImagesActivity : AppCompatActivity() {

    private lateinit var mPictureList: ArrayList<String>

    private lateinit var mAdapter: StateAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_images)

        mPictureList = retrieveStringListParameter()

        image_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        mAdapter = StateAdapter(this, mPictureList,
                { Glide.with(this).load(it).into(image_preview) })

        image_list.adapter = mAdapter
        Glide.with(this).load(mPictureList.first()).into(image_preview)

        ItemTouchHelper(mItemTouch).attachToRecyclerView(image_list)
    }

    val mItemTouch = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?,
                target: RecyclerView.ViewHolder?): Boolean {
            Collections.swap(mAdapter.imageList, viewHolder!!.adapterPosition,
                    target!!.adapterPosition)
            mAdapter.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition);
            return true;
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
            val pos = viewHolder!!.adapterPosition

            mAdapter.remove(pos)
        }

        override fun getMovementFlags(recyclerView: RecyclerView?,
                viewHolder: RecyclerView.ViewHolder?): Int {
            return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                    ItemTouchHelper.DOWN or ItemTouchHelper.UP or ItemTouchHelper.START or ItemTouchHelper.END);
        }

    }

    class StateAdapter(val mContext: Context, var imageList: ArrayList<String>, val itemClick: (String) -> Unit) : RecyclerView.Adapter<StateAdapter.ViewHolder>() {

        var first: View? = null

        fun remove(position: Int) {
            val item = imageList.get(position)
            imageList.remove(item)
            notifyItemRemoved(position)
        }

        override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
            with(imageList[position]) {
                Glide.with(mContext).load(this).into(holder!!.mImageView)
                holder.mImageView.setOnClickListener { itemClick.invoke(this) }
            }
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
        }
    }

}


