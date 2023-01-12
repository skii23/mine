package com.fit2cloud.devops.dto;


import com.fit2cloud.devops.base.domain.ApplicationVersion;

public class ApplicationVersionDTO extends ApplicationVersion {
    private String applicationName;
    private String environmentValueId;
    private String clusterId;
    private String clusterRoleId;
    private String systemTagValueAlias;
    private String envTagValueAlias;

    public String getSystemTagValueAlias() {
        return systemTagValueAlias;
    }

    public void setSystemTagValueAlias(String systemTagValueAlias) {
        this.systemTagValueAlias = systemTagValueAlias;
    }

    public String getEnvTagValueAlias() {
        return envTagValueAlias;
    }

    public void setEnvTagValueAlias(String envTagValueAlias) {
        this.envTagValueAlias = envTagValueAlias;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getClusterRoleId() {
        return clusterRoleId;
    }

    public void setClusterRoleId(String clusterRoleId) {
        this.clusterRoleId = clusterRoleId;
    }

    public String getEnvironmentValueId() {
        return environmentValueId;
    }

    public void setEnvironmentValueId(String environmentValueId) {
        this.environmentValueId = environmentValueId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }
}
