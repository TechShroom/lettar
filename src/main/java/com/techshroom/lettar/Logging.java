package com.techshroom.lettar;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

public class Logging {
    
    public static Logger getLogger() {
        List<StackTraceElement> stack = Throwables.lazyStackTrace(new Throwable());
        // 0 - this code
        // 1 - invoker (we want this)
        return LoggerFactory.getLogger(stack.get(1).getClassName());
    }

}
