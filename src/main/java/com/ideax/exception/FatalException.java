/*
* Copyright (c) 2014 Qunar.com. All Rights Reserved.
*/
package com.ideax.exception;

/**
 * 需要报警的异常
 * 携带了errorCode和errorMsg, 用于返回api result
 */
public class FatalException extends MonitorAwareException {
    public FatalException() {
        super();
    }

    public FatalException(String message) {
        super(message);
    }

    public FatalException(String msgTemplate, Object... args) {
        super(msgTemplate, args);
    }

    public FatalException(String message, Throwable cause) {
        super(message, cause);
    }

    public FatalException(String msgTemplate, Throwable cause, Object... args) {
        super(msgTemplate, cause, args);
    }

    public FatalException(Throwable cause) {
        super(cause);
    }
}