package com.ezhome.rxpresenter.reactive;

import rx.Observable;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Default transformer that subscribes on given scheduler and observes on Android main thread.
 * This used for RxJava2 {@link rx.Observable}
 */
public class RxDefaultTransformer implements RxPresenterTransformer {

   @Override public  <T> Observable.Transformer<T, T> subscribeOn(Scheduler scheduler) {
    return new Observable.Transformer<T, T>() {
      @Override
      public Observable<T> call(Observable<T> observable) {
        return observable.subscribeOn(scheduler);
      }
    };
  }

  @Override public  <T> Observable.Transformer<T, T> observeOnMain() {
    return new Observable.Transformer<T, T>() {
      @Override
      public Observable<T> call(Observable<T> observable) {
        return observable.observeOn(AndroidSchedulers.mainThread());
      }
    };
  }
}