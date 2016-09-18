package com.sthagios.stopmotion.list

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.view.Menu
import android.view.MenuItem
import com.sthagios.stopmotion.R
import com.sthagios.stopmotion.create.CreateNewImage
import com.sthagios.stopmotion.image.database.Gif
import com.sthagios.stopmotion.image.database.getRealmInstance
import com.sthagios.stopmotion.rating.RatingActivity
import com.sthagios.stopmotion.rating.shouldShowRating
import com.sthagios.stopmotion.settings.SettingsActivity
import com.sthagios.stopmotion.utils.startActivity
import kotlinx.android.synthetic.main.activity_image_list.*
import kotlinx.android.synthetic.main.toolbar.*

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   30.04.16
 */
class ImageListActivity : AppCompatActivity() {

    private lateinit var mAdapter: ImageListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_list)

        setSupportActionBar(toolbar)

        recyclerViewImageList.setHasFixedSize(true)
        recyclerViewImageList.layoutManager = GridLayoutManager(this, 2)

        val realm = getRealmInstance()

        mAdapter = ImageListAdapter(this, realm.where(Gif::class.java).findAllAsync())

        //Use set adapter
        recyclerViewImageList.setAdapter(mAdapter, empty_image_view, empty_text_view)

        recyclerViewImageList.addItemDecoration(ItemDecorator())

        fab.setOnClickListener({ view -> createNewImage() })

        if (shouldShowRating())
            startActivity<RatingActivity>()
    }

    override fun onResume() {
        super.onResume()
        mAdapter.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 1 && data != null) {
                mAdapter.notifyDataSetChanged()
//                val id = data.getStringExtra("deleted_id")
                val name = data.getStringExtra("deleted_name")
                if (name != null && name.length > 0) {
                    Snackbar.make(recyclerViewImageList, "$name successfully deleted",
                            Snackbar.LENGTH_LONG).show()
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun createNewImage() {
        startActivity<CreateNewImage>()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater;
        inflater.inflate(R.menu.main, menu);
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.settings -> {
                startActivity<SettingsActivity>()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}