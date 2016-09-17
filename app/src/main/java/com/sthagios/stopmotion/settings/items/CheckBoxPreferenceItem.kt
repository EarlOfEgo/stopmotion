package com.sthagios.stopmotion.settings.items

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.sthagios.stopmotion.R
import kotlinx.android.synthetic.main.checkbox_preference_item.view.*

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   08.06.16
 */
class CheckBoxPreferenceItem @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
        defStyleRes: Int = 0) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    init {
        View.inflate(context, R.layout.checkbox_preference_item, this)
        val array = context.theme.obtainStyledAttributes(attrs, R.styleable.CheckBoxPreferenceItem,
                0, 0)

        try {
            val title = array.getString(R.styleable.CheckBoxPreferenceItem_title)
            val subTitle = array.getString(R.styleable.CheckBoxPreferenceItem_subTitle)

            if (title != null) setTitle(title)
            if (subTitle != null) setSubtitle(subTitle)

        } finally {
            array.recycle()
        }
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

    fun setChecked(boolean: Boolean) {
        checkBox.isChecked = boolean
    }

    fun onCheckChanged(function: (Boolean) -> Unit) {
        checkBox.setOnCheckedChangeListener { compoundButton, b -> function(b) }
    }

    fun onClickListener(function: () -> Unit) {
        checkBox.setOnClickListener {
            function()
        }
        setOnClickListener {
            function()
        }
    }
}
