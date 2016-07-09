package com.sthagios.stopmotion.create.edit

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import com.bumptech.glide.Glide
import com.sthagios.stopmotion.R
import com.sthagios.stopmotion.utils.retrieveStringListParameter
import kotlinx.android.synthetic.main.activity_edit_images.*
import java.util.*

class EditImagesActivity : AppCompatActivity() {

    private lateinit var mPictureList: ArrayList<String>

    private lateinit var mAdapter: StateAdapter

    private val sSavedStateKey = "SAVED_STATE_IMAGE_LIST"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_images)

        mPictureList = retrieveStringListParameter()

        if (savedInstanceState != null) {
            mPictureList = savedInstanceState.getStringArrayList(sSavedStateKey)
        }

        image_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        mAdapter = StateAdapter(this, mPictureList,
                { Glide.with(this).load(it).into(image_preview) })

        image_list.adapter = mAdapter
        Glide.with(this).load(mAdapter.imageList[0]).into(image_preview)

        val itemTouch = ItemTouchHelperCallback(mAdapter, {
            val itemId = mAdapter.getItemId(it)
            showUndo(mAdapter.remove(it), it)
            if (mAdapter.mSelectedItemId == itemId) {
                val newPos = switchPreviewImage(it)
                if (newPos != -1)
                    mAdapter.mSelectedItemId = mAdapter.getItemId(newPos)
            }
        })

        ItemTouchHelper(itemTouch).attachToRecyclerView(image_list)
    }

    private fun switchPreviewImage(pos: Int): Int {
        var newPos = -1
        val size = mAdapter.imageList.size
        //first image selected
        if (pos == 0) {
            if (size > 0) {
                newPos = 0
            }
            //last image selected
        } else if (pos == size - 1) {
            if (pos > 0) {
                newPos = pos - 1
            }
            //somewhere in the middle
        } else {
            newPos = pos - 1
        }

        if (newPos != -1)
            Glide.with(this).load(mAdapter.imageList[newPos]).into(image_preview)
        else {
            //TODO empty view
            Glide.with(this).load(R.drawable.abc_ab_share_pack_mtrl_alpha).into(image_preview)
        }
        return newPos
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

}


