package com.fit2cloud.devops.service.jenkins.model.maven;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("localRepository")
public class LocalRepository {
    @XStreamAsAttribute
    @XStreamAlias("class")
    private String classStr;

    public String getClassStr() {
        return classStr;
    }

    public void setClassStr(String classStr) {
        this.classStr = classStr;
    }
}
