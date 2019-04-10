package com.willowtreeapps.common.ui

import com.beyondeye.reduks.SelectorSubscriberFn
import com.beyondeye.reduks.Store
import com.willowtreeapps.common.Actions
import com.willowtreeapps.common.AppState
import com.willowtreeapps.common.Presenter
import com.willowtreeapps.common.boundary.toGameResultsViewState
import com.willowtreeapps.common.middleware.Screen

class GameResultsPresenter(val store: Store<AppState>) : Presenter<GameResultsView>() {
    override fun makeSubscriber() = SelectorSubscriberFn(store) {
        withAnyChange { view?.showResults(state.toGameResultsViewState()) }
    }

    fun startOverTapped() {
        store.dispatch(Actions.StartOverAction())
        store.dispatch(Actions.SaveGameState())
        store.dispatch(Actions.Navigate(Screen.START))
    }

    fun onBackPressed() {
        TODO("Handle back press from Game Results")
    }
}