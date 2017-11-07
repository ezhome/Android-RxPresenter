package com.ezhome.rxpresenter;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import com.ezhome.rxpresenter.mvp.MvpView;
import com.ezhome.rxpresenter.mvp.Presenter;
import com.ezhome.rxpresenter.reactive.DefaultSubscriber;
import com.trello.navi.Event;
import com.trello.navi.NaviComponent;
import com.trello.navi.component.NaviActivity;
import com.trello.navi.component.support.NaviAppCompatActivity;
import com.trello.navi.component.support.NaviDialogFragment;
import com.trello.navi.component.support.NaviFragment;
import com.trello.navi.rx.RxNavi;
import com.trello.rxlifecycle.LifecycleProvider;
import com.trello.rxlifecycle.RxLifecycle;
import com.trello.rxlifecycle.android.ActivityEvent;
import com.trello.rxlifecycle.android.FragmentEvent;
import com.trello.rxlifecycle.navi.NaviLifecycle;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * An Rx presenter based on lifecycle
 */
public abstract class RxPresenter<V extends MvpView> implements Presenter<V> {

  private static final String LOG_TAG = RxPresenter.class.getSimpleName();

  /**
   * Internal subscriptions ofr {@link RxPresenter} to cleanup the {@link RxNavi}
   * subscriptions
   */
  private final CompositeSubscription subscriptions = new CompositeSubscription();

  /**
   * {@link NaviComponent}
   */
  private NaviComponent naviComponent;

  /**
   * {@link MvpView}
   */
  private MvpView mvpView;

  /**
   * {@link LifecycleProvider}
   */
  private LifecycleProvider lifecycleProvider;

  /**
   * The view which is bind with the presenter
   */
  protected V view;

  @Override public void bind(NaviFragment fragment) {
    Timber.tag(getClass().getSimpleName());
    this.naviComponent = fragment;
    this.initFragment();
  }

  @Override public void bind(NaviDialogFragment fragment) {
    Timber.tag(getClass().getSimpleName());
    this.naviComponent = fragment;
    this.initFragment();
  }

  @Override public void bind(NaviAppCompatActivity activity) {
    Timber.tag(getClass().getSimpleName());
    this.naviComponent = activity;
    this.initActivity();
  }

  @Override public void bind(NaviActivity activity) {
    Timber.tag(getClass().getSimpleName());
    this.naviComponent = activity;
    this.initActivity();
  }

  @Override public void resume() {
    //empty method
  }

  @Override public void pause() {
    //empty method
  }

  @Override public void destroy() {
    Timber.d("%s destroying and cleanup its references", LOG_TAG);
    this.subscriptions.clear();
    this.view = null;
  }

  /**
   * Just binds the view
   *
   * @param mpvView {@link MvpView}
   */
  public void bindView(@NonNull V mpvView) {
    this.view = mpvView;
  }

  /**
   * Executes an observable subscription based on {@link RxLifecycle}
   * {@link LifecycleProvider}
   *
   * @param observable {@link rx.Observable} given observable for subscribe
   * @param subscribers [OPTIONAL] {@link rx.Subscriber} custom subscriber
   * @param <T> any object for {@link rx.Observable}
   * @return {@link Subscription}
   */
  @SafeVarargs protected final <T> Subscription bindLifecycle(@NonNull Observable<T> observable,
      Subscriber<T>... subscribers) {
    if (subscribers.length > 1) {
      throw new IllegalArgumentException("You can pass only one Subscriber<T>");
    }
    if (subscribers.length == 0) {
      return composeLifecycle(observable, Schedulers.io()).subscribe(new DefaultSubscriber<T>());
    }
    return composeLifecycle(observable, Schedulers.io()).subscribe(subscribers[0]);
  }

  /**
   * Executes an observable subscription based on {@link RxLifecycle}
   * {@link LifecycleProvider}
   *
   * @param <T> any object for {@link rx.Observable}
   * @param subscribers [OPTIONAL] {@link rx.Subscriber} custom subscriber
   * @param scheduler the scheduler to run the stream
   * @return {@link Subscription}
   */
  @SafeVarargs protected final <T> Subscription bindLifecycle(@NonNull Observable<T> observable,
      Scheduler scheduler, Subscriber<T>... subscribers) {
    if (subscribers.length > 1) {
      throw new IllegalArgumentException("You can pass only one Subscriber<T>");
    }
    if (subscribers.length == 0) {
      return composeLifecycle(observable, scheduler).subscribe(new DefaultSubscriber<T>());
    }
    return composeLifecycle(observable, scheduler).subscribe(subscribers[0]);
  }

  /**
   * Executes an observable subscription based on {@link RxLifecycle}
   * {@link LifecycleProvider}
   *
   * @param observable {@link rx.Observable} given observable for subscribe
   * @param action [OPTIONAL] {@link rx.functions.Action1} custom action
   * @param <T> any object for {@link rx.Observable}
   * @return {@link Subscription}
   */
  protected final <T> Subscription bindLifecycle(@NonNull Observable<T> observable,
      Action1<T> action) {
    if (action == null) {
      return bindLifecycle(observable);
    }
    return composeLifecycle(observable, Schedulers.io()).subscribe(action);
  }

