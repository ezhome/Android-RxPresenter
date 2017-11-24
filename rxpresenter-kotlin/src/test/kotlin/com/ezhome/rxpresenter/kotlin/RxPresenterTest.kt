package com.ezhome.rxpresenter.kotlin

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import com.ezhome.rxpresenter.mvp.MvpView
import com.trello.navi.component.NaviActivity
import com.trello.navi.component.support.NaviDialogFragment
import com.trello.navi.component.support.NaviFragment
import com.trello.rxlifecycle.android.FragmentEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class RxPresenterTest {

    private lateinit var presenter: TestRxPresenter

    @Before
    @Throws(Exception::class)
    fun setUp() {
        this.presenter = TestRxPresenter()
    }

    @Test
    @Throws(Exception::class)
    fun testBindFragment() {
        val fragment = TestFragment()
        startFragment(fragment)
        this.presenter.bind(fragment)
        this.testBind(fragment)
    }

    @Test
    @Throws(Exception::class)
    fun testBindDialogFragment() {
        val fragment = TestDialogFragment()
        startFragment(fragment)
        this.presenter.bind(fragment)
        this.testBind(fragment)
    }

    @Test
    @Throws(Exception::class)
    fun testBindActivity() {
        val controller = Robolectric.buildActivity(TestActivity::class.java).create().start()
        val activity = controller.get() as NaviActivity

        this.presenter.bind(activity)

        controller.create()
        //before activity start view is unbound
        assertThat(this.presenter.view == null).isEqualTo(true)

        controller.start()
        //after activity started view has been bound automatically
        assertThat(this.presenter.view != null).isEqualTo(true)

        controller.destroy()
        //before activity destroyed view is unbound
        assertThat(this.presenter.view == null).isEqualTo(true)
    }

    @Test
    @Throws(Exception::class)
    fun testFragmentObservableBindLifecycle() {
        val observable = PublishSubject.create<Any>().asObservable()

        val testSubscriber = TestSubscriber.create<Any>()
        val fragment = TestFragment()
        this.presenter.bind(fragment)

        startFragment(fragment)
        this.presenter.bindLifecycle(observable, Schedulers.test(), testSubscriber)

        fragment.onAttach(null)
        assertThat(testSubscriber.isUnsubscribed).isEqualTo(false)
        fragment.onCreate(null)
        assertThat(testSubscriber.isUnsubscribed).isEqualTo(false)
        fragment.onViewCreated(null, null)
        assertThat(testSubscriber.isUnsubscribed).isEqualTo(false)
        fragment.onStart()
        assertThat(testSubscriber.isUnsubscribed).isEqualTo(false)
        fragment.onResume()
        assertThat(testSubscriber.isUnsubscribed).isEqualTo(false)
        fragment.onPause()
        assertThat(testSubscriber.isUnsubscribed).isEqualTo(false)
        fragment.onStop()
        assertThat(testSubscriber.isUnsubscribed).isEqualTo(false)
        fragment.onDestroyView()
        testSubscriber.assertCompleted()
        testSubscriber.assertUnsubscribed()
    }

    @Test
    @Throws(Exception::class)
    fun testActivityObservableBindLifecycle() {
        val observable = PublishSubject.create<Any>().asObservable()

        val testSubscriber = TestSubscriber.create<Any>()

        val controller = Robolectric.buildActivity(TestActivity::class.java).create().start()
        val activity = controller.get() as NaviActivity
        this.presenter.bind(activity)

        this.presenter.bindLifecycle(observable, subscriber = testSubscriber)

        controller.create()
        assertThat(testSubscriber.isUnsubscribed).isEqualTo(false)
        controller.start()
        assertThat(testSubscriber.isUnsubscribed).isEqualTo(false)
        controller.resume()
        assertThat(testSubscriber.isUnsubscribed).isEqualTo(false)
        controller.pause()
        assertThat(testSubscriber.isUnsubscribed).isEqualTo(false)
        controller.stop()
        assertThat(testSubscriber.isUnsubscribed).isEqualTo(false)
        controller.destroy()
        testSubscriber.assertCompleted()
        testSubscriber.assertUnsubscribed()
    }

    @Test
    @Throws(Exception::class)
    fun testActivityObservableBindUntilEvent() {
        val observable = PublishSubject.create<Any>().asObservable()

        val testSubscriber = TestSubscriber.create<Any>()
        val fragment = TestFragment()
        this.presenter.bind(fragment)

        startFragment(fragment)
        this.presenter.bindUntilEvent(observable, FragmentEvent.STOP, testSubscriber)

        fragment.onAttach(null)
        assertThat(testSubscriber.isUnsubscribed)
        fragment.onCreate(null)
        assertThat(testSubscriber.isUnsubscribed)
        fragment.onViewCreated(null, null)
        assertThat(testSubscriber.isUnsubscribed)
        fragment.onStart()
        assertThat(testSubscriber.isUnsubscribed)
        fragment.onResume()
        assertThat(testSubscriber.isUnsubscribed)
        fragment.onPause()
        assertThat(testSubscriber.isUnsubscribed)
        fragment.onStop()
        testSubscriber.assertCompleted()
        testSubscriber.assertUnsubscribed()
        fragment.onDestroyView()
        testSubscriber.assertCompleted()
        testSubscriber.assertUnsubscribed()
    }

    private fun testBind(fragment: Fragment) {
        fragment.onAttach(null)
        fragment.onCreate(null)

        //before view created null
        assertThat(this.presenter.view == null).isEqualTo(true)

        fragment.onCreateView(null, null, null)
        fragment.onViewCreated(null, null)

        //after view created, the view has been bound automatically
        assertThat(this.presenter.view != null).isEqualTo(true)

        fragment.onStart()
        fragment.onResume()
        fragment.onPause()
        fragment.onStop()
        fragment.onDestroy()

        //after on destroy the view has been released automatically
        assertThat(this.presenter.view == null).isEqualTo(true)
    }

    private fun startFragment(fragment: Fragment) {
        Robolectric.setupActivity(FragmentActivity::class.java)
            .supportFragmentManager
            .beginTransaction()
            .add(fragment, null)
            .commit()
    }

    class TestFragment : NaviFragment(), MvpView

    class TestDialogFragment : NaviDialogFragment(), MvpView

    class TestActivity : NaviActivity(), MvpView
}
