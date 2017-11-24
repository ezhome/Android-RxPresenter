package com.ezhome.rxpresenter

import android.support.v4.app.Fragment
import com.ezhome.rxpresenter.mvp.MvpView
import com.ezhome.rxpresenter.mvp.Presenter
import com.ezhome.rxpresenter.reactive.DefaultSubscriber
import com.trello.navi.Event
import com.trello.navi.NaviComponent
import com.trello.navi.component.NaviActivity
import com.trello.navi.component.support.NaviAppCompatActivity
import com.trello.navi.component.support.NaviDialogFragment
import com.trello.navi.component.support.NaviFragment
import com.trello.navi.rx.RxNavi
import com.trello.rxlifecycle.LifecycleProvider
import com.trello.rxlifecycle.RxLifecycle
import com.trello.rxlifecycle.android.ActivityEvent
import com.trello.rxlifecycle.android.FragmentEvent
import com.trello.rxlifecycle.navi.NaviLifecycle
import rx.Observable
import rx.Scheduler
import rx.Subscriber
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action0
import rx.functions.Action1
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import timber.log.Timber

const val LOG_TAG: String = "RxPresenter"

/**
 * An Rx presenter based on lifecycle
 */
abstract class RxPresenter<V : MvpView> : Presenter<V> {

    /**
     * Internal subscriptions ofr [RxPresenter] to cleanup the [RxNavi]
     * subscriptions
     */
    private val subscriptions = CompositeSubscription()

    /**
     * [NaviComponent]
     */
    private lateinit var naviComponent: NaviComponent

    /**
     * [MvpView]
     */
    private var mvpView: MvpView? = null

    /**
     * [LifecycleProvider]
     */
    private lateinit var lifecycleProvider: LifecycleProvider<out Any>

    /**
     * The view which is bind with the presenter
     */
    internal var view: V? = null

    /**
     * Logs the un-subscription of a sequence
     */
    private val loggingUnsub = Action0 { Timber.d("Sequence un-subscribed") }

    override fun bind(fragment: NaviFragment) {
        this.naviComponent = fragment
        this.initFragment()
    }

    override fun bind(fragment: NaviDialogFragment) {
        this.naviComponent = fragment
        this.initFragment()
    }

    override fun bind(activity: NaviAppCompatActivity) {
        this.naviComponent = activity
        this.initActivity()
    }

    override fun bind(activity: NaviActivity) {
        this.naviComponent = activity
        this.initActivity()
    }

    override fun resume() {
        //empty method
    }

    override fun pause() {
        //empty method
    }

    override fun destroy() {
        Timber.d("%s destroying and cleanup its references", LOG_TAG)
        this.subscriptions.clear()
        this.view = null
    }

    /**
     * Just binds the view
     *
     * @param mpvView [MvpView]
     */
    fun bindView(mpvView: V) {
        Timber.tag(javaClass.simpleName)
        this.view = mpvView
    }

    /**
     * Executes an observable subscription based on [RxLifecycle]
     * [LifecycleProvider]
     *
     * @param observable [rx.Observable] given observable for subscribe
     * @param scheduler the scheduler to run the stream
     * @param subscriber [Subscriber] [rx.Subscriber] custom subscriber
     * @param <T> any object for [rx.Observable]
     * @return [Subscription]
    </T> */
    internal fun <T> bindLifecycle(observable: Observable<T>,
                                   scheduler: Scheduler = Schedulers.io(),
                                   subscriber: Subscriber<T> = DefaultSubscriber()): Subscription =
        composeLifecycle(observable, scheduler).subscribe(subscriber)

    /**
     * Executes an observable subscription based on [RxLifecycle]
     * [LifecycleProvider]
     *
     * @param observable [rx.Observable] given observable for subscribe
     * @param action [OPTIONAL] [rx.functions.Action1] custom action
     * @param <T> any object for [rx.Observable]
     * @return [Subscription]
    </T> */
    internal fun <T> bindLifecycle(observable: Observable<T>,
                                   scheduler: Scheduler = Schedulers.io(),
                                   action: Action1<T>?): Subscription {
        action?.let {
            return composeLifecycle(observable, scheduler).subscribe(action)
        }.run {
            return bindLifecycle(observable, scheduler)
        }
    }

    /**
     * Executes an observable subscription based on [RxLifecycle]
     * [LifecycleProvider]
     *
     * @param observable [rx.Observable] given observable for subscribe
     * @param event [FragmentEvent] a fragment on [RxLifecycle]
     * @param subscriber [OPTIONAL] [rx.Subscriber] custom subscriber
     * @param <T> any object for [rx.Observable]
     * @return [Subscription]
    </T> */
    internal fun <T> bindUntilEvent(observable: Observable<T>,
                                    event: FragmentEvent,
                                    subscriber: Subscriber<T> = DefaultSubscriber()): Subscription =
        composeUntilEvent(observable, event).subscribe(subscriber)

    /**
     * Executes an observable subscription based on [RxLifecycle]
     * [LifecycleProvider]
     *
     * @param observable [rx.Observable] given observable for subscribe
     * @param event [FragmentEvent] a fragment on [RxLifecycle]
     * @param action [OPTIONAL] [rx.functions.Action1] custom action
     * @param <T> any object for [rx.Observable]
     * @return [Subscription]
    </T> */
    internal fun <T> bindUntilEvent(observable: Observable<T>,
                                    event: FragmentEvent, action: Action1<T>?): Subscription {
        action?.let {
            return composeUntilEvent(observable, event).subscribe(action)
        }.run {
            return bindUntilEvent(observable, event)
        }
    }

