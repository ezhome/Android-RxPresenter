package com.ezhome.rxpresenter.reactive;

import rx.Observable;
import rx.Scheduler;

/**
 * Interface for transformer used in RxPresenter
 * This used for RxJava2 {@link rx.Observable}
 */
public interface RxPresenterTransformer {
  <T> Observable.Transformer<T, T> subscribeOn(Scheduler scheduler);
  <T> Observable.Transformer<T, T> observeOnMain();
}