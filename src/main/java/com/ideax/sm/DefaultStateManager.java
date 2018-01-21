package com.ideax.sm;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.MoreExecutors;
import com.ideax.base.Preconditions;
import com.ideax.sm.annotation.Hook;
import com.ideax.sm.annotation.OnTransition;
import com.ideax.sm.annotation.WithStateMachine;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;


/**
 * 默认的状态流转引擎
 *
 * @author xinrd.xu
 * @version 15/11/23
 */
public class DefaultStateManager<S extends State, E extends Action> extends ApplicationObjectSupport implements StateManager<S, E>, InitializingBean, BeanNameAware {

    private final static Logger logger = LoggerFactory.getLogger(DefaultStateManager.class);

    private Executor hookExecutor = MoreExecutors.directExecutor();

    private TransactionOperations transactionTemplate;

    /**
     * 状态的对应关系
     */
    private Map<S, StateNode<S, E>> stateMapping = new LinkedHashMap<>();
    private StateNode<S, E> initState = new StateNode<>(null);

    private Class<? extends Enum> stateClass;
    private Class<? extends Enum> actionClass;
    private String beanName;

    private static List<String> MANAGER_NAMES = Lists.newArrayList();

    public DefaultStateManager() {
    }

    public Object updateState(final StateContext<S, E> context) {
        final Transition<S, E> transition;
        if (context.getSource() == null && initState.getTransitions().containsKey(context.getEvent())) {
            transition = initState.getTransitions().get(context.getEvent());
            Preconditions.checkNotNull(transition, "[%s]不是初始操作", context.getEvent().getText());
        } else {
            StateNode<S, E> sourceState = stateMapping.get(context.getSource());
            Preconditions.checkNotNull(sourceState, "未知的状态[%s]", context.getSource());
            transition = sourceState.getTransitions().get(context.getEvent());
            Preconditions.checkNotNull(transition, "[%s]操作无法作用于状态[%s]", context.getEvent().getText(), context.getSource().getText());
        }
        context.setTarget(transition.getTarget());

        // pre interceptor
        // guardian
        // handle
        Object result = null;
        if (transition.getHandler() != null) {
            if (transactionTemplate != null) {
                result = transactionTemplate.execute(status -> transition.getHandler().handle(context));
            } else {
                result = transition.getHandler().handle(context);
            }
        }
        // hook
        context.setCurrentStage(StateContext.Stage.EXEC_HOOKS);


        for (final TransitionHook<S, E> hook : transition.getHooks()) {
            hookExecutor.execute(() -> {
                try {
                    hook.onTransited(context);
                } catch (Exception e) {
//                        QMonitor.recordOne(MonitorConstants.STATE_HOOK_EXECUTE_FAIL);
                    logger.error("SM: error when executing hook [{}], on context [{}]", hook, context, e);
                    if (hook instanceof RecoverableTransitionHook) {
                        try {
                            ((RecoverableTransitionHook<S, E>) hook).store(context);
                            logger.error("SM: but fortunately , task was backed up by store()");
                        } catch (Exception ex) {
                            logger.error("SM: the worse thing is , [{}] store() failed", hook, ex);
                        }
                    }
                }
            });
        }

        return result;
    }


    /**
     * 初始状态
     *
     * @param action
     * @param toState
     */
    public Transition<S, E> init(E action, S toState) {
        StateNode<S, E> toStateNode = stateMapping.get(toState);
        if (toStateNode == null) {
            toStateNode = new StateNode<>(toState);
            stateMapping.put(toState, toStateNode);
        }
        Transition<S, E> transition = new Transition<>(toState);
        initState.getTransitions().put(action, transition);
        return transition;
    }

