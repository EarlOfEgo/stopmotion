package com.sthagios.stopmotion.show

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Dialog
import android.app.DialogFragment
import android.content.Intent
import android.graphics.drawable.Animatable
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.transition.Transition
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.sthagios.stopmotion.R
import com.sthagios.stopmotion.image.database.Gif
import com.sthagios.stopmotion.image.database.getRealmInstance
import com.sthagios.stopmotion.share.shareGif
import com.sthagios.stopmotion.utils.LogDebug
import com.sthagios.stopmotion.utils.LogError
import com.sthagios.stopmotion.utils.getApproximateAppStarts
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

    private var mOffset1: Float = 0f
    private var mOffset2: Float = 0f
    private var mOffset3: Float = 0f

    private lateinit var mGifUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_gif)

        setSupportActionBar(toolbar);

        for (i in 0..toolbar.childCount) {
            val view = toolbar.getChildAt(i)
            if (view is TextView) {
                ViewCompat.setTransitionName(view, "shared_text");
            }
        }

        supportActionBar!!.setDisplayHomeAsUpEnabled(true);

        val id = retrieveLongParameter()
        LogDebug(id.toString())

        val gif = getRealmInstance().where(Gif::class.java).equalTo("id", id).findFirst()

        mGifUri = Uri.parse(gif.fileUriString)
        val uriThumb = Uri.parse(gif.thumbnailUriString)

        if (getApproximateAppStarts() < 4) {
            Snackbar.make(preview_gif, R.string.snackbar_info_loading_time,
                    Snackbar.LENGTH_LONG).show()
        }

        Glide.with(this).load(uriThumb).into(preview)

        doOnTransitionFinished()

        title = gif.name

        dialog = EditDialog.Companion.newInstance("$title")

        expand_button.setOnClickListener({
            if (mFabExpanded) {
                collapseFab()
                mFabExpanded = false
            } else {
                expandFab()
                mFabExpanded = true
            }
        })

        share_button.setOnClickListener({
            shareGif(gif.shareUriString, gif.name)
        })

        edit_button.setOnClickListener({
            dialog.show(fragmentManager, "EditDialog")

        })

        delete_button.setOnClickListener({
            val dialog = object : DialogFragment() {
                override fun onCreateDialog(savedInstanceState: Bundle?): Dialog? {

                    val dialog = MaterialDialog.Builder(activity)
                            .title(R.string.dialog_delete_title)
                            .positiveText(R.string.delete_button)
                            .negativeText(R.string.cancel_button)
                            .onPositive { materialDialog, dialogAction ->
                                deleteGif()
                            }
                            .show()
                    return dialog
                }

            }
            dialog.show(fragmentManager, "DeleteDialog")
        })

        fab_container.viewTreeObserver.addOnPreDrawListener(
                object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        fab_container.viewTreeObserver.removeOnPreDrawListener(this);
                        mOffset1 = expand_button.y - share_button.y;
                        share_button.translationY = mOffset1;
                        mOffset2 = expand_button.y - edit_button.y;
                        edit_button.translationY = mOffset2;
                        mOffset3 = expand_button.y - delete_button.y;
                        delete_button.translationY = mOffset3;
                        return true
                    }
                });
    }

    private fun doOnTransitionFinished() {

        if (window.sharedElementEnterTransition != null) {
            window.sharedElementEnterTransition.addListener(object : Transition.TransitionListener {
                override fun onTransitionEnd(transition: Transition?) {
                    loadGif()
                    window.sharedElementEnterTransition.removeListener(this)
                }

                override fun onTransitionResume(transition: Transition?) {
                }

                override fun onTransitionPause(transition: Transition?) {
                }

                override fun onTransitionCancel(transition: Transition?) {
                }

                override fun onTransitionStart(transition: Transition?) {
                }

            })
        } else {
            loadGif()
        }
    }

    private fun loadGif() {
        Observable.just(Glide.with(baseContext).load(mGifUri).into(preview_gif))
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, {
                    e ->
                    e.printStackTrace()
                }, { })
    }

    private fun expandFab() {
        expand_button.setImageResource(R.drawable.animated_plus_to_x);
        val animatorSet = AnimatorSet();
        animatorSet.playTogether(createExpandAnimator(share_button, mOffset1),
                createExpandAnimator(edit_button, mOffset2),
                createExpandAnimator(delete_button, mOffset3));
        animatorSet.start();
        animateFab();
    }

    private fun animateFab() {
        val drawable = expand_button.drawable
        if (drawable is Animatable) {
            drawable.start()
        }
    }

    private fun createExpandAnimator(view: View, offset: Float): Animator {
        return ObjectAnimator.ofFloat(view, "translationY", offset, 0f)
                .setDuration(
                        resources.getInteger(android.R.integer.config_mediumAnimTime).toLong());
    }

    private fun collapseFab() {
        expand_button.setImageResource(R.drawable.animated_x_to_plus);
        val animatorSet = AnimatorSet();
        animatorSet.playTogether(createCollapseAnimator(share_button, mOffset1),
                createCollapseAnimator(edit_button, mOffset2),
                createCollapseAnimator(delete_button, mOffset3));
        animatorSet.start();
        animateFab();
    }

    private fun createCollapseAnimator(view: View, offset: Float): Animator {
        return ObjectAnimator.ofFloat(view, "translationY", 0f, offset)
                .setDuration(
                        resources.getInteger(android.R.integer.config_mediumAnimTime).toLong());
    }

    var mFabExpanded = false

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
                supportFinishAfterTransition()
//                NavUtils.navigateUpFromSameTask(this)
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
        Snackbar.make(share_button, getString(R.string.snackbar_renamed_gif, oldName, name),
                Snackbar.LENGTH_LONG)
                .setAction(R.string.snackbar_undo_action_text, {
                    getRealmInstance().executeTransaction {
                        gif.name = oldName
                        title = oldName
                    }
                })
                .show()
    }

}
