package com.sthagios.stopmotion.show

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.bumptech.glide.Glide
import com.sthagios.stopmotion.R
import com.sthagios.stopmotion.image.database.Gif
import com.sthagios.stopmotion.image.database.getRealmInstance
import com.sthagios.stopmotion.share.shareGif
import com.sthagios.stopmotion.utils.LogDebug
import com.sthagios.stopmotion.utils.retrieveLongParameter
import kotlinx.android.synthetic.main.activity_show_gif.*
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers


class ShowGifActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_gif)

        val id = retrieveLongParameter()
        LogDebug(id.toString())

        val gif = getRealmInstance().where(Gif::class.java).equalTo("id", id).findFirst()

        val uri = Uri.parse(gif.fileUriString)
        val uriThumb = Uri.parse(gif.thumbnailUriString)

        Observable.just(Glide.with(this).load(uriThumb).into(preview))
//                .subscribeOn(Schedulers.computation())
                .flatMap {
                    LogDebug("Loaded, loading gif")
                    Observable.just(Glide.with(this).load(uri).into(preview_gif))
                            .subscribeOn(Schedulers.computation())
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, {
                    e ->
                    e.printStackTrace()
                }, { loading_spinner.visibility = View.GONE })


        share_button.setOnClickListener({
            shareGif(gif.shareUriString)
        })
    }
}
