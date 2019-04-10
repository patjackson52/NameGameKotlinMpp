package com.willowtreeapps.common

import com.willowtreeapps.common.middleware.Cmd2
import com.willowtreeapps.common.middleware.Screen
import com.willowtreeapps.common.repo.Profile
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class AppState(val isLoadingProfiles: Boolean = false,
                    val profiles: List<Profile> = listOf(),
                    val errorLoadingProfiles: Boolean = false,
                    val errorMsg: String = "",
                    val currentQuestionIndex: Int = 0,
                    val waitingForNextQuestion: Boolean = false,
                    val waitingForResultsTap: Boolean = false,
                    val questionClock: Int = 0,
                    val questions: List<Question> = listOf(),
                    val currentScreen: Screen = Screen.START,
                    @Transient
                    var viewCmd: Cmd2<*, *, *,*, *> = Cmd2.noOp,
                    val settings: UserSettings = UserSettings.defaults()) {
    companion object {
        val INITIAL_STATE = AppState()
    }

    fun Question.profile(): Profile = profiles.find { ProfileId(it.id) == this.profileId }!!

    @Transient
    val timerText: String
        get() = when {
            hasAnsweredCurrentQuestion -> ""
            questionClock >= 0 -> questionClock.toString()
            else -> "TIME'S UP!!"
        }

    @Transient
    val hasAnsweredCurrentQuestion: Boolean
        get() = currentQuestion?.answerName != null

    @Transient
    val currentQuestion: Question?
        get() = if (questions.size > currentQuestionIndex)
            questions[currentQuestionIndex]
        else
            null

    fun getProfile(id: ProfileId?) = profiles.find { it.id == id?.id }

    fun currentQuestionProfile() = getProfile(currentQuestion?.profileId)!!

    fun isGameComplete(): Boolean = currentQuestionIndex >= questions.size || (currentQuestionIndex == questions.size - 1 && questions[currentQuestionIndex].status != Question.Status.UNANSWERED)

    @Transient
    val numCorrect: Int
        get() = questions.count { it.status == Question.Status.CORRECT }
}

@Serializable
class ProfileId(val id: String)

@Serializable
data class Question(val profileId: ProfileId,
                    val choices: List<ProfileId>,
                    val status: Status = Status.UNANSWERED,
                    val answerName: String? = null) {
    enum class Status {
        UNANSWERED,
        CORRECT,
        INCORRECT,
        TIMES_UP
    }
}

@Serializable
data class UserSettings(val numQuestions: Int) {
    companion object {
        fun defaults() = UserSettings(3)
    }
}

