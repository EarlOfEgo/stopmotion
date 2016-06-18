package com.sthagios.stopmotion.settings.items

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.sthagios.stopmotion.R
import kotlinx.android.synthetic.main.text_preference_item.view.*

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   14.06.16
 */
class TextPreferenceItem @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
        defStyleRes: Int = 0) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    init {
        View.inflate(context, R.layout.text_preference_item, this)
        val paddingTop = context.resources.getDimension(
                R.dimen.preferences_item_padding_top).toInt()
        val paddingBottom = context.resources.getDimension(
                R.dimen.preferences_item_padding_bottom).toInt()

        setPadding(0, paddingTop, 0, paddingBottom)
        orientation = VERTICAL
    }

    fun setTitle(string: String) {
        title.text = string
    }

    fun setTitle(id: Int) {
        title.setText(id)
    }

    fun setSubtitle(string: String) {
        description.text = string
        description.visibility = View.VISIBLE
    }

    fun setSubtitle(id: Int) {
        description.setText(id)
        description.visibility = View.VISIBLE
    }
}