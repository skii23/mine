package com.fit2cloud.devops.service.jenkins.model.common.actions;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("org.jenkinsci.plugins.pipeline.modeldefinition.actions.DeclarativeJobAction")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE)
public class DeclarativeJobAction {
    @XStreamAsAttribute
    private String plugin = "pipeline-model-definition@2.2097.v33db_b_de764b_e";

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }
}

