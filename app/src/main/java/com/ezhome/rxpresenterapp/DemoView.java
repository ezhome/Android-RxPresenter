package com.ezhome.rxpresenterapp;

import com.ezhome.rxpresenter.mvp.MvpView;

/**
 * Custom demo view contract
 */
interface DemoView extends MvpView {

  void renderResumeTxt();

  void renderPauseTxt();
}
