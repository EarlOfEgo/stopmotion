package com.sthagios.stopmotion.base

import rx.Subscription
import rx.subscriptions.CompositeSubscription

/**
 * Stopmotion

 * @author stephan
 * *
 * @since 05.09.16
 */
abstract class AbstractPresenter<V : AbstractView> {

    protected var mPresenterSubscriptions = CompositeSubscription()

    protected var mView: V? = null

    fun attachView(view: V) {
        mView = view
    }

    fun detachView() {
        mView = null
        if (mPresenterSubscriptions.hasSubscriptions()) {
            mPresenterSubscriptions.clear()
        }
    }

    fun hasView(): Boolean {
        return mView != null
    }

    abstract fun onStart()

    protected fun subscribe(subscription: Subscription) {
        mPresenterSubscriptions.add(subscription)
    }

}
