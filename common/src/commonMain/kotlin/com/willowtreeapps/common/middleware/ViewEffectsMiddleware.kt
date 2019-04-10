package com.willowtreeapps.common.middleware

import com.beyondeye.reduks.Store
import com.willowtreeapps.common.*
import com.willowtreeapps.common.ui.QuestionPresenter
import com.willowtreeapps.common.ui.QuestionView
import com.willowtreeapps.common.ui.StartPresenter
import com.willowtreeapps.common.ui.StartView
import kotlin.reflect.KProperty1
import kotlin.reflect.KFunction1
import kotlin.reflect.KFunction2
import kotlin.reflect.KFunction3

typealias ViewEffectsSubscriber = (ViewEffect) -> Unit

/**
 * Middleware that handles visual "side effects".  These are temporary effects on the UI that
 * are not persisted in the app state, i.e. animations and transitions
 * Listens to actions and dispatches "ViewEffects" to subscribers.
 * Subscribe with:
 *      ViewEffectsMiddleware.subscribeToViewEffects(this)
 *
 *  Must be unsubscribed to avoid leaks.
 */
class ViewEffectsMiddleware(private val gameEngine: GameEngine) {


    fun dispatch(store: Store<AppState>, nextDispatcher: (Any) -> Any, action: Any): Any {
//        Cmd.run(PresenterFactory::questPresenter, QuestionPresenter::endGameTapped)
//        Cmd2.run(PresenterFactory::questPresenter, QuestionPresenter::view, QuestionView::hashCode)
//        Cmd2.run(PresenterFactory::startPresenter, StartPresenter::view, StartView::hideLoading)
//        Cmd2.run(PresenterFactory::startPresenter, StartPresenter::view, StartView::showError, "test")

        val result = nextDispatcher(action)
        val viewCmd = store.state.viewCmd
//        if (gameEngine.presenterFactory.hasAttachedViews()) {
//            viewCmd?.execute(gameEngine.presenterFactory)
//            store.state.viewCmd = Cmd2.noOp
//        }
        return result
    }
}


internal class Cmd<TFactory, TPresenter, TArg1> private constructor(private val presenterSelector: KProperty1<TFactory, TPresenter>? = null,
                                                                    val f0: KFunction1<TPresenter, Unit>? = null,
                                                                    val f1: KFunction2<TPresenter, TArg1, Unit>? = null,
                                                                    val arg0: TArg1? = null
) {

    private constructor(presenterSelector: KProperty1<TFactory, TPresenter>? = null,
                        f0: KFunction1<TPresenter, Unit>? = null) : this(presenterSelector, f0, null, null)


    fun execute(presenterFactory: TFactory) {
        val presenter = presenterSelector?.invoke(presenterFactory)

        f1?.invoke(presenter!!, arg0!!)
    }

    companion object {


        fun <TFactory, TPresenter> run(presenterSelector: KProperty1<TFactory, TPresenter>, f: KFunction1<TPresenter, Unit>): Cmd<TFactory, TPresenter, Any> {

            return Cmd(presenterSelector, f)
        }

        fun <TFactory, TPresenter, TArg1> run(presenterSelector: KProperty1<TFactory, TPresenter>, f: KFunction2<TPresenter, TArg1, Unit>, arg1: TArg1): Cmd<TFactory, TPresenter, TArg1> {

            return Cmd(presenterSelector, f1 = f, arg0 = arg1)
        }
    }
}

data class Cmd2<TFactory, TFactory2, TTarget1, TArg1, TArg2> private constructor(private val selector1: KProperty1<TFactory, TFactory2>? = null,
                                                                            private val selector2: KProperty1<TFactory2, TTarget1?>? = null,
                                                                            val f0: KFunction1<TTarget1, *>? = null,
                                                                            val f1: KFunction2<TTarget1, TArg1, *>? = null,
                                                                            val f2: KFunction3<TTarget1, TArg1, TArg2, *>? = null,
                                                                            val arg0: TArg1? = null,
                                                                            val arg1: TArg2? = null
) {

    fun execute(factor1: Any) {
            val factory2 = selector1?.invoke(factor1 as TFactory)

            val target = selector2?.invoke(factory2!!)

            f0?.invoke(target!!)
            f1?.invoke(target!!, arg0!!)
            f2?.invoke(target!!, arg0!!, arg1!!)
    }

    companion object {
        val noOp = Cmd2<Nothing,Nothing,Nothing, Nothing, Nothing>()
        fun <TFactory, TFactory2, TTarget> run(selector: KProperty1<TFactory, TFactory2>,
                                               selector2: KProperty1<TFactory2, TTarget?>,
                                               f: KFunction1<TTarget, *>): Cmd2<TFactory, TFactory2, TTarget, Nothing, Nothing> {

            return Cmd2(selector, selector2, f)
        }

        fun <TFactory, TFactory2, TTarget, TArg1> run(selector: KProperty1<TFactory, TFactory2>,
                                                      selector2: KProperty1<TFactory2, TTarget?>,
                                                      f: KFunction2<TTarget, TArg1, Unit>, arg1: TArg1): Cmd2<TFactory, TFactory2, TTarget, TArg1, Nothing> {

            return Cmd2(selector, selector2, f1 = f, arg0 = arg1)
        }

        fun <TFactory, TFactory2, TTarget, TArg1, TArg2> run(selector: KProperty1<TFactory, TFactory2>,
                                                             selector2: KProperty1<TFactory2, TTarget?>,
                                                             f: KFunction3<TTarget, TArg1, TArg2, *>,
                                                             arg1: TArg1,
                                                             arg2: TArg2): Cmd2<TFactory, TFactory2, TTarget, TArg1, TArg2> {

            return Cmd2(selector, selector2, f2 = f, arg0 = arg1, arg1 = arg2)
        }
    }
}
