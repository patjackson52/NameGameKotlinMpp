package com.willowtreeapps.namegame

import android.app.Application
import com.willowtreeapps.common.GameEngine
import com.willowtreeapps.common.middleware.Navigator
import kotlinx.coroutines.Dispatchers

class NameGameApp : Application() {

    private lateinit var navigator: AndroidNavigator

    val gameEngine by lazy {
        val engine = GameEngine(navigator, this, Dispatchers.IO, Dispatchers.Main)
        engine.start()
        engine
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        navigator = AndroidNavigator()

        registerActivityLifecycleCallbacks(navigator)
    }

    companion object {
        lateinit var instance: NameGameApp

        fun gameEngine() = instance.gameEngine
    }
}