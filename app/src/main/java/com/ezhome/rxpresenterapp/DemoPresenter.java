package com.ezhome.rxpresenterapp;

import com.ezhome.rxpresenter.RxPresenter;

/**
 * A demo presenter for MVP
 */
class DemoPresenter extends RxPresenter<DemoView> {

  DemoPresenter() {
  }

  @Override public void resume() {
    super.resume();
    this.view.renderResumeTxt();
  }

  @Override public void pause() {
    super.pause();
    this.view.renderPauseTxt();
  }
}
