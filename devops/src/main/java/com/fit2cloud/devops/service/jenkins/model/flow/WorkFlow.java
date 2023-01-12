package com.fit2cloud.devops.service.jenkins.model.flow;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fit2cloud.devops.service.jenkins.model.common.BaseJobModel;
import com.fit2cloud.devops.service.jenkins.model.common.actions.Actions;
import com.fit2cloud.devops.service.jenkins.model.common.definition.Definition;
import com.fit2cloud.devops.service.jenkins.model.common.properties.BasePropertyModel;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import java.util.List;

@XStreamAlias("flow-definition")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE)
public class WorkFlow extends BaseJobModel {

    @XStreamAsAttribute
    private String plugin = "workflow-job@1189.va_d37a_e9e4eda_";

    private Boolean keepDependencies = false;

    @XStreamAlias("properties")
    private List<BasePropertyModel> properties;

    private Actions actions;

    private Definition definition;

    private Boolean disabled = false;

    public String getPlugin() {
        return plugin;
    }


    public Actions getActions() {
        return actions;
    }

    public void setActions(Actions actions) {
        this.actions = actions;
    }

    public Definition getDefinition() {
        return definition;
    }

    public void setDefinition(Definition definition) {
        this.definition = definition;
    }

    public List<BasePropertyModel> getProperties() {
        return properties;
    }

    public void setProperties(List<BasePropertyModel> properties) {
        this.properties = properties;
    }

    public Boolean getKeepDependencies() {
        return keepDependencies;
    }

    public void setKeepDependencies(Boolean keepDependencies) {
        this.keepDependencies = keepDependencies;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }
}
