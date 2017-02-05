[![Hex.pm](https://img.shields.io/hexpm/l/plug.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Platform](https://img.shields.io/badge/platform-android-green.svg)](http://developer.android.com/index.html)
[ ![Download](https://api.bintray.com/packages/ezhome/maven/rxpresenter/images/download.svg) ](https://bintray.com/ezhome/maven/rxpresenter/_latestVersion)

# RxPresenter

A Reactive Presenter library for MV**P** pattern for modern Android Apps. This library follows the
lifecycle of an android app component (`Fragment`, `DialogFragment`). Specifically relies on [RxLifecycle](https://github.com/trello/RxLifecycle) by Trello.

----
Contents
--------
- [Usage](#usage)
- [Download](#download)
- [Tests](#tests)
- [Code style](#code-style)
- [License](#license)

Usage
-----

*NOTE: Currently supports only `Fragment` and `DialogFragment`*

You need to define an interface which representing a View in a MVP pattern and there is a 1-to-1 relationship
and two way communication between View & Presenter. In our case this interface must extends `MvpView` eg.

```java
interface DemoView extends MvpView {

}
```

Then you need to create a Presenter in the MVP pattern. In this librady the Presenter must extend `RxPresenter<V>`
where `V` is the `View` interface which we defined above. eg.

```java
class DemoPresenter extends RxPresenter<DemoView> {

  DemoPresenter() {
  }
 }
```

The corresponding android app component must implements the `View` interface! After that you need just
to initialise the presenter and `bind(component)` it to `Fragment` or `DialogFragment`. eg.

**Without Dependency Injection**
```
public class DemoFragment extends MvpFragment implements DemoView {

  private Presenter presenter = new DemoPresenter();

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.presenter.bind(this);
  }
}
```


**With Dependency Injection - Dagger2**
```
public class DemoFragment extends MvpFragment implements DemoView {

  @Inject Presenter presenter;

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    //Dagger2 injection code

    this.presenter.bind(this);
  }
}
```

After the line `this.presenter.bind(this)` the Presenter starts following the Android App Component's
lifecycle and specifically on `onViewCreated` we bound the corresponding interface `View` automatically without the
need to do it explicitly. Then the Presenter follows the typical lifecycle methods `onResume`, `onPause`, `onDestroy`.

`RxPresenter` exposes also helper methods which will help your RxJava observables to follow the Android App Component's
lifecycle (with the use of [RxLifecycle](https://github.com/trello/RxLifecycle) by Trello).

1. By default stops emitting in `FragmentEvent.DESTROY_VIEW` and use a `Subscriber<T>` to "react"
```java
bindLifecycle(@NonNull Observable<T> observable, Subscriber<T>... subscribers)
```

2. By default stops emitting in `FragmentEvent.DESTROY_VIEW` and use an `Action1<T>` to "react"
```java
bindLifecycle(@NonNull Observable<T> observable, Action1<T> action)
```

3. Stops emitting on the provided `FragmentEvent` and use a `Subscriber<T>` to "react"
```java
 bindUntilEvent(@NonNull Observable<T> observable, @NonNull FragmentEvent event, Subscriber<T>... subscribers)
```

4. Stops emitting on the provided `FragmentEvent` and use an `Action1<T>` to "react"
```java
 bindUntilEvent(@NonNull Observable<T> observable, @NonNull FragmentEvent event, Action1<T> action)
```

Download
--------
The project is available on jCenter. In your app build.gradle (or explicit module) you must add this:
```
dependencies {
  compile 'com.ezhome:rxpresenter:1.0.0'
}
```

Tests
----------

//TDB

Code style
----------

Code style used in the project is called `SquareAndroid` from Java Code Styles repository by Square available at: https://github.com/square/java-code-styles.


License
-------

    Copyright 2016 Ezhome Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.