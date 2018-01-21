package com.ideax.sm;

import java.lang.reflect.Method;

/**
 * 代理 @OnTransientEnd 方法，支持一些参数的自动注入
 *
 * @author xinrd.xu
 * @version 16/1/29
 */
public class MethodTransitionHook<S, E> implements TransitionHook<S, E> {

    private MethodWrapper<S, E> methodWrapper;
    private int order;

    public MethodTransitionHook(Object object, Method method, int order) {
        methodWrapper = new MethodWrapper<>(object, method);
        this.order = order;
    }

    @Override
    public void onTransited(StateContext<S, E> context) {
        methodWrapper.invoke(context);
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public String toString() {
        Method method = methodWrapper.getMethod();
        return method.getDeclaringClass().getName() + "#" + method.getName();
    }
}
