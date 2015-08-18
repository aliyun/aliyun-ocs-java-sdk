package com.aliyun.ocs.support.logging;

public interface Log {
    void error(String msg, Throwable e);
    void error(String msg);

    boolean isInfoEnabled();
    void info(String msg);

    boolean isDebugEnabled();
    void debug(String msg);
    void debug(String msg, Throwable e);

    boolean isWarnEnabled();
    void warn(String msg);
    void warn(String msg, Throwable e);
}
