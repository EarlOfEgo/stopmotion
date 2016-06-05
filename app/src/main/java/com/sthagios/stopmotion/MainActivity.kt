package com.sthagios.stopmotion

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.sthagios.stopmotion.list.ImageList
import com.sthagios.stopmotion.utils.startActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startActivity<ImageList>()
//        startActivity<ShowGifActivity>()
    }

}

