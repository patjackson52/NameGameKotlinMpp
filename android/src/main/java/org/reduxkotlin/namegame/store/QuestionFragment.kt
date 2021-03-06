package org.reduxkotlin.namegame.store

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.*
import android.view.animation.BounceInterpolator
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import org.reduxkotlin.namegame.common.ui.QuestionViewState
import org.reduxkotlin.namegame.common.ui.QuestionView
import kotlinx.android.synthetic.main.fragment_question.*
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import android.widget.Button
import androidx.core.content.res.ResourcesCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import org.reduxkotlin.namegame.common.util.Logger
import org.reduxkotlin.namegame.common.middleware.UiActions
import org.reduxkotlin.namegame.*
import org.reduxkotlin.namegame.MainActivity
import org.reduxkotlin.namegame.dispatch
import org.reduxkotlin.namegame.onComplete
import java.util.*


class QuestionFragment : BaseNameGameViewFragment<QuestionView>(), QuestionView, MainActivity.IOnBackPressed {
    private lateinit var speechRecognizer: SpeechRecognizer
    private val speechRecognizerIntent by lazy {
        val speechRecIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        speechRecIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault())
        speechRecIntent
    }

    override fun openMic() {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
            }

            override fun onRmsChanged(rmsdB: Float) {
            }

            override fun onBufferReceived(buffer: ByteArray?) {
            }

            override fun onPartialResults(partialResults: Bundle) {
                val matches = partialResults
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)

                //displaying the first match
                if (matches != null)
                    dispatch(UiActions.NamePicked(matches[0]))
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
            }

            override fun onBeginningOfSpeech() {
            }

            override fun onEndOfSpeech() {
            }

            override fun onError(error: Int) {
                Logger.d("Error with speech recognizer: code =$error")
            }

            override fun onResults(results: Bundle) {
                val matches = results
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)

                //displaying the first match
                if (matches != null)
                    dispatch(UiActions.NamePicked(matches[0]))
            }

        })
        speechRecognizer.startListening(speechRecognizerIntent)

    }

    override fun closeMic() {
        speechRecognizer.stopListening()
    }

    private var restoreX: Float? = null
    private var restoreY: Float? = null
    private var lastCorrectBtn: Button? = null
    private var lastSelectedBtn: Button? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity!!)
        return inflater.inflate(R.layout.fragment_question, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        button1.setOnClickListener { dispatch(UiActions.NamePicked(button1.text.toString())) }
        button2.setOnClickListener { dispatch(UiActions.NamePicked(button2.text.toString())) }
        button3.setOnClickListener { dispatch(UiActions.NamePicked(button3.text.toString())) }
        button4.setOnClickListener { dispatch(UiActions.NamePicked(button4.text.toString())) }
        btn_next.setOnClickListener { dispatch(UiActions.NextTapped()) }
        btn_end_game.setOnClickListener { dispatch(UiActions.EndGameTapped()) }
    }


    override fun onBackPressed(): Boolean {
        //TODO revisit this - is needed with presenter middleware
//        dispatch(DetachView(this))
//        dispatch(ClearView(this))
        dispatch(UiActions.BackPressOnQuestions())
        closeMic()
//        presenter.onBackPressed()
        return false
    }

    override fun showProfileNotAnimated(viewState: QuestionViewState) {
        view?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                Logger.d("viewState: $viewState")
                GlideApp.with(this@QuestionFragment).load(viewState.itemImageUrl)
                        .into(imageView)
                txt_question_title.text = viewState.title
                when {
                    viewState.nextButtonVisible -> {
                        txt_timer.visibility = View.GONE
                        btn_next.visibility = View.VISIBLE
                        btn_next.alpha = 1f
                        btn_end_game.visibility = View.GONE
                        btn_end_game.alpha = 0f
                        showCorrectButton(viewState.correctBtnNum)
                        setButtonText(viewState)
                    }
                    viewState.endButtonVisible -> {
                        txt_timer.visibility = View.GONE
                        btn_next.visibility = View.GONE
                        btn_next.alpha = 0f
                        btn_end_game.visibility = View.VISIBLE
                        btn_end_game.alpha = 1f
                        showCorrectButton(viewState.correctBtnNum)
                        setButtonText(viewState)
                    }
                    else -> {
                        txt_timer.text = viewState.timerText
                        setButtonText(viewState)
                        setAllButtonsVisible()
                        btn_next.visibility = View.GONE
                        btn_next.alpha = 0f
                        btn_end_game.visibility = View.GONE
                        btn_end_game.alpha = 1f
                        //start timer again
                        dispatch(UiActions.ProfileImageDidShow())
                    }

                }
                view?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
            }
        })
    }

    private fun showCorrectButton(correctBtnNum: Int) {
        val correctBtn = getBtnByNum(correctBtnNum)
        if (correctBtn != null) {
            correctBtn.visibility = View.VISIBLE
            correctBtn.alpha = 1f
            restoreX = correctBtn.x
            restoreY = correctBtn.y
            correctBtn.x = correctBtnX(correctBtn)
            correctBtn.y = correctBtnY()
            correctBtn.scaleX = 2f
            correctBtn.scaleY = 2f
            lastCorrectBtn = correctBtn
        }
    }

    override fun showProfile(viewState: QuestionViewState) {
        if (btn_next.visibility == View.VISIBLE) {
            fadeNextButton { setProfileAndFadeIn(viewState) }
        } else {
            setProfileAndFadeIn(viewState)
        }
    }

    override fun showCorrectAnswer(viewState: QuestionViewState, isEndGame: Boolean) {
        hideButtonsShowNext(viewState, isEndGame)
        celebrate()
    }

    override fun showWrongAnswer(viewState: QuestionViewState, isEndGame: Boolean) {
        wrongShakeAnimation(viewState) { hideButtonsShowNext(viewState, isEndGame) }
    }

    private val showButtonsAnimatorSet by lazy {
        val hide1 = ObjectAnimator.ofFloat(button1, View.ALPHA, 1F)
        val hide2 = ObjectAnimator.ofFloat(button2, View.ALPHA, 1F)
        val hide3 = ObjectAnimator.ofFloat(button3, View.ALPHA, 1F)
        val hide4 = ObjectAnimator.ofFloat(button4, View.ALPHA, 1F)
        val set = AnimatorSet()
        set.playTogether(hide1, hide2, hide3, hide4)
        set
    }

    private fun wrongShakeAnimation(viewState: QuestionViewState, after: () -> Unit) {
        val selectedBtn = getBtnByNum(viewState.selectedBtnNum)
        if (selectedBtn != null) {
            selectedBtn.isSelected = true
            val animScaleX = ObjectAnimator.ofFloat(selectedBtn, View.SCALE_X, 3F, 0.5F, 1F)
            val animScaleY = ObjectAnimator.ofFloat(selectedBtn, View.SCALE_Y, 3F, 0.5F, 1F)
            val upSet = AnimatorSet()
            upSet.playTogether(animScaleX, animScaleY)
            upSet.interpolator = BounceInterpolator()
            upSet.duration = 500
            upSet.onComplete { after() }
            upSet.start()
        } else {
            after()
        }
    }

    private fun correctBtnX(btn: Button) = imageView.x + (imageView.width - btn.width) / 2
    private fun correctBtnY() = imageView.y + imageView.height

    /**
     *  Hides the incorrect buttons and animates the correct name to be centered below profile image
     */
    private fun hideButtonsShowNext(viewState: QuestionViewState, isEndGame: Boolean) {

        val correctBtn = getBtnByNum(viewState.correctBtnNum)
        val selectedBtn = getBtnByNum(viewState.selectedBtnNum)

        fun Button.hideOrMoveAnimation(): AnimatorSet {
            return if (this == correctBtn) {
                val endX = correctBtnX(this)
                val endY = correctBtnY()

                val animX = ObjectAnimator.ofFloat(this, View.X, endX)
                val animY = ObjectAnimator.ofFloat(this, View.Y, endY)
                val animScaleX = ObjectAnimator.ofFloat(this, View.SCALE_X, 2F)
                val animScaleY = ObjectAnimator.ofFloat(this, View.SCALE_Y, 2F)
                val set = AnimatorSet()
                set.playTogether(animX, animY, animScaleX, animScaleY)
                set
            } else {
                val anim = ObjectAnimator.ofFloat(this, View.ALPHA, 0F)
                val set = AnimatorSet()
                set.playTogether(anim)
                set
            }
        }
        restoreX = correctBtn?.x
        restoreY = correctBtn?.y
        lastCorrectBtn = correctBtn
        lastSelectedBtn = selectedBtn

        val anim1 = button1?.hideOrMoveAnimation()
        val anim2 = button2?.hideOrMoveAnimation()
        val anim3 = button3?.hideOrMoveAnimation()
        val anim4 = button4?.hideOrMoveAnimation()

        //TODO replace with isViewCreated fun
        if (anim1 != null) {
            val set = AnimatorSet()
            set.playTogether(anim1, anim2, anim3, anim4)
            set.onComplete {
                val btn = if (isEndGame) {
                    btn_end_game
                } else {
                    btn_next
                }
                if (btn != null) {
                    btn.visibility = View.VISIBLE
                    btn.alpha = 0F
                    btn.animate().alpha(1f)
                }
            }
            set.start()
        }
    }

    private fun showButtons() {
        if (restoreX != null && restoreY != null) {
            lastCorrectBtn?.x = restoreX!!
            lastCorrectBtn?.y = restoreY!!
            lastCorrectBtn?.scaleX = 1F
            lastCorrectBtn?.scaleY = 1F
            lastSelectedBtn?.isSelected = false
        }
        showButtonsAnimatorSet.start()
    }

    private fun fadeNextButton(after: () -> Unit) {
        btn_next.animate().alpha(0f).withEndAction {
            lastCorrectBtn?.alpha = 0f
            btn_next?.visibility = View.GONE
            after()
        }
    }

    private fun setProfileAndFadeIn(viewState: QuestionViewState) {
        with(viewState) {
            if (txt_question_title != null) {
                txt_question_title.text = title
                GlideApp.with(this@QuestionFragment).load(itemImageUrl)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .onComplete {
                            showButtons()
                            txt_timer.visibility = View.VISIBLE
                            dispatch(UiActions.ProfileImageDidShow())
                        }
                        .into(imageView)
                setButtonText(viewState)
            }
        }
    }

    private fun setButtonText(viewState: QuestionViewState) {
        with(viewState) {
            button1.text = button1Text
            button2.text = button2Text
            button3.text = button3Text
            button4.text = button4Text
        }
    }

    private fun setAllButtonsVisible() {
        button1.visibility = View.VISIBLE
        button2.visibility = View.VISIBLE
        button3.visibility = View.VISIBLE
        button4.visibility = View.VISIBLE
        button1.alpha = 1f
        button2.alpha = 1f
        button3.alpha = 1f
        button4.alpha = 1f
    }

    override fun setTimerText(viewState: QuestionViewState) {
        if (view != null) {
            txt_timer.scaleX = 0f
            txt_timer.scaleY = 0f
            txt_timer.alpha = 1f
            txt_timer.text = viewState.timerText
            txt_timer.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setInterpolator(FastOutSlowInInterpolator())
                    .setDuration(500)
                    .withEndAction {
                        if (txt_timer != null) {
                            txt_timer.animate()
                                    .scaleX(0f)
                                    .scaleY(0f)
                                    .duration = 500
                        }
                    }
        }
    }

    override fun showTimesUp(viewState: QuestionViewState, isEndGame: Boolean) {
        txt_timer.scaleX = 0f
        txt_timer.scaleY = 0f
        txt_timer.text = viewState.timerText
        val restoreColor = txt_timer.currentTextColor
        txt_timer.setTextColor(ResourcesCompat.getColor(context?.resources!!, R.color.red, activity?.theme))
        txt_timer.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setInterpolator(FastOutSlowInInterpolator())
                .setDuration(500)
                .withEndAction {
                    showWrongAnswer(viewState, isEndGame)
                    txt_timer?.animate()?.alpha(0f)
                            ?.withEndAction {
                                txt_timer?.visibility = View.VISIBLE
                                txt_timer?.setTextColor(restoreColor)
                            }
                }
    }

    private fun celebrate() {
        view_konfetti.build()
                .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA)
                .setDirection(0.0, 359.0)
                .setSpeed(1f, 5f)
                .setFadeOutEnabled(true)
                .setTimeToLive(5000L)
                .addShapes(Shape.RECT, Shape.CIRCLE)
                .addSizes(Size(12), Size(16, 6f))
                .setPosition(-50f, view_konfetti.width + 50f, -50f, -50f)
                .burst(200)
    }

    private fun getBtnByNum(num: Int): Button? = when (num) {
        1 -> button1
        2 -> button2
        3 -> button3
        4 -> button4
        else -> null//throw IllegalStateException("Invalid button index")
    }
}