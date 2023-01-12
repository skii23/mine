package com.fit2cloud.devops.service.jenkins.model.common;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * @author wisonic
 */
@XStreamAlias("settings")
public class Settings {
    @XStreamAlias("class")
    @XStreamAsAttribute
    private String classStr = "jenkins.mvn.DefaultSettingsProvider";

    /**
     * 插件版本
     */
    @XStreamAsAttribute
    private String plugin;

    private String path;

    private String settingsConfigId;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getClassStr() {
        return classStr;
    }

    public void setClassStr(String classStr) {
        this.classStr = classStr;
    }

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public String getSettingsConfigId() {
        return settingsConfigId;
    }

    public void setSettingsConfigId(String settingsConfigId) {
        this.settingsConfigId = settingsConfigId;
    }
}
