package com.fit2cloud.devops.dto;

import com.fit2cloud.devops.base.domain.ScriptImplementLog;

public class ScriptImplementLogDto extends ScriptImplementLog {
    private String clusterId;
    private String clusterName;
    private String clusterRoleId;
    private String clusterRoleName;
    private String scriptName;
    private String cloudServerName;
    private String organizationName;
    private String organizationId;
    private String workspaceName;


    public String getCloudServerName() {
        return cloudServerName;
    }

    public void setCloudServerName(String cloudServerName) {
        this.cloudServerName = cloudServerName;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getClusterRoleId() {
        return clusterRoleId;
    }

    public void setClusterRoleId(String clusterRoleId) {
        this.clusterRoleId = clusterRoleId;
    }

    public String getClusterRoleName() {
        return clusterRoleName;
    }

    public void setClusterRoleName(String clusterRoleName) {
        this.clusterRoleName = clusterRoleName;
    }

    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }
}
