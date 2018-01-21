package com.ideax.sm;

import org.springframework.core.Ordered;

/**
 * 变迁完成后的 非主流程操作
 *
 * @author xinrd.xu
 * @version 16/1/13
 */
public interface TransitionHook<S, E> extends Ordered {

    void onTransited(StateContext<S, E> context);
}