  /**
   * Executes an observable subscription based on {@link RxLifecycle}
   * {@link LifecycleProvider}
   *
   * @param observable {@link rx.Observable} given observable for subscribe
   * @param event {@link FragmentEvent} a fragment on {@link RxLifecycle}
   * @param subscribers [OPTIONAL] {@link rx.Subscriber} custom subscriber
   * @param <T> any object for {@link rx.Observable}
   * @return {@link Subscription}
   */
  @SafeVarargs protected final <T> Subscription bindUntilEvent(@NonNull Observable<T> observable,
      @NonNull FragmentEvent event, Subscriber<T>... subscribers) {
    if (subscribers.length > 1) {
      throw new IllegalArgumentException("You can pass only one Subscriber<T>");
    }
    if (subscribers.length == 0) {
      return composeUntilEvent(observable, event).subscribe(new DefaultSubscriber<T>());
    }
    return composeUntilEvent(observable, event).subscribe(subscribers[0]);
  }

  /**
   * Executes an observable subscription based on {@link RxLifecycle}
   * {@link LifecycleProvider}
   *
   * @param observable {@link rx.Observable} given observable for subscribe
   * @param event {@link FragmentEvent} a fragment on {@link RxLifecycle}
   * @param action [OPTIONAL] {@link rx.functions.Action1} custom action
   * @param <T> any object for {@link rx.Observable}
   * @return {@link Subscription}
   */
  protected final <T> Subscription bindUntilEvent(@NonNull Observable<T> observable,
      @NonNull FragmentEvent event, Action1<T> action) {
    if (action == null) {
      return bindUntilEvent(observable, event);
    }
    return composeUntilEvent(observable, event).subscribe(action);
  }

  /**
   * Helper methods, Composes a new {@link rx.Observable} based on a specific event
   *
   * @param observable {@link rx.Observable} given observable for subscribe
   * @param event {@link FragmentEvent} the event which must stop emitting items
   * @param <T> any object for {@link rx.Observable}
   * @return {@link rx.Observable}
   */
  @SuppressWarnings("unchecked")
  private <T> Observable<T> composeUntilEvent(@NonNull Observable<T> observable,
      @NonNull FragmentEvent event) {
    return observable.doOnUnsubscribe(loggingUnsub)
        .compose(lifecycleProvider.<T>bindUntilEvent(event));
  }

  /**
   * Helper methods, Composes a new {@link rx.Observable} based on a specific event
   *
   * @param observable {@link rx.Observable} given observable for subscribe
   * @param <T> any object for {@link rx.Observable}
   * @return {@link rx.Observable}
   */
  @SuppressWarnings("unchecked")
  private <T> Observable<T> composeLifecycle(@NonNull Observable<T> observable, Scheduler scheduler) {
    if (naviComponent instanceof Fragment) {
      return observable.doOnUnsubscribe(loggingUnsub)
          .compose(applySchedulers(scheduler))
          .compose(lifecycleProvider.<T>bindUntilEvent(FragmentEvent.DESTROY_VIEW));
    } else {
      return observable.doOnUnsubscribe(loggingUnsub)
          .compose(applySchedulers(scheduler))
          .compose(lifecycleProvider.<T>bindUntilEvent(ActivityEvent.DESTROY));
    }
  }

  /**
   * We are using RxJava transformers to compose the observables in order
   * to not break the chain and help in the UI to do process and avoid
   * lagging
   */
  private <T> Observable.Transformer<T, T> applySchedulers(final Scheduler scheduler) {
    return new Observable.Transformer<T, T>() {
      @Override public Observable<T> call(Observable<T> observable) {
        return observable.subscribeOn(scheduler)
            .observeOn(AndroidSchedulers.mainThread());
      }
    };
  }

  /**
   * Used to follow the lifecycle and bind the view
   */
  @SuppressWarnings("unchecked") private void initFragment() {
    this.mvpView = (MvpView) naviComponent;
    this.lifecycleProvider = NaviLifecycle.createFragmentLifecycleProvider(naviComponent);
    this.initCommon(Event.VIEW_CREATED);
  }

  /**
   * Used to follow the lifecycle and bind the view
   */
  @SuppressWarnings("unchecked") private void initActivity() {
    this.mvpView = (MvpView) naviComponent;
    this.lifecycleProvider = NaviLifecycle.createActivityLifecycleProvider(naviComponent);
    this.initCommon(Event.START);
  }

  /**
   * Standard common lifecycle
   */
  @SuppressWarnings("unchecked")
  private <T> void initCommon(Event<T> event) {
    this.subscriptions.add(
        RxNavi.observe(naviComponent, event).subscribe(new Action1<T>() {
          @Override public void call(T t) {
            RxPresenter.this.bindView((V) mvpView);
          }
        }));
    this.subscriptions.add(
        RxNavi.observe(naviComponent, Event.RESUME).subscribe(new Action1<Void>() {
          @Override public void call(Void aVoid) {
            RxPresenter.this.resume();
          }
        }));
    this.subscriptions.add(
        RxNavi.observe(naviComponent, Event.PAUSE).subscribe(new Action1<Void>() {
          @Override public void call(Void aVoid) {
            RxPresenter.this.pause();
          }
        }));
    this.subscriptions.add(
        RxNavi.observe(naviComponent, Event.DESTROY).subscribe(new Action1<Object>() {
          @Override public void call(final Object object) {
            RxPresenter.this.destroy();
          }
        }));
  }

  /**
   * Logs the un-subscription of a sequence
   */
  private final Action0 loggingUnsub = new Action0() {
    @Override public void call() {
      Timber.d("Sequence un-subscribed");
    }
  };
}