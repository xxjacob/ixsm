package com.ideax.exception;

/**
 * @author xinrd.xu
 * @version 16/8/8
 */
public abstract class MonitorAwareException extends RuntimeException {

    static final long serialVersionUID = 1L;

    private int errorCode = 0;

    private String monitorKey;

    private boolean exposeMsg;

    public MonitorAwareException() {
    }

    public MonitorAwareException(String message) {
        super(message);
    }

    public MonitorAwareException(String msgTemplate, Object... args) {
        super(String.format(msgTemplate, args));
    }

    public MonitorAwareException(String message, Throwable cause) {
        super(message, cause);
    }

    public MonitorAwareException(String msgTemplate, Throwable cause, Object... args) {
        super(String.format(msgTemplate, args), cause);
    }

    public MonitorAwareException(Throwable cause) {
        super(cause);
    }

    public MonitorAwareException errorCode(int errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public MonitorAwareException monitorKey(String monitorKey) {
        this.monitorKey = monitorKey;
        return this;
    }

    public String getMonitorKey() {
        return monitorKey;
    }

    public MonitorAwareException exposeMsg() {
        this.exposeMsg = true;
        return this;
    }

    public boolean isExposeMsg() {
        return exposeMsg;
    }
}
