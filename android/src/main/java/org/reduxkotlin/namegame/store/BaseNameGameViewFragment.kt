package org.reduxkotlin.namegame.store

import android.os.Bundle
import androidx.fragment.app.Fragment
import org.reduxkotlin.namegame.common.ui.GameBaseView
import org.reduxkotlin.PresenterLifecycleObserver

open class BaseNameGameViewFragment<V: GameBaseView>: Fragment(), GameBaseView {
    private val presenterObserver = PresenterLifecycleObserver(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        retainInstance = true
        lifecycle.addObserver(presenterObserver)
        super.onCreate(savedInstanceState)
    }
}
