package com.willowtreeapps.common.middleware

import com.beyondeye.reduks.Store
import com.willowtreeapps.common.Actions
import com.willowtreeapps.common.AppState

class NavigationMiddleware(private val navigator: Navigator) {

    fun dispatch(store: Store<AppState>, nextDispatcher: (Any) -> Any, action: Any): Any {
        val result = nextDispatcher(action)
        when (action) {
            is Actions.Navigate -> navigator.goto(action.screen)
        }
        return result
    }
}

enum class Screen {
    START,
    QUESTION,
    GAME_COMPLETE,
    SETTINGS
}

interface Navigator {
    fun goto(screen: Screen)
}