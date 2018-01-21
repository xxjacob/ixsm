package com.ideax.sm;


import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

import lombok.ToString;

/**
 * 转换
 *
 * @author xinrd.xu
 * @version 16/1/13
 */
@ToString
public class Transition<S, E> {

    /**
     * 目标状态
     */
    S target;

    /**
     * 转化处理类
     */
    private TransitionHandler<S, E> handler;

    /**
     * 状态转换的钩子
     */
    private TreeSet<TransitionHook<S, E>> hooks = new TreeSet<>(new Comparator<TransitionHook<S, E>>() {
        // WARN : never equal, consistent
        @Override
        public int compare(TransitionHook<S, E> o1, TransitionHook<S, E> o2) {
            if (o1.equals(o2)) {
                return 0;
            }
            int ret = Integer.compare(o1.getOrder(), o2.getOrder());
            if (ret == 0) {
                return o1.toString().compareTo(o2.toString());
            }
            return ret;
        }
    });

    public Transition(S target) {
        this.target = target;
    }

    public S getTarget() {
        return target;
    }

    public void setTarget(S target) {
        this.target = target;
    }

    public TransitionHandler<S, E> getHandler() {
        return handler;
    }

    public void setHandler(TransitionHandler<S, E> handler) {
        this.handler = handler;
    }

    public Collection<TransitionHook<S, E>> getHooks() {
        return hooks;
    }

    public void addHook(TransitionHook<S, E> hook) {
        hooks.add(hook);
    }
}
