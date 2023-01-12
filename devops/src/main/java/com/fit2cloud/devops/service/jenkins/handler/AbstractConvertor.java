package com.fit2cloud.devops.service.jenkins.handler;

import java.util.*;

public abstract class AbstractConvertor<S, T> {

    protected Map<String, String> convertMap;
    private String name;
    private Map<String, Object> contextHolder;
    private List<Hook<Map<String, Object>>> preConvertHooks;
    private List<Hook<Map<String, Object>>> postConvertHooks;


    public AbstractConvertor() {
        this.convertMap = new HashMap<>();
    }

    public AbstractConvertor(String name) {
        this.name = name;
        this.convertMap = new HashMap<>();
    }

    public AbstractConvertor(String name, String sourceName) {
        this.name = name;
        this.convertMap = new HashMap<>();
        this.convertMap.put(sourceName, sourceName);
    }

    public AbstractConvertor(String name, String sourceName, String targetName) {
        this.name = name;
        this.convertMap = new HashMap<>();
        this.convertMap.put(sourceName, targetName);
    }

    public void convert(S source, T target) {
        Optional.ofNullable(this.preConvertHooks).ifPresent(hooks -> hooks.forEach(hook -> hook.apply(this.contextHolder)));
        doConvert(source, target);
        Optional.ofNullable(this.postConvertHooks).ifPresent(hooks -> hooks.forEach(hook -> hook.apply(this.contextHolder)));
    }

    public abstract void doConvert(S source, T target);

    public AbstractConvertor<S, T> addPreHook(Hook<Map<String, Object>> hook) {
        if (this.preConvertHooks == null) {
            this.preConvertHooks = new ArrayList<>();
        }
        this.preConvertHooks.add(hook);
        return this;
    }

    public AbstractConvertor<S, T> addPostHook(Hook<Map<String, Object>> hook) {
        if (this.postConvertHooks == null) {
            this.postConvertHooks = new ArrayList<>();
        }
        this.postConvertHooks.add(hook);
        return this;
    }

    public <V> V done(V v) {
        return v;
    }

    public AbstractConvertor<S, T> addMap(String sourceName, String targetName) {
        this.convertMap.put(sourceName, targetName);
        return this;
    }

    public AbstractConvertor<S, T> addMap(String sourceName) {
        this.convertMap.put(sourceName, sourceName);
        return this;
    }

    public AbstractConvertor<S, T> addContext(String name, Object object) {
        if (this.contextHolder == null) {
            this.contextHolder = new HashMap<>();
        }
        this.contextHolder.put(name, object);
        return this;
    }

    public AbstractConvertor<S, T> setContextHolder(Map<String, Object> contextHolder) {
        this.contextHolder = contextHolder;
        return this;
    }

    public Map<String, Object> getContextHolder() {
        return contextHolder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
