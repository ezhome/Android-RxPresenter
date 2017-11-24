package com.ezhome.rxpresenter.mvp

import com.trello.navi.component.NaviActivity
import com.trello.navi.component.support.NaviAppCompatActivity
import com.trello.navi.component.support.NaviDialogFragment
import com.trello.navi.component.support.NaviFragment

/**
 * Interface representing a Presenter in a model view presenter (MVP) pattern.
 */
interface Presenter<V : MvpView> {
    /**
     * Binds a fragment to proper lifecycle
     *
     * @param fragment [NaviFragment]
     */
    fun bind(fragment: NaviFragment)

    /**
     * Binds a dialog fragment to proper lifecycle
     *
     * @param fragment [NaviDialogFragment]
     */
    fun bind(fragment: NaviDialogFragment)

    /**
     * Binds a activity to proper lifecycle
     *
     * @param activity [NaviAppCompatActivity]
     */
    fun bind(activity: NaviAppCompatActivity)

    /**
     * Binds a activity to proper lifecycle
     *
     * @param activity [NaviActivity]
     */
    fun bind(activity: NaviActivity)

    /**
     * Method that control the lifecycle of the view.
     */
    fun resume()

    /**
     * Method that control the lifecycle of the view.
     */
    fun pause()

    /**
     * Method that control the lifecycle of the view.
     * It should be called in the view's (Activity or Fragment)
     * onDestroy() method.
     */
    fun destroy()
}