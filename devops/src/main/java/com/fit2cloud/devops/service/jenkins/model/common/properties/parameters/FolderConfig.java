package com.fit2cloud.devops.service.jenkins.model.common.properties.parameters;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fit2cloud.devops.service.jenkins.model.common.properties.BasePropertyModel;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * @author caiwzh
 * @date 2022/8/19
 */
@XStreamAlias("org.jenkinsci.plugins.docker.workflow.declarative.FolderConfig")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE)
public class FolderConfig extends BasePropertyModel {
    @XStreamAsAttribute
    private String plugin = "docker-workflow@1.29";

    private String dockerLabel;

    private Registry registry;

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public String getDockerLabel() {
        return dockerLabel;
    }

    public void setDockerLabel(String dockerLabel) {
        this.dockerLabel = dockerLabel;
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public static class Registry{
        @XStreamAsAttribute
        private String plugin = "docker-commons@1.19";
    }
}
