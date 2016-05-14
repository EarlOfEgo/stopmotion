package com.sthagios.stopmotion.list

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.sthagios.stopmotion.R
import kotlinx.android.synthetic.main.activity_image_list.*

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   30.04.16
 */
class ImageList : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_list)

        recyclerViewImageList.setHasFixedSize(true)
        recyclerViewImageList.layoutManager = LinearLayoutManager(this)

        recyclerViewImageList.adapter = ImageListAdapter()

    }
}