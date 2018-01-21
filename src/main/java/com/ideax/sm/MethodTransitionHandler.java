package com.ideax.sm;

import java.lang.reflect.Method;

/**
 * 代理 @OnTransient 方法，支持一些参数的自动注入
 *
 * @author xinrd.xu
 * @version 16/1/29
 */
public class MethodTransitionHandler<S, E> implements TransitionHandler<S, E> {

    private MethodWrapper<S, E> methodWrapper;

    public MethodTransitionHandler(Object object, Method method) {
        methodWrapper = new MethodWrapper<>(object, method);
    }

    @Override
    public Object handle(StateContext<S, E> context) {
        return methodWrapper.invoke(context);
    }

    @Override
    public String toString() {
        Method method = methodWrapper.getMethod();
        return method.getDeclaringClass().getName() + "#" + method.getName();
    }
}
