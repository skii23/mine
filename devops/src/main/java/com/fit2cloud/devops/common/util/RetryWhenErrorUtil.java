package com.fit2cloud.devops.common.util;

import java.util.concurrent.TimeUnit;

/**
 * @author caiwzh
 * @date 2022/8/26
 */
public class RetryWhenErrorUtil {

    public static <T> T execute(int times, int interval, Function<T> supplier) throws Exception {
        Exception lastException = null;
        for (int i = 0; i < times; i++) {
            try {
                return supplier.get();
            } catch (Exception e) {
                lastException = e;
                TimeUnit.SECONDS.sleep(interval);
            }
        }
        throw lastException;
    }

    @FunctionalInterface
    public interface Function<T> {
        T get() throws Exception;
    }
}
