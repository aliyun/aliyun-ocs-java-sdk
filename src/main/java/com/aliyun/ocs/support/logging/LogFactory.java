package com.aliyun.ocs.support.logging;

import java.lang.reflect.Constructor;;

public class LogFactory {

    private static Constructor<?> logConstructor;

    static {
        // 优先选择log4j,而非Apache Common Logging. 因为后者无法设置真实Log调用者的信息
        tryImplementation("org.apache.log4j.Logger", "com.aliyun.ocs.support.logging.Log4jImpl");

        if (logConstructor == null) {
            try {
                logConstructor = NoopLogImpl.class.getConstructor(String.class);
            } catch (Exception e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    private static void tryImplementation(String testClassName, String implClassName) {
        if (logConstructor != null) {
            return;
        }

        try {
            Resources.classForName(testClassName);
            Class<?> implClass = Resources.classForName(implClassName);
            logConstructor = implClass.getConstructor(new Class[] { String.class });

            Class<?> declareClass = logConstructor.getDeclaringClass();
            if (!Log.class.isAssignableFrom(declareClass)) {
                logConstructor = null;
            }

            try {
                if (null != logConstructor) {
                    logConstructor.newInstance(LogFactory.class.getName());
                }
            } catch (Throwable t) {
                logConstructor = null;
            }

        } catch (Throwable t) {
            // skip
        }
    }

    public static Log getLog(Class<?> clazz) {
        return getLog(clazz.getName());
    }

    public static Log getLog(String loggerName) {
        try {
            return (Log) logConstructor.newInstance(loggerName);
        } catch (Throwable t) {
            throw new RuntimeException("Error creating logger for logger '" + loggerName + "'.  Cause: " + t, t);
        }
    }

    public static synchronized void selectLog4JLogging() {
        try {
            Resources.classForName("org.apache.log4j.Logger");
            Class<?> implClass = Resources.classForName("com.aliyun.ocs.support.logging.Log4jImpl");
            logConstructor = implClass.getConstructor(new Class[] { String.class });
        } catch (Throwable t) {
            //ignore
        }
    }
}