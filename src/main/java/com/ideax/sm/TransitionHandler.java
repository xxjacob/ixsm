package com.ideax.sm;

/**
 * 状态变更的主要业务
 *
 * @author xinrd.xu
 * @version 16/1/13
 */
public interface TransitionHandler<S, E> {

    Object handle(StateContext<S, E> context);
}
