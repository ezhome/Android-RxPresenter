package com.ezhome.rxpresenter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import com.ezhome.rxpresenter.mvp.MvpView;
import com.ezhome.rxpresenter.reactive.DefaultSubscriber;
import com.trello.navi.component.NaviActivity;
import com.trello.navi.component.support.NaviDialogFragment;
import com.trello.navi.component.support.NaviFragment;
import com.trello.rxlifecycle.android.FragmentEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class RxPresenterTest {

  private RxPresenter<TestMvpView> presenter;

  @Before public void setUp() throws Exception {
    this.presenter = new TestRxPresenter();
  }

  @After public void tearDown() throws Exception {
    this.presenter = null;
  }

  @Test public void testBindFragment() throws Exception {
    NaviFragment fragment = new TestFragment();
    startFragment(fragment);
    this.presenter.bind(fragment);
    this.testBind(fragment);
  }

  @Test public void testBindDialogFragment() throws Exception {
    NaviDialogFragment fragment = new TestDialogFragment();
    startFragment(fragment);
    this.presenter.bind(fragment);
    this.testBind(fragment);
  }

  @Test public void testBindActivity() throws Exception {
    ActivityController controller = Robolectric.buildActivity(TestActivity.class).create().start();
    NaviActivity activity = (NaviActivity) controller.get();

    this.presenter.bind(activity);

    controller.create();
    //before activity start view is unbound
    assertTrue(this.presenter.view == null);

    controller.start();
    //after activity started view has been bound automatically
    assertTrue(this.presenter.view != null);

    controller.destroy();
    //before activity destroyed view is unbound
    assertTrue(this.presenter.view == null);
  }

  @Test public void testFragmentObservableBindLifecycle() throws Exception {
    final Observable<Object> observable = PublishSubject.create().asObservable();

    TestSubscriber<Object> testSubscriber = TestSubscriber.create();
    NaviFragment fragment = new TestFragment();
    this.presenter.bind(fragment);

    startFragment(fragment);
    this.presenter.bindLifecycle(observable, Schedulers.test(), testSubscriber);

    fragment.onAttach(null);
    assertFalse(testSubscriber.isUnsubscribed());
    fragment.onCreate(null);
    assertFalse(testSubscriber.isUnsubscribed());
    fragment.onViewCreated(null, null);
    assertFalse(testSubscriber.isUnsubscribed());
    fragment.onStart();
    assertFalse(testSubscriber.isUnsubscribed());
    fragment.onResume();
    assertFalse(testSubscriber.isUnsubscribed());
    fragment.onPause();
    assertFalse(testSubscriber.isUnsubscribed());
    fragment.onStop();
    assertFalse(testSubscriber.isUnsubscribed());
    fragment.onDestroyView();
    testSubscriber.assertCompleted();
    testSubscriber.assertUnsubscribed();
  }

  @Test public void testActivityObservableBindLifecycle() throws Exception {
    final Observable<Object> observable = PublishSubject.create().asObservable();

    TestSubscriber<Object> testSubscriber = TestSubscriber.create();

    ActivityController controller = Robolectric.buildActivity(TestActivity.class).create().start();
    NaviActivity activity = (NaviActivity) controller.get();
    this.presenter.bind(activity);

    this.presenter.bindLifecycle(observable, testSubscriber);

    controller.create();
    assertFalse(testSubscriber.isUnsubscribed());
    controller.start();
    assertFalse(testSubscriber.isUnsubscribed());
    controller.resume();
    assertFalse(testSubscriber.isUnsubscribed());
    controller.pause();
    assertFalse(testSubscriber.isUnsubscribed());
    controller.stop();
    assertFalse(testSubscriber.isUnsubscribed());
    controller.destroy();
    testSubscriber.assertCompleted();
    testSubscriber.assertUnsubscribed();
  }

  @Test public void testActivityObservableBindUntilEvent() throws Exception {
    final Observable<Object> observable = PublishSubject.create().asObservable();

    TestSubscriber<Object> testSubscriber = TestSubscriber.create();
    NaviFragment fragment = new TestFragment();
    this.presenter.bind(fragment);

    startFragment(fragment);
    this.presenter.bindUntilEvent(observable, FragmentEvent.STOP, testSubscriber);

    fragment.onAttach(null);
    assertFalse(testSubscriber.isUnsubscribed());
    fragment.onCreate(null);
    assertFalse(testSubscriber.isUnsubscribed());
    fragment.onViewCreated(null, null);
    assertFalse(testSubscriber.isUnsubscribed());
    fragment.onStart();
    assertFalse(testSubscriber.isUnsubscribed());
    fragment.onResume();
    assertFalse(testSubscriber.isUnsubscribed());
    fragment.onPause();
    assertFalse(testSubscriber.isUnsubscribed());
    fragment.onStop();
    testSubscriber.assertCompleted();
    testSubscriber.assertUnsubscribed();
    fragment.onDestroyView();
    testSubscriber.assertCompleted();
    testSubscriber.assertUnsubscribed();
  }

  @SuppressWarnings("unchecked") @Test(expected = IllegalArgumentException.class)
  public void testObservableBindLifecycleEmptySubscribersArray() throws Exception {
    final Observable<Object> observable = PublishSubject.create().asObservable();

    ActivityController controller = Robolectric.buildActivity(TestActivity.class).create().start();
    NaviActivity activity = (NaviActivity) controller.get();
    this.presenter.bind(activity);

    this.presenter.bindLifecycle(observable, new DefaultSubscriber(), new DefaultSubscriber());
  }

  private void testBind(Fragment fragment) {
    fragment.onAttach(null);
    fragment.onCreate(null);

    //before view created null
    assertTrue(this.presenter.view == null);

    fragment.onCreateView(null, null, null);
    fragment.onViewCreated(null, null);

    //after view created, the view has been bound automatically
    assertTrue(this.presenter.view != null);

    fragment.onStart();
    fragment.onResume();
    fragment.onPause();
    fragment.onStop();
    fragment.onDestroy();

    //after on destroy the view has been released automatically
    assertTrue(this.presenter.view == null);
  }

  private void startFragment(Fragment fragment) {
    Robolectric.setupActivity(FragmentActivity.class)
        .getSupportFragmentManager()
        .beginTransaction()
        .add(fragment, null)
        .commit();
  }

  public static class TestFragment extends NaviFragment implements MvpView {

  }

  public static class TestDialogFragment extends NaviDialogFragment implements MvpView {

  }

  public static class TestActivity extends NaviActivity implements MvpView {

  }
}
