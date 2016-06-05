package com.sthagios.stopmotion.show

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.sthagios.stopmotion.R
import com.sthagios.stopmotion.image.database.Gif
import com.sthagios.stopmotion.image.database.getRealmInstance
import com.sthagios.stopmotion.share.shareGif
import com.sthagios.stopmotion.utils.LogDebug
import com.sthagios.stopmotion.utils.retrieveLongParameter
import kotlinx.android.synthetic.main.activity_show_gif.*


class ShowGifActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_gif)

        val id = retrieveLongParameter()
        LogDebug(id.toString())

        val gif = getRealmInstance().where(Gif::class.java).equalTo("id", id).findFirst()


        val uri = Uri.parse(gif.fileUriString)

        Glide.with(this).load(uri).into(preview)


        share_button.setOnClickListener({
           shareGif(gif.shareUriString)
        })
    }
}
