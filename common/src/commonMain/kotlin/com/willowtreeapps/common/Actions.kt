package com.willowtreeapps.common

import com.beyondeye.reduks.Action
import com.willowtreeapps.common.repo.Profile

sealed class Actions : Action {

    class GameStartedAction()

    class FetchingProfilesStartedAction
    class FetchingProfilesSuccessAction(val profiles: List<Profile>)
    class FetchingProfilesFailedAction(val message: String)

    class NamePickedAction(val name: String)

    class NextQuestionAction

    class GameCompleteAction

    class StartOverAction
    class ResetGameStateAction()

}