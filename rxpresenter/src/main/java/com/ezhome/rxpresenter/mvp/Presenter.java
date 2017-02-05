package com.ezhome.rxpresenter.mvp;

import com.trello.navi.component.NaviActivity;
import com.trello.navi.component.support.NaviAppCompatActivity;
import com.trello.navi.component.support.NaviDialogFragment;
import com.trello.navi.component.support.NaviFragment;

/**
 * Interface representing a Presenter in a model view presenter (MVP) pattern.
 */
public interface Presenter<V extends MvpView> {
  /**
   * Binds a fragment to proper lifecycle
   *
   * @param fragment {@link NaviFragment}
   */
  void bind(NaviFragment fragment);

  /**
   * Binds a dialog fragment to proper lifecycle
   *
   * @param fragment {@link NaviDialogFragment}
   */
  void bind(NaviDialogFragment fragment);

  /**
   * Binds a activity to proper lifecycle
   *
   * @param activity {@link NaviActivity}
   */
  void bind(NaviAppCompatActivity activity);

  /**
   * Method that control the lifecycle of the view.
   */
  void resume();

  /**
   * Method that control the lifecycle of the view.
   */
  void pause();

  /**
   * Method that control the lifecycle of the view.
   * It should be called in the view's (Activity or Fragment)
   * onDestroy() method.
   */
  void destroy();
}