package com.ideax.sm;

/**
 * Created by chunhai.wang on 2016/7/19.
 */
public class StateContextFactory {

    public static <S, E> StateContext<S, E> create(S source, E event) {
        return new StateContext<>(event, source);
    }
}
