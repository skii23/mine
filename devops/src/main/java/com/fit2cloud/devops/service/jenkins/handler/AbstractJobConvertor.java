package com.fit2cloud.devops.service.jenkins.handler;

import com.fit2cloud.devops.service.jenkins.model.common.BaseJobModel;
import org.jdom2.Document;

import java.util.Map;
import java.util.Optional;

public abstract class AbstractJobConvertor<S, T> {

    protected Map<String, AbstractConvertor<S, T>> convertors;
    protected Map<String, Object> contextHolder;
    protected S source;
    protected T target;

    public Map<String, Object> getContextHolder() {
        return contextHolder;
    }

    public void setContextHolder(Map<String, Object> contextHolder) {
        this.contextHolder = contextHolder;
    }

    public AbstractConvertor<S, T> fluentAddConvertor(AbstractConvertor<S, T> convertor) {
        silentAddConvertor(convertor);
        return convertor;
    }

    public AbstractConvertor<S, T> getConvertor(String name) {
        return convertors.get(name);
    }

    public AbstractJobConvertor<S,T> addConvertor(AbstractConvertor<S, T> convertor) {
        silentAddConvertor(convertor);
        return this;
    }

    public void silentAddConvertor(AbstractConvertor<S, T> convertor){
        Optional.ofNullable(convertor.getContextHolder()).ifPresent(holder -> this.contextHolder.putAll(holder));
        convertor.setContextHolder(this.contextHolder);
        this.convertors.put(convertor.getName(), convertor);
    }

    public AbstractJobConvertor<S,T> addContext(String name, Object object) {
        this.contextHolder.put(name, object);
        this.convertors.values().forEach(convertor -> convertor.addContext(name, object));
        return this;
    }

    public void convert() {
        this.convertors.values().forEach(convertor -> convertor.convert(this.source, this.target));
    }

}
