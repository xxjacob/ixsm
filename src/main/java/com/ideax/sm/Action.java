/*
 *$Id$
 *Copyright (c) 2011 Qunar.com. All Rights Reserved.
 */
package com.ideax.sm;

/**
 * 订单处理请求类型<br>
 * 用于状态机变迁。 不同的状态机动作需要继承于该类。
 *
 * @author jiaqiang.yan
 */
public interface Action {
    String getText();
}