    /**
     * 增加转换
     *
     * @param action
     * @param fromState
     * @param toState
     * @return
     */
    public Transition<S, E> addTransition(E action, S fromState, S toState) {
        // 初始状态
        StateNode<S, E> fromStateNode = stateMapping.get(fromState);
        if (fromStateNode == null) {
            fromStateNode = new StateNode<>(fromState);
            stateMapping.put(fromState, fromStateNode);
        }

        StateNode<S, E> toStateNode = stateMapping.get(toState);
        if (toStateNode == null) {
            toStateNode = new StateNode<>(toState);
            stateMapping.put(toState, toStateNode);
        }
        Transition<S, E> transition = new Transition<S, E>(toState);
        fromStateNode.getTransitions().put(action, transition);
        return transition;
    }

    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("Scanning state machine handler in application context: " + getApplicationContext());
        long t1 = System.nanoTime();

        // find a sample data, and getClass at runtime
        StateNode<S, E> next = stateMapping.values().iterator().next();
        S stateSample = next.getState();
        Preconditions.checkArgument(Enum.class.isAssignableFrom(stateSample.getClass()), "目前只支持枚举类型的state用注解配置");
        stateClass = (Class<? extends Enum>) stateSample.getClass();
        E actionSample = next.getTransitions().keySet().iterator().next();
        Preconditions.checkArgument(Enum.class.isAssignableFrom(actionSample.getClass()), "目前只支持枚举类型的action用注解配置");
        actionClass = (Class<? extends Enum>) actionSample.getClass();

        // 遍历所有bean处理@OnTransition @Hook
        String[] beanNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(getApplicationContext(), Object.class);
        for (String beanName : beanNames) {
            Class<?> type = getApplicationContext().getType(beanName);
            if (isHandler(type)) {
                detectHookBean(beanName, type);
                detectHandlerMethods(beanName);
            }
        }

        logger.info("End Scanning state machine handler in application context: {} cost: {}ns", getApplicationContext(), System.nanoTime() - t1);

        logger.info("-----------------statemachine----------------\n{}", drawStateTree());
        logger.info("transaction template {}", transactionTemplate);

