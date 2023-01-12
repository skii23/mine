package com.fit2cloud.devops.service.jenkins.handler;

public interface Hook<T> {
    void apply(T t);
}
