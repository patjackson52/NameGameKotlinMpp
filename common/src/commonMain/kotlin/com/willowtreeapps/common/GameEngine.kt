package com.willowtreeapps.common

import com.beyondeye.reduks.SimpleStore
import com.beyondeye.reduks.combineEnhancers
import com.beyondeye.reduks.middlewares.applyMiddleware
import com.beyondeye.reduks.middlewares.thunkMiddleware
import com.willowtreeapps.common.middleware.NavigationMiddleware
import com.willowtreeapps.common.middleware.Navigator
import com.willowtreeapps.common.middleware.SettingsMiddleware
import com.willowtreeapps.common.middleware.ViewEffectsMiddleware
import com.willowtreeapps.common.repo.LocalStorageSettingsRepository
import com.willowtreeapps.common.repo.userSettings
import com.willowtreeapps.common.util.VibrateUtil
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlin.coroutines.CoroutineContext

class GameEngine(navigator: Navigator,
                 application: Any = Any(),
                 networkContext: CoroutineContext,
                 uiContext: CoroutineContext) {
    private val navigationMiddleware = NavigationMiddleware(navigator)
    internal val presenterFactory by lazy { PresenterFactory(this, networkContext) }
    private val viewEffectsMiddleware by lazy { ViewEffectsMiddleware(this) }
    val vibrateUtil = VibrateUtil(application)
    private val settingsMiddleware by lazy {  SettingsMiddleware(LocalStorageSettingsRepository(userSettings(application)), CoroutineScope(networkContext)) }
    val appStore by lazy {
        SimpleStore(AppState.INITIAL_STATE, ::reducer)
                .applyMiddleware(::thunkMiddleware,
                        viewEffectsMiddleware::dispatch,
                        navigationMiddleware::dispatch,
                        settingsMiddleware::dispatch)
    }

    fun start() {
        appStore.dispatch(Actions.LoadAllSettingsAction())
        appStore.dispatch(Actions.LoadSavedGameState())
    }

    fun attachView(view: View): Presenter<out View?> = presenterFactory.attachView(view)

    fun detachView(view: View) = presenterFactory.detachView(view)
}