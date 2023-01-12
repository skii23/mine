package com.fit2cloud.devops.common.util;

import com.fit2cloud.commons.utils.LogUtil;
import com.fit2cloud.devops.common.model.RetValues;
import com.fit2cloud.commons.utils.ResultHolder;


public class RetUtils {
    private RetUtils() {

    }
    public static <T> RetValues error(String logMsg, T values) {
        LogUtil.error(logMsg);
        return new RetValues<T>(false, logMsg, values);
    }

    public static  RetValues<?> error(String logMsg) {
        LogUtil.error(logMsg);
        return new RetValues<>(false, logMsg, null);
    }

    public static <T> RetValues success(T values) {
        return new RetValues<T>(true, "I'm fine!", values);
    }
    public static <T> RetValues success(String logMsg, T values) {
        return new RetValues<T>(true, logMsg, values);
    }
    public static RetValues<?> success() {
        return new RetValues<>(true, "I'm fine!", null);
    }

    public static <T> ResultHolder convert(RetValues<T> r) {
        if (r.isSuccess()) {
            return ResultHolder.success(r.getValues());
        }
        return ResultHolder.error(r.getMessage(), r.getValues());
    }
}
