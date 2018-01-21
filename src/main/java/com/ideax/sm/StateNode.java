package com.ideax.sm;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 状态的节点
 *
 * @author xinrd.xu
 * @version 16/1/13
 */
public class StateNode<S, E> {

    /**
     * 状态
     */
    private S state;

    /**
     * 转变
     */
    private Map<E, Transition<S, E>> transitions = new LinkedHashMap<>();

    public StateNode(S state) {
        this.state = state;
    }

    public S getState() {
        return state;
    }

    public void setState(S state) {
        this.state = state;
    }

    public Map<E, Transition<S, E>> getTransitions() {
        return transitions;
    }

    public void setTransitions(Map<E, Transition<S, E>> transitions) {
        this.transitions = transitions;
    }

}