        MANAGER_NAMES.add(beanName);
    }

    private String drawStateTree() {
        StringBuilder sb = new StringBuilder();
        sb.append("├──[").append("--").append("]\n");
        drawTransition(sb, initState, true);
        for (Iterator<StateNode<S, E>> iterator1 = stateMapping.values().iterator(); iterator1.hasNext(); ) {
            StateNode<S, E> en = iterator1.next();
            S state = en.getState();
            boolean isNotLast1 = iterator1.hasNext();
            sb.append(isNotLast1 ? "├──" : "└──");
            sb.append('[').append(state.getText()).append('(').append(state).append(")]\n");
            drawTransition(sb, en, isNotLast1);
        }
        return sb.toString();
    }

    private void drawTransition(StringBuilder sb, StateNode<S, E> en, boolean isNotLast1) {
        for (Iterator<Map.Entry<E, Transition<S, E>>> iterator2 = en.getTransitions().entrySet().iterator(); iterator2.hasNext(); ) {
            Map.Entry<E, Transition<S, E>> transition = iterator2.next();
            Transition<S, E> value = transition.getValue();
            E action = transition.getKey();
            S target = value.getTarget();
            sb.append(isNotLast1 ? "│  " : "   ");
            sb.append(iterator2.hasNext() ? "├──" : "└──");
            sb.append('[').append(action.getText()).append('(').append(action).append(")=>").append(target.getText()).append('(').append(target).append(")]\n");
            sb.append(isNotLast1 ? "│  " : "   ");
            sb.append(iterator2.hasNext() ? "│  " : "   ");
            sb.append("├──[HANDLER:").append(value.getHandler()).append("]\n");
            for (Iterator<TransitionHook<S, E>> iterator3 = value.
                    getHooks().iterator(); iterator3.hasNext(); ) {
                TransitionHook<S, E> hook = iterator3.next();
                sb.append(isNotLast1 ? "│  " : "   ");
                sb.append(iterator2.hasNext() ? "│  " : "   ");
                sb.append(iterator3.hasNext() ? "├──" : "└──");
                sb.append("[HOOK:").append(hook.getOrder()).append(':').append(hook).append("]\n");
            }
        }
    }

    private void detectHookBean(String beanName, Class<?> type) {
        Hook anno = AnnotationUtils.findAnnotation(type, Hook.class);
        if (anno == null || !TransitionHook.class.isAssignableFrom(type)) {
            return;
        }
        final Object hook = getApplicationContext().getBean(beanName);
        for (Transition<S, E> transition : getMatchedTransition(anno.source(), anno.action(), anno.target())) {
            transition.addHook((TransitionHook) hook);
            logger.info("添加hook source=[{}] action=[{}] handler=[{}]", anno.source(), anno.action(), hook);
        }

    }

    private List<Transition<S, E>> getMatchedTransition(String[] sources, String[] actions, String[] targets) {
        List<Transition<S, E>> result = Lists.newArrayList();
        // 判断init state
        if (sources.length == 0) {
            result.addAll(getMatchingTransitionByActionAndTarget(actions, targets, initState));
        }

        for (StateNode<S, E> en : stateMapping.values()) {
            if (sources.length > 0
                    && (!containsEnum(stateClass, sources, en.getState()))) {
                continue;
            }
            result.addAll(getMatchingTransitionByActionAndTarget(actions, targets, en));
        }
        return result;
    }

    private List<Transition<S, E>> getMatchingTransitionByActionAndTarget(String[] actions, String[] targets, StateNode<S, E> en) {
        List<Transition<S, E>> result = Lists.newArrayList();
        for (Map.Entry<E, Transition<S, E>> transition : en.getTransitions().entrySet()) {
            if (actions.length > 0
                    && (!containsEnum(actionClass, actions, transition.getKey()))) {
                continue;
            }

            if (targets.length > 0
                    && (!containsEnum(stateClass, targets, transition.getValue().getTarget()))) {
                continue;
            }
            result.add(transition.getValue());
        }
        return result;
    }


    private void detectHandlerMethods(final String handler) {
        Class<?> handlerType = (handler instanceof String) ?
                getApplicationContext().getType((String) handler) : handler.getClass();
        final Object handlerObject = getApplicationContext().getBean(handler);
        final Class<?> userType = ClassUtils.getUserClass(handlerType);


        ReflectionUtils.doWithMethods(userType, new ReflectionUtils.MethodCallback() {
            @Override
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                // handler 是严格匹配 source action的
                OnTransition tAnno = AnnotationUtils.findAnnotation(method, OnTransition.class);
                if (tAnno != null) {
                    for (Transition<S, E> transition : getMatchedTransition(tAnno.source(), tAnno.action(), tAnno.target())) {
                        Preconditions.checkArgument(transition.getHandler() == null, "重复的handler %s 和 %s", transition.getHandler(), method);
                        transition.setHandler(new MethodTransitionHandler<S, E>(handlerObject, method));
                        logger.info("添加handler source=[{}] action=[{}] handler=[{}]", tAnno.source(), tAnno.action(), method);
                    }
                }

                // hook source或action为空表示全匹配
                Hook hAnno = AnnotationUtils.findAnnotation(method, Hook.class);
                if (hAnno != null) {
                    for (Transition<S, E> transition : getMatchedTransition(hAnno.source(), hAnno.action(), hAnno.target())) {
                        transition.addHook(new MethodTransitionHook<>(handlerObject, method, hAnno.order()));
                        logger.info("添加hook source=[{}] action=[{}] hook=[{}]", hAnno.source(), hAnno.action(), method);
                    }
                }
            }
        });
    }

    private boolean containsEnum(Class<? extends Enum> stateClass, String[] sources, Object state) {
        for (String source : sources) {
            if (ObjectUtils.equals(Enum.valueOf(stateClass, source), state)) {
                return true;
            }
        }
        return false;
    }

    private boolean isHandler(Class<?> beanType) {
        WithStateMachine annotation = AnnotationUtils.findAnnotation(beanType, WithStateMachine.class);
        if (annotation != null) {
            Object bean = getApplicationContext().getBean(annotation.name());
            if (bean == null) {
                throw new IllegalArgumentException("state machine doesn't exist : " + annotation.name() + " on class " + beanType);
            }
            return bean == this;
        }
        return false;
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }
}
