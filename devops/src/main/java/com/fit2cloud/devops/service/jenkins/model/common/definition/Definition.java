package com.fit2cloud.devops.service.jenkins.model.common.definition;

import com.fit2cloud.devops.service.jenkins.model.AbstractBaseModel;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamInclude;

@XStreamInclude({CpsScmFlowDefinition.class, CpsFlowDefinition.class})
public abstract class Definition extends AbstractBaseModel {
    @XStreamAsAttribute
    private String plugin = "workflow-cps@2725.v7b_c717eb_12ce";

    @XStreamAlias("class")
    @XStreamAsAttribute
    private String clazz;

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }
}
