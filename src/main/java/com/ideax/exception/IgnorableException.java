/*
* Copyright (c) 2014 Qunar.com. All Rights Reserved.
*/
package com.ideax.exception;

/**
 * 不需要报警的异常, 只是为了结合 Precondition 便利处理业务判断逻辑
 * 携带了errorCode和errorMsg, 用于返回api result
 */
public class IgnorableException extends MonitorAwareException {
    public IgnorableException() {
        super();
    }

    public IgnorableException(String message) {
        super(message);
    }

    public IgnorableException(String msgTemplate, Object... args) {
        super(msgTemplate, args);
    }

    public IgnorableException(String message, Throwable cause) {
        super(message, cause);
    }

    public IgnorableException(String msgTemplate, Throwable cause, Object... args) {
        super(msgTemplate, cause, args);
    }

    public IgnorableException(Throwable cause) {
        super(cause);
    }
}