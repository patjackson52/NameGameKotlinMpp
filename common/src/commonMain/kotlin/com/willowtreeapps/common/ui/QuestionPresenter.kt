package com.willowtreeapps.common.ui

import com.beyondeye.reduks.*
import com.willowtreeapps.common.*
import com.willowtreeapps.common.boundary.toQuestionViewState
import com.willowtreeapps.common.middleware.Screen
import com.willowtreeapps.common.util.VibrateUtil


class QuestionPresenter(
        val store: Store<AppState>,
        private val vibrateUtil: VibrateUtil,
        private val timerThunks: TimerThunks) : Presenter<QuestionView>() {


    override fun makeSubscriber() = SelectorSubscriberFn(store) {

        withSingleField({
            it.currentQuestion?.profileId?.id ?: Any()
        }, { view?.showProfile(state.toQuestionViewState()) })

        withSingleField({ it.waitingForNextQuestion }) {
            if (state.waitingForNextQuestion) {
                when (state.currentQuestion?.status) {
                    Question.Status.CORRECT -> {
                        view?.showCorrectAnswer(state.toQuestionViewState(), state.isGameComplete())
                    }
                    Question.Status.INCORRECT -> {
                        vibrateUtil.vibrate()
                        view?.showWrongAnswer(state.toQuestionViewState(), state.isGameComplete())
                    }
                    Question.Status.TIMES_UP -> {
                        vibrateUtil.vibrate()
//                        view?.showTimesUpAnimation(state.toQuestionViewState(), state.isGameComplete())
                    }
                    Question.Status.UNANSWERED -> throw IllegalStateException("Question status cannot be Unanswered when waiting for next round == true")
                }
            }
        }
    }

    fun namePicked(name: String) {
        store.dispatch(Actions.NamePickedAction(name))
        store.dispatch(timerThunks.stopTimer())
        store.dispatch(Actions.SaveGameState())
    }

    fun nextTapped() {
        store.dispatch(Actions.NextQuestionAction())
    }

    fun onProfileImageVisible() {
        if (!store.state.hasAnsweredCurrentQuestion) {
            store.dispatch(timerThunks.startCountDownTimer(5))
        }
    }

    fun endGameTapped() {
        store.dispatch(Actions.GameCompleteAction())
        store.dispatch(Actions.Navigate(Screen.GAME_COMPLETE))
        store.dispatch(Actions.SaveGameState())
    }

    fun onBackPressed() {
        store.dispatch(Actions.StartOverAction())
        store.dispatch(timerThunks.stopTimer())
        store.dispatch(Actions.SaveGameState())
    }

}