package com.ideax.sm;


/**
 * 订单状态管理
 *
 * @author zhongyuan.zhang
 */
public interface StateManager<S, E> {

    /**
     * 订单状态变更
     *
     * @return
     */
    Object updateState(StateContext<S, E> context);

}
