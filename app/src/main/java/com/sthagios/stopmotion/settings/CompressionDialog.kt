package com.sthagios.stopmotion.settings

import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.os.Bundle
import com.afollestad.materialdialogs.MaterialDialog
import com.sthagios.stopmotion.R

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
        if (context is CompressionDialog.Callback) {
            mListener = context
        } else
            throw Exception("${context.toString()} must implement Callback")
    }

    lateinit var mListener: Callback

    companion object {
        val BUNDLE_VALUE = "BUNDLE_VALUE"

        fun newInstance(value: Float): CompressionDialog {
            val args = Bundle();
            when(value) {
                0.2f -> args.putInt(BUNDLE_VALUE, 0);
                0.4f -> args.putInt(BUNDLE_VALUE, 1);
                0.6f -> args.putInt(BUNDLE_VALUE, 2);
            }

            val fragment = CompressionDialog()

            fragment.arguments = args
            return fragment
        }
    }

    var mValue = 0

    val BUNDLE_VALUE = "BUNDLE_VALUE"

    override fun onSaveInstanceState(outState: Bundle?) {
        outState!!.putInt(BUNDLE_VALUE, mValue)
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