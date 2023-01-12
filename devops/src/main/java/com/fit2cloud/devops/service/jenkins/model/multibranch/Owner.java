package com.fit2cloud.devops.service.jenkins.model.multibranch;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * @author caiwzh
 * @date 2022/8/19
 */
@XStreamAlias("owner")
public class Owner {
    @XStreamAlias("class")
    @XStreamAsAttribute
    private String clazz= "org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject";

    @XStreamAsAttribute
    private String reference= "../..";

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
