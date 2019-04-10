package com.willowtreeapps.common.boundary

import com.willowtreeapps.common.*
import com.willowtreeapps.common.repo.Profile

/**
 * Functions for transforming AppState data into ViewState data to be used by Views.
 */
fun AppState.toQuestionViewState(): QuestionViewState {
    val profile = currentQuestionProfile()
    val imageUrl = profile.headshot.url
    val choice1 = getProfile(currentQuestion?.choices?.get(0))!!.displayName()
    val choice2 = getProfile(currentQuestion?.choices?.get(1))!!.displayName()
    val choice3 = getProfile(currentQuestion?.choices?.get(2))!!.displayName()
    val choice4 = getProfile(currentQuestion?.choices?.get(3))!!.displayName()
    val correctBtnNum = currentQuestion?.choices?.indexOfFirst { it.id == profile.id }!! + 1
    var selectedBtnNum = currentQuestion?.choices?.indexOfFirst { getProfile(it)?.matches(currentQuestion?.answerName ?: "") ?: false}
    if (selectedBtnNum != null) {
        selectedBtnNum += 1
    }
    var lastSelectBtnNum = -1
    if (currentQuestionIndex > 0) {

        val lastProfile = getProfile(questions[currentQuestionIndex - 1].profileId)
        lastSelectBtnNum = questions[currentQuestionIndex - 1]?.choices?.indexOfFirst { it.id == lastProfile?.id }
    }
    return QuestionViewState(title = "Who is this?",
            profileImageUrl = "https:$imageUrl",
            currentQuestion = (currentQuestionIndex + 1).toString(),
            numQuestions = questions.size.toString(),
            button1Text = choice1,
            button2Text = choice2,
            button3Text = choice3,
            button4Text = choice4,
            correctBtnNum = correctBtnNum,
            timerText = timerText,
            lastCorrectBtnNum = lastSelectBtnNum,
            selectedBtnNum =  selectedBtnNum ?: -1)
}

fun Profile.displayName() = "$firstName $lastName"


fun AppState.toGameResultsViewState(): GameResultsViewState {
    val percentage = ((numCorrect.toFloat() / questions.size) * 100).toInt()
    val messageText = when (percentage) {
        100 -> perfectScoreResponses.takeRandom()
        in 80..99 -> goodScoreResponses.takeRandom()
        in 50..79 -> okScoreResponses.takeRandom()
        in 10..49 -> badScoreResponses.takeRandom()
        0 -> zeroScoreResponses.takeRandom()
        else -> throw IllegalStateException("Error in toGameResultsViewState when statement or invalid game state")
    }
    return GameResultsViewState(resultsText = gameTotals(),
            messageText = messageText)
}

fun UserSettings.toViewState(): SettingsViewState = SettingsViewState(this.numQuestions)

//TODO should this be here?
private fun AppState.roundTotals() = "${currentQuestionIndex + 1} out of ${questions.size}"

private fun AppState.gameTotals() = "$numCorrect out of ${questions.size}"

