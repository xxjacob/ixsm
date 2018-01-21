package com.ideax.sm;

import java.util.HashMap;
import java.util.Map;

import com.ideax.base.Preconditions;

/**
 * 方便构建初始状态执行上下文
 *
 * @author xinrd.xu
 * @version 16/1/13
 */
public class StateContextBuilder<S, E> {

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
     * 附件
     */
    private Map<String, Object> attachment = new HashMap<>();

    public StateContextBuilder<S, E> source(S source) {
        Preconditions.checkNotNull(source, "原始状态不能为空");
        this.source = source;
        return this;
    }

    public StateContextBuilder<S, E> target(S target) {
        Preconditions.checkNotNull(target, "目标状态不能为空");
        this.target = target;
        return this;
    }

    public StateContextBuilder<S, E> event(E event) {
        Preconditions.checkNotNull(event, "动作不合法");
        this.event = event;
        return this;
    }

    public StateContextBuilder<S, E> addAttribute(String key, Object value) {
        this.attachment.put(key, value);
        return this;
    }

    public StateContext<S, E> build() {
        StateContext<S, E> seStateContext = new StateContext<S, E>(event, source);
        for (Map.Entry<String, Object> en : attachment.entrySet()) {
            seStateContext.putAttribute(en.getKey(), en.getValue());
        }
        return seStateContext;
    }

}
