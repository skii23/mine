package com.fit2cloud.devops.service.jenkins.model.multibranch;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * @author caiwzh
 * @date 2022/8/24
 */
@XStreamAlias("factory")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE)
public class Factory {
    @XStreamAlias("class")
    @XStreamAsAttribute
    private String clazz= "org.jenkinsci.plugins.workflow.multibranch.WorkflowBranchProjectFactory";

    private Owner owner;

    private String scriptPath= "Jenkinsfile";

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public String getScriptPath() {
        return scriptPath;
    }

    public void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath;
    }
}
