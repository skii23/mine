package com.fit2cloud.devops.service.jenkins.model.common.actions;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("org.jenkinsci.plugins.pipeline.modeldefinition.actions.DeclarativeJobPropertyTrackerAction")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE)
public class DeclarativeJobPropertyTrackerAction {
    @XStreamAsAttribute
    private String plugin = "pipeline-model-definition@2.2097.v33db_b_de764b_e";
    private String jobProperties = "";
    private String triggers = "";
    private String parameters = "";
    private String options = "";

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public String getJobProperties() {
        return jobProperties;
    }

    public void setJobProperties(String jobProperties) {
        this.jobProperties = jobProperties;
    }

    public String getTriggers() {
        return triggers;
    }

    public void setTriggers(String triggers) {
        this.triggers = triggers;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }
}

