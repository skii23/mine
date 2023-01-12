package com.fit2cloud.devops.dto;


import com.fit2cloud.devops.base.domain.Cluster;

public class ClusterDTO extends Cluster {
    private Integer countClusterRole;
    private Integer countServer;
    private String organizationName;
    private String organizationId;
    private String workspaceName;
    private String ansibleGroupId;
    private String systemValueId;
    private String systemTagValueAlias;
    private String envValueId;
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

    public String getSystemValueId() {
        return systemValueId;
    }

    public void setSystemValueId(String systemValueId) {
        this.systemValueId = systemValueId;
    }

    public String getEnvValueId() {
        return envValueId;
    }

    public void setEnvValueId(String envValueId) {
        this.envValueId = envValueId;
    }

    public String getAnsibleGroupId() {
        return ansibleGroupId;
    }

    public void setAnsibleGroupId(String ansibleGroupId) {
        this.ansibleGroupId = ansibleGroupId;
    }

    public Integer getCountClusterRole() {
        return countClusterRole;
    }

    public void setCountClusterRole(Integer countClusterRole) {
        this.countClusterRole = countClusterRole;
    }

    public Integer getCountServer() {
        return countServer;
    }

    public void setCountServer(Integer countServer) {
        this.countServer = countServer;
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
