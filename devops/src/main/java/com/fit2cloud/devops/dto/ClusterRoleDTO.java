package com.fit2cloud.devops.dto;


import com.fit2cloud.devops.base.domain.ClusterRole;

public class ClusterRoleDTO extends ClusterRole {
    private String clusterName;
    private Integer countServer;
    private String organizationId;
    private String organizationName;
    private String workspaceName;
    private String workspaceId;
    private String ansibleGroupId;

    public String getAnsibleGroupId() {
        return ansibleGroupId;
    }

    public void setAnsibleGroupId(String ansibleGroupId) {
        this.ansibleGroupId = ansibleGroupId;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public Integer getCountServer() {
        return countServer;
    }

    public void setCountServer(Integer countServer) {
        this.countServer = countServer;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }
}
