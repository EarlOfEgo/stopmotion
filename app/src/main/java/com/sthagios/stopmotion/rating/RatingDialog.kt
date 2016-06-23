package com.sthagios.stopmotion.rating

import android.app.Activity
import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import com.afollestad.materialdialogs.MaterialDialog
import com.sthagios.stopmotion.R

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   20.06.16
 */
@Suppress("OverridingDeprecatedMember")
open class RatingDialog : DialogFragment() {

    interface Callback {
        fun enjoyTheApp(enjoy: Boolean)

        fun giveFeedback(feedback: Boolean)

        fun rateTheApp(rate: Boolean)

        fun canceled()
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)
        mCallback.canceled()
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (activity is RatingDialog.Callback) {
                mCallback = activity
            } else
                throw Exception("${context.toString()} must implement Callback")
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is RatingDialog.Callback) {
            mCallback = context
        } else
            throw Exception("${context.toString()} must implement Callback")
    }

    lateinit var mCallback: Callback
}

class RatingDialogFirst : RatingDialog() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog? {
        val dialog = MaterialDialog.Builder(activity)
                .content(R.string.rating_first_question_title)
                .positiveText(R.string.rating_first_question_positive)
                .onPositive { materialDialog, dialogAction ->
                    mCallback.enjoyTheApp(true)
                }
                .negativeText(R.string.rating_first_question_negative)
                .onNegative { materialDialog, dialogAction ->
                    mCallback.enjoyTheApp(false)
                }
                .cancelable(false)
                .build()

        return dialog
    }

}

class RatingDialogFeedback : RatingDialog() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog? {
        val dialog = MaterialDialog.Builder(activity)
                .content(R.string.rating_feedback_title)
                .positiveText(R.string.rating_feedback_positive)
                .onPositive { materialDialog, dialogAction ->
                    mCallback.giveFeedback(true)
                }
                .negativeText(R.string.rating_feedback_negative)
                .onNegative { materialDialog, dialogAction ->
                    mCallback.giveFeedback(false)
                }
                .cancelable(false)
                .build()

        return dialog
    }
}

class RatingDialogRate : RatingDialog() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog? {
        val dialog = MaterialDialog.Builder(activity)
                .content(R.string.rating_rate_text)
                .positiveText(R.string.rating_rate_positive)
                .onPositive { materialDialog, dialogAction ->
                    mCallback.rateTheApp(true)
                }
                .negativeText(R.string.rating_rate_negative)
                .onNegative { materialDialog, dialogAction ->
                    mCallback.rateTheApp(false)
                }
                .cancelable(false)
                .build()

        return dialog
    }
}