    /**
     * Executes an observable subscription based on [RxLifecycle]
     * [LifecycleProvider]
     *
     * @param observable [rx.Observable] given observable for subscribe
     * @param event [ActivityEvent] an activity evetny on [RxLifecycle]
     * @param subscriber [OPTIONAL] [rx.Subscriber] custom subscriber
     * @param <T> any object for [rx.Observable]
     * @return [Subscription]
    </T> */
    internal fun <T> bindUntilEvent(observable: Observable<T>,
                                    event: ActivityEvent,
                                    subscriber: Subscriber<T> = DefaultSubscriber()): Subscription =
        composeUntilEvent(observable, event).subscribe(subscriber)

    /**
     * Executes an observable subscription based on [RxLifecycle]
     * [LifecycleProvider]
     *
     * @param observable [rx.Observable] given observable for subscribe
     * @param event [FragmentEvent] a fragment on [RxLifecycle]
     * @param action [OPTIONAL] [rx.functions.Action1] custom action
     * @param <T> any object for [rx.Observable]
     * @return [Subscription]
    </T> */
    internal fun <T> bindUntilEvent(observable: Observable<T>,
                                    event: ActivityEvent, action: Action1<T>?): Subscription {
        action?.let {
            return composeUntilEvent(observable, event).subscribe(action)
        }.run {
            return bindUntilEvent(observable, event)
        }
    }

    /**
     * Helper methods, Composes a new [rx.Observable] based on a specific event
     *
     * @param observable [rx.Observable] given observable for subscribe
     * @param event [FragmentEvent] the event which must stop emitting items
     * @param <T> any object for [rx.Observable]
     * @return [rx.Observable]
    </T> */
    @Suppress("UNCHECKED_CAST")
    private fun <T> composeUntilEvent(observable: Observable<T>,
                                      event: FragmentEvent): Observable<T> {
        val provider = lifecycleProvider as LifecycleProvider<FragmentEvent>
        return observable.doOnUnsubscribe(loggingUnsub).compose(provider.bindUntilEvent(event))
    }

    /**
     * Helper methods, Composes a new [rx.Observable] based on a specific event
     *
     * @param observable [rx.Observable] given observable for subscribe
     * @param event [FragmentEvent] the event which must stop emitting items
     * @param <T> any object for [rx.Observable]
     * @return [rx.Observable]
    </T> */
    @Suppress("UNCHECKED_CAST")
    private fun <T> composeUntilEvent(observable: Observable<T>,
                                      event: ActivityEvent): Observable<T> {
        val provider = lifecycleProvider as LifecycleProvider<ActivityEvent>
        return observable.doOnUnsubscribe(loggingUnsub).compose(provider.bindUntilEvent(event))
    }

    /**
     * Helper methods, Composes a new [rx.Observable] based on a specific event
     *
     * @param observable [rx.Observable] given observable for subscribe
     * @param <T> any object for [rx.Observable]
     * @return [rx.Observable]
    </T> */
    @Suppress("UNCHECKED_CAST")
    private fun <T> composeLifecycle(observable: Observable<T>, scheduler: Scheduler): Observable<T> {
        return if (naviComponent is Fragment) {
            val provider = lifecycleProvider as LifecycleProvider<FragmentEvent>
            observable.doOnUnsubscribe(loggingUnsub)
                .compose(applySchedulers(scheduler))
                .compose(provider.bindUntilEvent(FragmentEvent.DESTROY_VIEW))
        } else {
            val provider = lifecycleProvider as LifecycleProvider<ActivityEvent>
            observable.doOnUnsubscribe(loggingUnsub)
                .compose(applySchedulers(scheduler))
                .compose(provider.bindUntilEvent(ActivityEvent.DESTROY))
        }
    }

    /**
     * We are using RxJava transformers to compose the observables in order
     * to not break the chain and help in the UI to do process and avoid
     * lagging
     */
    private fun <T> applySchedulers(scheduler: Scheduler): Observable.Transformer<T, T> {
        return Observable.Transformer { observable ->
            observable.subscribeOn(scheduler)
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

    /**
     * Used to follow the lifecycle and bind the view
     */
    private fun initFragment() {
        this.mvpView = naviComponent as MvpView?
        this.lifecycleProvider = NaviLifecycle.createFragmentLifecycleProvider(naviComponent)
        this.initCommon(Event.VIEW_CREATED)
    }

    /**
     * Used to follow the lifecycle and bind the view
     */
    private fun initActivity() {
        this.mvpView = naviComponent as MvpView?
        this.lifecycleProvider = NaviLifecycle.createActivityLifecycleProvider(naviComponent)
        this.initCommon(Event.START)
    }

    /**
     * Standard common lifecycle
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T> initCommon(event: Event<T>) {
        this.subscriptions.add(
            RxNavi.observe(naviComponent, event).subscribe { this@RxPresenter.bindView(mvpView as V) })
        this.subscriptions.add(
            RxNavi.observe(naviComponent, Event.RESUME).subscribe { this@RxPresenter.resume() })
        this.subscriptions.add(
            RxNavi.observe(naviComponent, Event.PAUSE).subscribe { this@RxPresenter.pause() })
        this.subscriptions.add(
            RxNavi.observe(naviComponent, Event.DESTROY).subscribe({ this@RxPresenter.destroy() }))
    }
}