package com.ezhome.rxpresenter.reactive;

import rx.Subscriber;
import timber.log.Timber;

/**
 * Default subscriber base class to be used whenever you want default error handling.
 * This used for RxJava2 {@link rx.Observable}
 */
public class DefaultSubscriber<T> extends Subscriber<T> {

  @Override public void onNext(T t) {
    // no-op by default.
  }

  @Override public void onCompleted() {
    //no-op by default
  }

  @Override public void onError(Throwable e) {
    Timber.e(e, e.getMessage());
  }
}