package com.fit2cloud.devops.common.model;

import lombok.Data;

@Data
public class RetValues<T> {
    private boolean success;
    private String  message;
    private T       values;
    public RetValues(boolean success, String  message, T t) {
        this.success = success;
        this.message = message;
        this.values = t;
    }
}
