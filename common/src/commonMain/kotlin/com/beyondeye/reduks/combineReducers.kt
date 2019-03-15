package com.beyondeye.reduks

fun <S> combineReducers(vararg reducers: Reducer<S>): Reducer<S> {
    return ReducerFn<S> { state: S, action: Any ->
        reducers.fold(state, { s, reducer -> reducer(s, action) })
//        var state = state
//        for (reducer in reducers) {
//            state = reducer.reduce(state, action)
//        }
//        state
    }
}

fun <S> Reducer<S>.combinedWith(vararg reducers: Reducer<S>):Reducer<S> {
    return ReducerFn<S> { state:S, action:Any ->
        val sAfterFirstReducer= this(state,action)
        reducers.fold(sAfterFirstReducer, { s, reducer -> reducer(s, action) })
    }
}

/**
 * combine two reducer with + operator
 */
operator fun <S> Reducer<S>.plus(other:Reducer<S>):Reducer<S> = ReducerFn { s, a ->
   other( this(s,a),a)
}