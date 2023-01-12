package com.fit2cloud.devops.service.jenkins.model.common.properties.discarder.strategy;

import com.fit2cloud.devops.service.jenkins.model.AbstractBaseModel;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class BaseStrategyModel extends AbstractBaseModel {
    @XStreamAsAttribute
    @XStreamAlias("class")
    protected String classStr;

    public String getClassStr() {
        return classStr;
    }

    public void setClassStr(String classStr) {
        this.classStr = classStr;
    }
}
