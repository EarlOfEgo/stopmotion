package com.sthagios.stopmotion.settings.dialogs

import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.webkit.WebView
import com.afollestad.materialdialogs.MaterialDialog
import com.sthagios.stopmotion.R

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   18.06.16
 */
class LicensesDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val view = LayoutInflater.from(activity).inflate(R.layout.licencenses_web_view, null) as WebView
        view.loadUrl("file:///android_asset/open_source_licenses.html")

        val dialog = MaterialDialog.Builder(activity)
                .title(R.string.settings_licences_title)
                .customView(view, true)
                .cancelable(true)
                .positiveText(R.string.confirm_ok)
                .show();

        return dialog
    }
}