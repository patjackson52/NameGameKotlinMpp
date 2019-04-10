package com.willowtreeapps.common

import com.willowtreeapps.common.Actions.*
import com.willowtreeapps.common.boundary.toQuestionViewState
import com.willowtreeapps.common.middleware.Cmd2
import com.willowtreeapps.common.repo.Profile
import com.willowtreeapps.common.ui.QuestionPresenter
import com.willowtreeapps.common.ui.QuestionView
import com.willowtreeapps.common.util.TimeUtil
import kotlin.math.abs
import kotlin.random.Random

/**
 * Reducers and functions used by reducers are in this file.  Functions must be pure functions without
 * side effects.
 */
fun reducer(state: AppState, action: Any): AppState =
        when (action) {
            is FetchingProfilesStartedAction -> state.copy(isLoadingProfiles = true)
            is FetchingProfilesSuccessAction -> {
                val rounds = generateRounds(action.profiles, state.settings.numQuestions)
                state.copy(isLoadingProfiles = false, profiles = action.profiles, questions = rounds)
            }
            is FetchingProfilesFailedAction -> state.copy(isLoadingProfiles = false, errorLoadingProfiles = true, errorMsg = action.message)
            is NamePickedAction -> {
                val status = if (state.currentQuestionProfile().matches(action.name)) {
                    Question.Status.CORRECT
                } else {
                    Question.Status.INCORRECT
                }
                val newQuestions = state.questions.toMutableList()
                newQuestions[state.currentQuestionIndex] = newQuestions[state.currentQuestionIndex].copy(answerName = action.name, status = status)
                val newState = state.copy(questions = newQuestions, waitingForNextQuestion = true)
                val viewCmd = when (status) {
                    Question.Status.CORRECT -> Cmd2.run(PresenterFactory::questPresenter, QuestionPresenter::view, QuestionView::showCorrectAnswerAnimation, newState.toQuestionViewState(), state.isGameComplete())
                    Question.Status.INCORRECT -> Cmd2.run(PresenterFactory::questPresenter, QuestionPresenter::view, QuestionView::showWrongAnswerAnimation, newState.toQuestionViewState(), state.isGameComplete())
                    else -> throw IllegalArgumentException("Status $status is invalid for this action($action)")
                }
                newState.copy(viewCmd = viewCmd)
            }
            is NextQuestionAction -> {
                val newState = state.copy(waitingForNextQuestion = false, currentQuestionIndex = state.currentQuestionIndex + 1)
                val viewCmd = Cmd2.run(PresenterFactory::questPresenter, QuestionPresenter::view, QuestionView::showProfileAnimation, newState.toQuestionViewState())
                newState.copy(viewCmd = viewCmd)
            }
            is GameCompleteAction -> state.copy(waitingForResultsTap = true, waitingForNextQuestion = false, currentQuestionIndex = state.currentQuestionIndex + 1)
            is StartOverAction, is ResetGameStateAction -> AppState.INITIAL_STATE.copy(settings = state.settings)
            is StartQuestionTimerAction -> {
                val newState = state.copy(questionClock = action.initialValue)
                val viewCmd = Cmd2.run(PresenterFactory::questPresenter, QuestionPresenter::view, QuestionView::setTimerText, newState.toQuestionViewState())
                newState.copy(viewCmd = viewCmd)
            }
            is DecrementCountDownAction -> {
                val newState = state.copy(questionClock = state.questionClock - 1)
                val viewCmd = Cmd2.run(PresenterFactory::questPresenter, QuestionPresenter::view, QuestionView::setTimerText, newState.toQuestionViewState())
                newState.copy(viewCmd = viewCmd)
            }
            is TimesUpAction -> {
                val status = Question.Status.TIMES_UP
                val newQuestions = state.questions.toMutableList()
                newQuestions[state.currentQuestionIndex] = newQuestions[state.currentQuestionIndex].copy(answerName = "", status = status)
                val newState = state.copy(questions = newQuestions, waitingForNextQuestion = true, questionClock = -1)
                val viewCmd = Cmd2.run(PresenterFactory::questPresenter, QuestionPresenter::view, QuestionView::showTimesUpAnimation, newState.toQuestionViewState(), newState.isGameComplete())
                newState.copy(viewCmd = viewCmd)
            }

            is ChangeNumQuestionsSettingsAction -> state.copy(settings = state.settings.copy(numQuestions = action.num))

            is Navigate -> state.copy(currentScreen = action.screen)
            is GameStateLoaded -> action.savedState

            else -> throw AssertionError("Action ${action::class.simpleName} not handled")
        }

fun generateRounds(profiles: List<Profile>, n: Int): List<Question> =
        profiles.takeRandomDistinct(n)
                .map {
                    val choiceList = profiles.takeRandomDistinct(3).toMutableList()
                    choiceList.add(abs(random.nextInt() % 4), it)

                    Question(profileId = ProfileId(it.id), choices = choiceList
                            .map { ProfileId(it.id) })
                }


private val random = Random(TimeUtil.systemTimeMs())

/**
 * Take N distict elements from the list.  Distinct is determined by a comparision of objects in the
 * list.
 * @throws IllegalStateException when n > number of distict elements.
 * @return New immutable list containing N random elements from the given List.
 */
fun <T> List<T>.takeRandomDistinct(n: Int): List<T> {
    val newList = mutableListOf<T>()
    val uniqueItems = this.distinctBy { it }
    if (uniqueItems.size < n) {
        throw IllegalStateException("Unable to get $n unique random elements from given list.")
    }
    while (newList.size < n) {
        val randomIndex = abs(random.nextInt() % uniqueItems.size)
        val next = uniqueItems[randomIndex]
        if (newList.contains(next)) {
            continue
        } else {
            newList.add(next)
        }
    }
    return newList.toList()
}


fun <T> List<T>.takeRandom(): T =
        this[random.nextInt(this.size - 1)]
