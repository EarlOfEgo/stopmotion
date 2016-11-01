package com.sthagios.stopmotion.settings.dialogs

import android.app.Activity
import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.os.Build
import android.os.Bundle
import com.afollestad.materialdialogs.MaterialDialog
import com.sthagios.stopmotion.R
import com.sthagios.stopmotion.settings.COMPRESSION_HIGH
import com.sthagios.stopmotion.settings.COMPRESSION_LOW
import com.sthagios.stopmotion.settings.COMPRESSION_MEDIUM

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   14.06.16
 */
class CompressionDialog : DialogFragment() {

    interface Callback {
        fun onRateChosen(value: Int)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is Callback) {
            mListener = context
        } else
            throw Exception("${context.toString()} must implement Callback")
    }

    @Suppress("DEPRECATION")
    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (activity is Callback) {
                mListener = activity
            }
        }
    }

    lateinit var mListener: Callback

    companion object {
        val BUNDLE_VALUE = "BUNDLE_VALUE"

        fun newInstance(value: Float): CompressionDialog {
            val args = Bundle()
            when (value) {
                COMPRESSION_HIGH   -> args.putInt(BUNDLE_VALUE, 0);
                COMPRESSION_MEDIUM -> args.putInt(BUNDLE_VALUE, 1);
                COMPRESSION_LOW    -> args.putInt(BUNDLE_VALUE, 2);
            }

            val fragment = CompressionDialog()

            fragment.arguments = args
            return fragment
        }
    }

    var mValue = 0

    val BUNDLE_VALUE = "BUNDLE_VALUE"

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putInt(BUNDLE_VALUE, mValue)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        if (arguments != null) {
            mValue = arguments.getInt(BUNDLE_VALUE, 0);
        }

        if (savedInstanceState != null) {
            mValue = savedInstanceState.getInt(BUNDLE_VALUE, 0);
        }

        val dialog = MaterialDialog.Builder(activity)
                .title(R.string.settings_gif_compression_rate_title)
                .items(R.array.compression_rate)
                .itemsCallbackSingleChoice(mValue, { materialDialog, view, i, charSequence ->
                    mValue = i
                    true

                })
                .alwaysCallSingleChoiceCallback()
                .positiveText(R.string.confirm_ok)
                .onPositive({ materialDialog, dialogAction ->
                    mListener.onRateChosen(mValue)
                })
                .show();

        return dialog
    }
}