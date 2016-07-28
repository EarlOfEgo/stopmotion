package com.sthagios.stopmotion.create.edit

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.Menu
import android.view.MenuItem
import com.bumptech.glide.Glide
import com.sthagios.stopmotion.R
import com.sthagios.stopmotion.create.CreateNewImage
import com.sthagios.stopmotion.create.GenerateGifActivity
import com.sthagios.stopmotion.tracking.logEditEvent
import com.sthagios.stopmotion.utils.retrieveStringListParameter
import com.sthagios.stopmotion.utils.startActivity
import com.sthagios.stopmotion.utils.startActivityForResultWithArgument
import kotlinx.android.synthetic.main.activity_edit_images.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.*

class EditImagesActivity : AppCompatActivity() {

    private lateinit var mPictureList: ArrayList<String>

    private lateinit var mAdapter: StateAdapter

    private val sSavedStateKey = "SAVED_STATE_IMAGE_LIST"

    private var mUndoPreviewLoadLast: Boolean = false

    private val sNewImageTake: Int = 1

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.edit, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.save -> {
                if (mAdapter.itemCount <= 2) {
                    Snackbar.make(image_preview, R.string.snackbar_image_required,
                            Snackbar.LENGTH_LONG)
                            .setAction(R.string.snackbar_take_picture_action, {
                                startActivityForResultWithArgument<CreateNewImage>(true,
                                        sNewImageTake)
                            })
                            .show()
                } else {
                    startActivity<GenerateGifActivity>(mAdapter.imageList)
                    finish()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_images)

        setSupportActionBar(toolbar)
        title = null

        mPictureList = retrieveStringListParameter()

        if (savedInstanceState != null) {
            mPictureList = savedInstanceState.getStringArrayList(sSavedStateKey)
        }

        image_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        mAdapter = StateAdapter(this, mPictureList,
                {
                    logEditEvent("delete_image", "DELETE")
                    Glide.with(this).load(it).into(image_preview)
                },
                {
                    startActivityForResultWithArgument<CreateNewImage>(true, sNewImageTake)
                })

        image_list.adapter = mAdapter
        if (mAdapter.itemCount > 1)
            Glide.with(this).load(mAdapter.imageList[0]).into(image_preview)

        val itemTouch = ItemTouchHelperCallback(mAdapter, {
            val itemId = mAdapter.getItemId(it)
            showUndo(mAdapter.remove(it), it)
            if (mAdapter.mSelectedItemId == itemId) {
                val newPos = switchPreviewImage(it)
                if (newPos != -1) {
                    mAdapter.mSelectedItemId = mAdapter.getItemId(newPos)
                }
                mUndoPreviewLoadLast = true
            }
        })

        ItemTouchHelper(itemTouch).attachToRecyclerView(image_list)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK && requestCode == sNewImageTake && data != null) {
            logEditEvent("add_image", "IMAGE_TAKEN")
            mAdapter.appendItem(data.getStringExtra("param_result"))
            if (mAdapter.itemCount == 2)
                Glide.with(this).load(mAdapter.imageList[0]).into(image_preview)

        }
        super.onActivityResult(requestCode, resultCode, data)
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

        if (newPos >= 0)
            Glide.with(this).load(mAdapter.imageList[newPos]).into(image_preview)
        else {
            image_preview.setImageResource(R.drawable.stopmotion_icon_bw)
        }
        return newPos
    }

    private fun showUndo(remove: String, pos: Int) {
        Snackbar.make(image_preview, "Image deleted", Snackbar.LENGTH_LONG)
                .setAction(R.string.snackbar_undo_action_text, {
                    logEditEvent("delete_image", "UNDO")
                    mAdapter.addItem(pos, remove)
                    if (mUndoPreviewLoadLast) {
                        Glide.with(this).load(remove).into(image_preview)
                        mUndoPreviewLoadLast = false
                    }
                })
                .show()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState!!.putStringArrayList(sSavedStateKey, mPictureList)
    }

}


