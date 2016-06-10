package com.sthagios.stopmotion.show

import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.NavUtils
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.bumptech.glide.Glide
import com.sthagios.stopmotion.R
import com.sthagios.stopmotion.image.database.Gif
import com.sthagios.stopmotion.image.database.getRealmInstance
import com.sthagios.stopmotion.share.shareGif
import com.sthagios.stopmotion.utils.LogDebug
import com.sthagios.stopmotion.utils.retrieveLongParameter
import kotlinx.android.synthetic.main.activity_show_gif.*
import kotlinx.android.synthetic.main.toolbar.*
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers


class ShowGifActivity : AppCompatActivity(), EditDialog.Callback {
    override fun onOk(name: String) {
        saveNewName(name)
    }

    lateinit var dialog: EditDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_gif)

        setSupportActionBar(toolbar);
        supportActionBar!!.setDisplayHomeAsUpEnabled(true);

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
                }, { })


        title = gif.name

        dialog = EditDialog.Companion.newInstance("$title")

        share_button.setOnClickListener({
            shareGif(gif.shareUriString)
        })

        edit_button.setOnClickListener({
            dialog.show(fragmentManager, "EditDialog")

        })
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
                return true;
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveNewName(name: String) {
        val id = retrieveLongParameter()
        val gif = getRealmInstance().where(Gif::class.java).equalTo("id", id).findFirst()

        val oldName = gif.name
        getRealmInstance().beginTransaction()
        gif.name = name
        getRealmInstance().commitTransaction()
        title = name
        Snackbar.make(share_button, name, Snackbar.LENGTH_LONG)
                .setAction("Undo", {
                    getRealmInstance().beginTransaction()
                    gif.name = oldName
                    getRealmInstance().commitTransaction()
                    title = oldName
                })
                .show()


    }
}
