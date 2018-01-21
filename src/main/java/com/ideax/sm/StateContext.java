package com.ideax.sm;

import java.util.HashMap;
import java.util.Map;

import lombok.ToString;

/**
 * 状态发生变迁时 共享的上下文, 供Action Listener使用
 *
 * @author xinrd.xu
 * @version 16/1/11
 */
@ToString
public class StateContext<S, E> {

    /**
     * 事件
     */
    private E event;

    /**
     * 来源状态
     */
    private S source;

    /**
     * 目标状态
     */
    private S target;

    /**
     * 当前状态
     */
    private Stage currentStage = Stage.INIT;

    /**
     * 附件
     */
    private Map<String, Object> attachment = new HashMap<>();

    public StateContext(E event, S source) {
        this.event = event;
        this.source = source;
    }

    /**
     * 获取上下文中的信息
     *
     * @param key
     * @return
     */
    public Object getAttribute(String key) {
        return attachment.get(key);
    }

    /**
     * 获取上下文中的信息
     *
     * @param key
     * @return
     */
    public Object putAttribute(String key, Object value) {
        return attachment.put(key, value);
    }

    public E getEvent() {
        return event;
    }

    public S getSource() {
        return source;
    }

    public Stage getCurrentStage() {
        return currentStage;
    }

    void setCurrentStage(Stage currentStage) {
        this.currentStage = currentStage;
    }

    public S getTarget() {
        return target;
    }

    public void setTarget(S target) {
        this.target = target;
    }

    public <T> T getAttribute(String key, Class<T> clz) {
        Object attribute = getAttribute(key);
        if (attribute == null) {
            return null;
        }
        if (clz.isAssignableFrom(attribute.getClass())) {
            return (T) attribute;
        }
        throw new IllegalStateException("Context中类型不匹配 required:" + clz.getName() + " actual:" + attribute.getClass().getName());
    }


    /**
     * Enumeration of possible stages context is attached.
     * INIT --[state action check]--> EVENT_NOT_ACCEPTED
     * INIT --[guard succ]--> EVENT_NOT_ACCEPTED
     */
    public static enum Stage {
        INIT,
        /**
         * state&action not match
         */
        EVENT_NOT_ACCEPTED,
        GUARDING,
        GUARDIAN_REJECT,
        INTERCEPTING,
        TRANSITING,
        EXEC_HOOKS
    }

}
