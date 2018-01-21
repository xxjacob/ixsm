package com.ideax.sm;

/**
 * 可重试的任务，提供store接口，落地任务，失败补救
 *
 * @author xinrd.xu
 * @version 16/1/13
 */
public interface RecoverableTransitionHook<S, E> extends TransitionHook {

    void store(StateContext<S, E> context);
}
