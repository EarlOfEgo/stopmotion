package com.sthagios.stopmotion.create.edit

import android.content.Context
import android.graphics.Canvas
import android.os.Bundle
import android.support.design.widget.Snackbar
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
import kotlinx.android.synthetic.main.image_list_item_new.view.*
import java.util.*

class EditImagesActivity : AppCompatActivity() {

    private lateinit var mPictureList: ArrayList<String>

    private lateinit var mAdapter: StateAdapter

    private val sSavedStateKey = "SAVED_STATE_IMAGE_LIST"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_images)

        mPictureList = retrieveStringListParameter()

        if(savedInstanceState != null) {
            mPictureList = savedInstanceState.getStringArrayList(sSavedStateKey)
        }

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

            showUndo(mAdapter.remove(pos), pos)
        }

        override fun getMovementFlags(recyclerView: RecyclerView?,
                viewHolder: RecyclerView.ViewHolder?): Int {
            return makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE,
                    ItemTouchHelper.DOWN or ItemTouchHelper.UP) or makeFlag(
                    ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.START or ItemTouchHelper.END)
        }

        override fun clearView(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?) {
            getDefaultUIUtil().clearView((viewHolder as StateAdapter.ViewHolder).mCardView);
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            if (viewHolder != null && actionState == ItemTouchHelper.ACTION_STATE_SWIPE)
                getDefaultUIUtil().onSelected((viewHolder as StateAdapter.ViewHolder).mCardView)
            else
                super.onSelectedChanged(viewHolder, actionState)
        }

        override fun onChildDraw(c: Canvas?, recyclerView: RecyclerView?,
                viewHolder: RecyclerView.ViewHolder?, dX: Float, dY: Float, actionState: Int,
                isCurrentlyActive: Boolean) {
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE)
                getDefaultUIUtil().onDraw(c, recyclerView,
                        (viewHolder as StateAdapter.ViewHolder).mCardView, dX, dY, actionState,
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
                        (viewHolder as StateAdapter.ViewHolder).mCardView, dX, dY, actionState,
                        isCurrentlyActive)
            else
                super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState,
                        isCurrentlyActive)
        }

    }

    private fun showUndo(remove: String, pos: Int) {
        Snackbar.make(image_preview, "Image deleted", Snackbar.LENGTH_LONG)
                .setAction(R.string.snackbar_undo_action_text, { mAdapter.addItem(pos, remove) })
                .show()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState!!.putStringArrayList(sSavedStateKey, mPictureList)
    }

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
            var mCardView = itemView!!.card_view
        }

        fun addItem(pos: Int, toAdd: String) {
            imageList.add(pos, toAdd)
            notifyItemInserted(pos)
        }
    }

}


