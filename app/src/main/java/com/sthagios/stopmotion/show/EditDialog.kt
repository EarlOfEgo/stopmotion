package com.sthagios.stopmotion.show

import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.os.Bundle
import android.text.InputType
import com.afollestad.materialdialogs.MaterialDialog

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   10.06.16
 */
class EditDialog() : DialogFragment() {


    companion object {
        val BUNDLE_VALUE = "BUNDLE_VALUE"

        fun newInstance(value: String): EditDialog {
            val fragment = EditDialog()
            val args = Bundle();
            args.putString(BUNDLE_VALUE, value);
            fragment.arguments = args
            return fragment
        }
    }

    interface Callback {
        fun onOk(name: String)
    }

    lateinit var mListener: Callback

    var mValue = ""

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is Callback) {
            mListener = context
        } else
            throw Exception("${context.toString()} must implement Callback")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        if (arguments != null) {
            mValue = arguments.getString(BUNDLE_VALUE, "");
        }

        if (savedInstanceState != null) {
            mValue = savedInstanceState.getString(BUNDLE_VALUE, "");
        }

        val dialog = MaterialDialog.Builder(activity)
                .title("Edit Gif Name")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .alwaysCallInputCallback()
                .input("R.string.input_hint", mValue, { dialog, input ->
                    mValue = input!!.toString()
                })
                .onPositive { materialDialog, dialogAction ->
                    mListener.onOk(mValue)
                }
                .show()

        return dialog
    }
}