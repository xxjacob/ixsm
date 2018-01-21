package com.ideax.sm;

/**
 * Created with IntelliJ IDEA.
 * User: jiaqiangyan
 * Date: 1/6/14
 * Time: 5:08 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 * 所有订单状态机枚举接口
 */
public interface State {

    int code();

    /**
     * 状态名称描述
     */
    String getText();

}
