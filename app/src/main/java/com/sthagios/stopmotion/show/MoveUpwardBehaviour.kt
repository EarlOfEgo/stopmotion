package com.sthagios.stopmotion.show

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.util.AttributeSet
import android.view.View

/**
 * Stopmotion

 * @author stephan
 * *
 * @since 11.06.16
 */
class MoveUpwardBehaviour : CoordinatorLayout.Behavior<View> {

    constructor() : super() {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    }

    override fun layoutDependsOn(parent: CoordinatorLayout?, child: View?,
            dependency: View?): Boolean {
        return dependency is Snackbar.SnackbarLayout
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout?, child: View?,
            dependency: View?): Boolean {
        val translationY = Math.min(0f, dependency!!.translationY - dependency.height)
        child!!.translationY = translationY
        return true
    }
}
