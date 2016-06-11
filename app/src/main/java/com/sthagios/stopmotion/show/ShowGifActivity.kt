package com.sthagios.stopmotion.show

import android.app.Dialog
import android.app.DialogFragment
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.NavUtils
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.sthagios.stopmotion.R
import com.sthagios.stopmotion.image.database.Gif
import com.sthagios.stopmotion.image.database.getRealmInstance
import com.sthagios.stopmotion.share.shareGif
import com.sthagios.stopmotion.utils.LogDebug
import com.sthagios.stopmotion.utils.LogError
import com.sthagios.stopmotion.utils.retrieveLongParameter
import kotlinx.android.synthetic.main.activity_show_gif.*
import kotlinx.android.synthetic.main.toolbar.*
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File


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

        delete_button.setOnClickListener({
            val dialog = object : DialogFragment() {
                override fun onCreateDialog(savedInstanceState: Bundle?): Dialog? {

                    val dialog = MaterialDialog.Builder(activity)
                            .title("Are you sure?")
                            .positiveText("Delete")
                            .negativeText("Cancel")
                            .onPositive { materialDialog, dialogAction ->
                                deleteGif()
                            }
                            .show()
                    return dialog
                }

            }
            dialog.show(fragmentManager, "DeleteDialog")
        })
    }

    private fun deleteGif() {
        val id = retrieveLongParameter()
        val gif = getRealmInstance().where(Gif::class.java).equalTo("id", id).findFirst()
        val gifUri = Uri.parse(gif.fileUriString)
        val thumbUri = Uri.parse(gif.thumbnailUriString)
        deleteFile(gifUri)
        deleteFile(thumbUri)

        getRealmInstance().executeTransaction {

            val bundle = Bundle();
            bundle.putString("deleted_name", gif.name);

            val intent = Intent()
            intent.putExtras(bundle)
            setResult(RESULT_OK, intent);
            gif.deleteFromRealm()
            finish()
        }
    }

    private fun deleteFile(gifUri: Uri?) {
        LogDebug("Deleting $gifUri")
        val file = File(gifUri!!.path)
        Observable.just(file)
                .subscribe({ it.delete() }, {
                    e ->
                    LogError("$e")
                }, {
                    LogDebug("Deleted")
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
        getRealmInstance().executeTransaction {
            gif.name = name
        }
        title = name
        Snackbar.make(share_button, name, Snackbar.LENGTH_LONG)
                .setAction("Undo", {
                    getRealmInstance().executeTransaction {
                        gif.name = oldName
                        title = oldName
                    }
                })
                .show()
    }
}
