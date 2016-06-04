package com.sthagios.stopmotion.create

import android.app.Activity
import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.os.Build
import android.os.Bundle
import com.afollestad.materialdialogs.MaterialDialog
import com.sthagios.stopmotion.R

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   28.05.16
 */
open class AbstractDialog() : DialogFragment() {

    interface Callback {
        fun timeChosen(time: Int)

        fun amountChosen(amount: Int)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is Callback) {
            mListener = context
        } else
            throw Exception("${context.toString()} must implement Callback")
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (activity is Callback) {
                mListener = activity
            } else
                throw Exception("${context.toString()} must implement Callback")
        }
    }

    lateinit var mListener: Callback

    var mValue = 0

    val BUNDLE_VALUE = "BUNDLE_VALUE"

    override fun onSaveInstanceState(outState: Bundle?) {
        outState!!.putInt(BUNDLE_VALUE, mValue)
        super.onSaveInstanceState(outState)
    }
}

class BurstTimeDialog : AbstractDialog() {
    companion object {
        val BUNDLE_VALUE = "BUNDLE_VALUE"

        fun newInstance(value: Int): BurstTimeDialog {
            val fragment = BurstTimeDialog()
            val args = Bundle();
            args.putInt(BUNDLE_VALUE, value);
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        if (arguments != null) {
            mValue = arguments.getInt(BUNDLE_VALUE, 0);
        }

        if (savedInstanceState != null) {
            mValue = savedInstanceState.getInt(BUNDLE_VALUE, 0);
        }

        val dialog = MaterialDialog.Builder(activity)
                .title("Choose Burst Interval")
                .items(R.array.burst_times)
                .itemsCallbackSingleChoice(mValue, { materialDialog, view, i, charSequence ->
                    mValue = i
                    true

                })
                .alwaysCallSingleChoiceCallback()
                .positiveText("ok")
                .onPositive({ materialDialog, dialogAction -> mListener.timeChosen(mValue) })
                .show();

        return dialog
    }
}

class BurstAmountDialog : AbstractDialog() {
    companion object {
        val BUNDLE_VALUE = "BUNDLE_VALUE"

        fun newInstance(value: Int): BurstAmountDialog {
            val fragment = BurstAmountDialog()
            val args = Bundle();
            args.putInt(BUNDLE_VALUE, value);
            fragment.arguments = args
            return fragment
        }
    }

    val mValues: List<Int> = arrayListOf(3, 4, 5, 6, 7)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        if (arguments != null) {
            mValue = mValues.indexOf(arguments.getInt(BUNDLE_VALUE, 3))
        }

        if (savedInstanceState != null) {
            mValue = savedInstanceState.getInt(BUNDLE_VALUE, 0);
        }

        val dialog = MaterialDialog.Builder(activity)
                .title("Choose Burst Amount")
                .items(mValues)
                .itemsCallbackSingleChoice(mValue, { materialDialog, view, i, charSequence ->
                    mValue = i
                    true

                })
                .alwaysCallSingleChoiceCallback()
                .positiveText("ok")
                .onPositive(
                        { materialDialog, dialogAction -> mListener.amountChosen(mValues[mValue]) })
                .show();

        return dialog
    }
}