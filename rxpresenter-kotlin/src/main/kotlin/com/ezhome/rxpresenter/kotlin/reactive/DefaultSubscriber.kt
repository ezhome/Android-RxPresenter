package com.ezhome.rxpresenter.reactive

import rx.Subscriber
import timber.log.Timber

/**
 * Default subscriber base class to be used whenever you want default error handling.
 * This used for RxJava2 [rx.Observable]
 */
class DefaultSubscriber<T> : Subscriber<T>() {

    @Override
    override fun onNext(t: T) {
        // no-op by default.
    }

    @Override
    override fun onCompleted() {
        //no-op by default
    }

    @Override
    override fun onError(e: Throwable) {
        Timber.e(e, e.message)
    }
}