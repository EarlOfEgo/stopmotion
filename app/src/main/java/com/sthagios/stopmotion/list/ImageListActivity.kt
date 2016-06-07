package com.sthagios.stopmotion.list

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.view.Menu
import android.view.MenuItem
import com.sthagios.stopmotion.R
import com.sthagios.stopmotion.create.CreateNewImage
import com.sthagios.stopmotion.image.database.Gif
import com.sthagios.stopmotion.image.database.getRealmInstance
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_list)

        setSupportActionBar(toolbar)


        recyclerViewImageList.setHasFixedSize(true)
        recyclerViewImageList.layoutManager = GridLayoutManager(this, 2)

        val realm = getRealmInstance()

        val adapter = ImageListAdapter(this, realm.where(Gif::class.java).findAllAsync())

        recyclerViewImageList.adapter = adapter

        recyclerViewImageList.addItemDecoration(ItemDecorator())

        fab.setOnClickListener({ view -> createNewImage() })

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
        when (item!!.itemId) {
            R.id.settings -> {
                startActivity<SettingsActivity>()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}