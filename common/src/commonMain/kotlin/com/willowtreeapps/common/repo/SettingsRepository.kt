package com.willowtreeapps.common.repo

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import com.willowtreeapps.common.AppState
import com.willowtreeapps.common.util.TimeUtil
import com.willowtreeapps.common.util.profile
import kotlinx.serialization.json.Json
import kotlinx.serialization.parse

expect fun userSettings(context: Any? = null): Settings

class LocalStorageSettingsRepository(private val settings: Settings) {

    var numRounds: Int
        get() = settings.getInt(NUM_ROUNDS, 4)
        set(num) {
            settings[NUM_ROUNDS] = num
        }


    var gameState: AppState
        get() {
            return profile("Load gameState") {
                val json = settings.getString(GAME_STATE, "")
                if (json.isBlank()) {
                    AppState.INITIAL_STATE
                } else {
                    Json.nonstrict.parse(AppState.serializer(), json)
                }
            }
        }
        set(appstate) {
            profile("Saving gamestate") {
                val json = Json.nonstrict.stringify(AppState.serializer(), appstate)
                settings[GAME_STATE] = json
            }
        }

    companion object {
        const val NUM_ROUNDS = "NUM_ROUNDS"
        const val GAME_STATE = "GAME_STATE"
    }
}