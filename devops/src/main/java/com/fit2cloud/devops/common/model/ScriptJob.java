package com.fit2cloud.devops.common.model;

import com.fit2cloud.devops.base.domain.Script;

public class ScriptJob extends Script {

    private String clusterId;
    private String clusterRoleId;
    private String serverId;
    private String executePath;
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getExecutePath() {
        return executePath;
    }

    public void setExecutePath(String executePath) {
        this.executePath = executePath;
    }
}
