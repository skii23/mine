package com.fit2cloud.devops.common.consts;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StatusConstants {
    public static final String PENDING = "pending";
    public static final String RUNNING = "running";
    public static final String SUCCESS = "success";
    public static final String FAIL = "fail";
    public static final String ERROR = "error";
    public static final String TIMEOUT = "timeout";
    public static final String VALID = "valid";
    public static final String INVALID = "invalid";


    public static boolean isCompleted(String status) {
        return Arrays.asList(FAIL,SUCCESS, ERROR, TIMEOUT).contains(status);
    }


}






