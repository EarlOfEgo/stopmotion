package com.sthagios.stopmotion.list

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
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
        recyclerViewImageList.layoutManager = GridLayoutManager(this, 2)

        val adapter = ImageListAdapter(this)

        adapter.addItem("LALALA")
        adapter.addItem("LOLOLO")
        adapter.addItem("LOLOLO")
        adapter.addItem("LOLOLO")
        adapter.addItem("LOLOLO")
        adapter.addItem("LOLOLO")
        adapter.addItem("LOLOLO")
        adapter.addItem("LOLOLO")
        adapter.addItem("LOLOLO")
        adapter.addItem("LOLOLO")
        adapter.addItem("LOLOLO")
        adapter.addItem("LOLOLO")
        adapter.addItem("LOLOLO")
        adapter.addItem("LOLOLO")
        adapter.addItem("LOLOLO")
        adapter.addItem("LOLOLO")
        adapter.addItem("LOLOLO")

        recyclerViewImageList.adapter = adapter


        recyclerViewImageList.addItemDecoration(ItemDecorator())

    }
}