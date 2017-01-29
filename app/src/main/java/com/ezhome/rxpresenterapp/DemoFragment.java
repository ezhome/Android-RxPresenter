package com.ezhome.rxpresenterapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.ezhome.rxpresenter.mvp.MvpFragment;
import com.ezhome.rxpresenter.mvp.Presenter;

/**
 * A demo {@link MvpFragment}
 */
public class DemoFragment extends MvpFragment implements DemoView {

  //Views
  @BindView(R.id.demoResumeTxt) TextView demoResumeTxt;
  @BindView(R.id.demoPauseTxt) TextView demoPauseTxt;
  @BindView(R.id.demoDestroyTxt) TextView demoDestroyTxt;

  private Unbinder unbinder;
  private Presenter demoPresenter = new DemoPresenter();

  public static DemoFragment newInstance() {
    return new DemoFragment();
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.demoPresenter.bind(this);
  }

  @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    View fragmentView = inflater.inflate(R.layout.fragment_demo, container, false);
    this.unbinder = ButterKnife.bind(this, fragmentView);
    return fragmentView;
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    if (this.unbinder != null) {
      this.unbinder.unbind();
    }
  }

  @Override public void renderResumeTxt() {
    this.demoResumeTxt.setText("Presenter on resume state");
  }

  @Override public void renderPauseTxt() {
    this.demoPauseTxt.setText("Presenter on resume pause state");

  }

  @Override public void renderDestroyTxt() {
    this.demoDestroyTxt.setText("Presenter on resume destroy staate");

  }
}
