package com.willowtreeapps.common.middleware

import com.beyondeye.reduks.Store
import com.willowtreeapps.common.*
import com.willowtreeapps.common.Actions.ChangeNumQuestionsSettingsAction
import com.willowtreeapps.common.PresenterFactory
import com.willowtreeapps.common.boundary.toQuestionViewState
import com.willowtreeapps.common.repo.LocalStorageSettingsRepository
import com.willowtreeapps.common.ui.QuestionPresenter
import com.willowtreeapps.common.ui.QuestionView
import com.willowtreeapps.common.ui.StartPresenter
import com.willowtreeapps.common.ui.StartView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.reflect.*

/**
 * Save and Loads user settings from local storage
 */
internal class SettingsMiddleware(private val settings: LocalStorageSettingsRepository,
                                  private val backgroundScope: CoroutineScope) {

    fun dispatch(store: Store<AppState>, nextDispatcher: (Any) -> Any, action: Any): Any {
        backgroundScope.launch {
            when (action) {
                is ChangeNumQuestionsSettingsAction -> settings.numRounds = action.num

                is Actions.LoadAllSettingsAction ->
                    store.dispatch(ChangeNumQuestionsSettingsAction(settings.numRounds))

                is Actions.LoadSavedGameState -> {
                    val savedGameState = settings.gameState
                    if (savedGameState != AppState.INITIAL_STATE) {
                        store.dispatch(Actions.GameStateLoaded(savedGameState))
                        store.dispatch(Actions.Navigate(savedGameState.currentScreen))
                    }
                }

                is Actions.SaveGameState -> settings.gameState = store.state
            }
        }
        return nextDispatcher(action)
    }
}
