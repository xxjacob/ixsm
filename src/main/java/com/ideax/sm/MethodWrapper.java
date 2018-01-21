package com.ideax.sm;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import com.google.common.collect.Lists;
import com.ideax.exception.FatalException;
import com.ideax.sm.annotation.Attr;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

/**
 * 代理 @OnTransient 方法，支持一些参数的自动注入
 *
 * @author xinrd.xu
 * @version 16/1/29
 */
public class MethodWrapper<S, E> {

    private static final Logger logger = LoggerFactory.getLogger(MethodWrapper.class);

    private static ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

    private Object object;
    private Method method;
    private List<Argument> arguments;

    public MethodWrapper(Object object, Method method) {
        this.object = object;
        this.method = method;
        arguments = Lists.newArrayList();
        Class<?>[] parameterTypes = method.getParameterTypes();
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < parameterTypes.length; i++) {
            Argument arg = new Argument();
            Class<?> parameterType = parameterTypes[i];
            arg.setClz(parameterType);
            if (parameterAnnotations != null && parameterAnnotations[i] != null) {
                Annotation[] parameterAnnotation = parameterAnnotations[i];
                for (Annotation annotation : parameterAnnotation) {
                    if (annotation.annotationType().equals(Attr.class)) {
                        String value = ((Attr) annotation).value();
                        arg.setName(value);
                        arg.setAnnotation(annotation);
                    }
                }
            }
            if (StringUtils.isBlank(arg.getName())) {
                if (parameterNames != null) {
                    arg.setName(parameterNames[i]);
                }
            }
            arguments.add(arg);
        }
    }

    public Object invoke(StateContext<S, E> context) {

        try {
            if (CollectionUtils.isNotEmpty(arguments)) {
                return method.invoke(object, resolveArgument(context));
            } else {
                return method.invoke(object);
            }
        } catch (IllegalAccessException e) {
            logger.error("invoke method [{}] handle error on [{}]", method, object);
            throw new FatalException(e);
        } catch (InvocationTargetException e) {
            logger.error("invoke method [{}] handle error on [{}]", method, object);
            // 我们的Handler不声明抛异常
            Throwable target = e.getTargetException();
            if (target instanceof RuntimeException) {
                throw (RuntimeException) target;
            } else {
                throw new FatalException(target);
            }
        }
    }

    private Object[] resolveArgument(StateContext<S, E> context) {
        Object[] objects = new Object[arguments.size()];
        for (int i = 0; i < arguments.size(); i++) {
            Argument arg = arguments.get(i);
            if (arg.getClz().equals(StateContext.class)) {
                objects[i] = context;
            } else {
                objects[i] = context.getAttribute(arg.getName(), arg.getClz());
            }
        }
        return objects;
    }

    static class Argument {
        private Class clz;
        private Annotation annotation;
        private String name;

        public Argument() {
        }

        public Argument(Class clz, Annotation annotation, String name) {
            this.clz = clz;
            this.annotation = annotation;
            this.name = name;
        }

        public Class getClz() {
            return clz;
        }

        public void setClz(Class clz) {
            this.clz = clz;
        }

        public Annotation getAnnotation() {
            return annotation;
        }

        public void setAnnotation(Annotation annotation) {
            this.annotation = annotation;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public Method getMethod() {
        return method;
    }

}
