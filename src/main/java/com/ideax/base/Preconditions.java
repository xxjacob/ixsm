package com.ideax.base;

import com.ideax.exception.MonitorAwareException;
import com.ideax.exception.FatalException;
import com.ideax.exception.IgnorableException;

import java.util.Collection;

/**
 * fatal : GlobalExceptionHandler会打监控 日志级别是ERROR
 * ignore : GlobalExceptionHandler不打监控 日志级别是WARN
 */
public class Preconditions {

    public static final Precondition fatal = new Precondition(new Precondition.ExceptionBuilder() {
        public MonitorAwareException newException() {
            return new FatalException();
        }

        public MonitorAwareException newException(String msg) {
            return new FatalException(msg);
        }

        public MonitorAwareException newException(String msg, Throwable cause) {
            return new FatalException(msg, cause);
        }
    });

    public static final Precondition ignore = new Precondition(new Precondition.ExceptionBuilder() {
        public MonitorAwareException newException() {
            return new IgnorableException();
        }

        public MonitorAwareException newException(String msg) {
            return new IgnorableException(msg);
        }

        public MonitorAwareException newException(String msg, Throwable cause) {
            return new IgnorableException(msg, cause);
        }
    });


    // -------------------------static------------------------

    public static void checkArgument(boolean arg0, Object arg1) {
        Preconditions.fatal.checkArgument(arg0, arg1);
    }

    public static void checkArgument(boolean arg0) {
        Preconditions.fatal.checkArgument(arg0);
    }

    public static void checkArgument(boolean arg0, String arg1, Object... arg2) {
        Preconditions.fatal.checkArgument(arg0, arg1, arg2);
    }

    public static void checkNotBlank(String arg0, String arg1, Object... arg2) {
        Preconditions.fatal.checkNotBlank(arg0, arg1, arg2);
    }

    public static void checkNotEmpty(Collection arg0, String arg1, Object... arg2) {
        Preconditions.fatal.checkNotEmpty(arg0, arg1, arg2);
    }

    public static void checkState(boolean arg0, String arg1, Object... arg2) {
        Preconditions.fatal.checkState(arg0, arg1, arg2);
    }

    public static void checkState(boolean arg0) {
        Preconditions.fatal.checkState(arg0);
    }

    public static void checkState(boolean arg0, Object arg1) {
        Preconditions.fatal.checkState(arg0, arg1);
    }

    public static void checkNotNull(Object arg0, String arg1, Object... arg2) {
        Preconditions.fatal.checkNotNull(arg0, arg1, arg2);
    }

    public static void checkPositionIndexes(int arg0, int arg1, int arg2) {
        Preconditions.fatal.checkPositionIndexes(arg0, arg1, arg2);
    }

    public static int checkElementIndex(int arg0, int arg1, String arg2) {
        return Preconditions.fatal.checkElementIndex(arg0, arg1, arg2);
    }

    public static int checkElementIndex(int arg0, int arg1) {
        return Preconditions.fatal.checkElementIndex(arg0, arg1);
    }

    public static int checkPositionIndex(int arg0, int arg1) {
        return Preconditions.fatal.checkPositionIndex(arg0, arg1);
    }

    public static int checkPositionIndex(int arg0, int arg1, String arg2) {
        return Preconditions.fatal.checkPositionIndex(arg0, arg1, arg2);
    }

}
