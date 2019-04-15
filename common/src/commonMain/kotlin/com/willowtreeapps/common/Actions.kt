package com.willowtreeapps.common

import com.beyondeye.reduks.Action
import com.willowtreeapps.common.repo.ItemsHolder
import com.willowtreeapps.common.repo.Profile

internal sealed class Actions : Action {

    class FetchingItemsStartedAction
    class FetchingItemsSuccessAction(val itemsHolder: ItemsHolder)
    class FetchingItemsFailedAction(val message: String)

    class NamePickedAction(val name: String)

    class NextQuestionAction

    class GameCompleteAction

    class StartOverAction
    class ResetGameStateAction

    class StartQuestionTimerAction(val initialValue: Int)
    class DecrementCountDownAction
    class TimesUpAction


    class SettingsTappedAction
    class LoadAllSettingsAction
    class SettingsLoadedAction(val settings: UserSettings)
    class ChangeNumQuestionsSettingsAction(val num: Int)
    class ChangeCategorySettingsAction(val categoryId: QuestionCategoryId)

}

