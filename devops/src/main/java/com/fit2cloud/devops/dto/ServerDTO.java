package com.fit2cloud.devops.dto;

import com.fit2cloud.commons.server.base.domain.CloudServer;

public class ServerDTO extends CloudServer {
    private String organizationId;
    private String organizationName;
    private String workspaceName;
    private String username;
    private String password;
    private String secretKey;
    private String source;
    private String clusterId;
    private String clusterRoleId;
    private String clusterRoleIds;
    private String clusterName;
    private String clusterRoleName;
    private String cloudAccountName;
    private String icon;
    private String ansibleHostId;
    private String manageOs;
    private String proxyIp;
    private String proxyId;
    private Boolean connectable;
    private String connectMsg;

    public String getConnectMsg() {
        return connectMsg;
    }

    public void setConnectMsg(String connectMsg) {
        this.connectMsg = connectMsg;
    }

    public Boolean getConnectable() {
        return connectable;
    }

    public void setConnectable(Boolean connectable) {
        this.connectable = connectable;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getSource() {
        return source;
    }

    public ServerDTO setSource(String source) {
        this.source = source;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public ServerDTO setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getClusterRoleIds() {
        return clusterRoleIds;
    }

    public ServerDTO setClusterRoleIds(String clusterRoleIds) {
        this.clusterRoleIds = clusterRoleIds;
        return this;
    }

    public String getProxyIp() {
        return proxyIp;
    }

    public void setProxyIp(String proxyIp) {
        this.proxyIp = proxyIp;
    }

    public String getManageOs() {
        return manageOs;
    }

    public void setManageOs(String manageOs) {
        this.manageOs = manageOs;
    }

    public String getAnsibleHostId() {
        return ansibleHostId;
    }

    public void setAnsibleHostId(String ansibleHostId) {
        this.ansibleHostId = ansibleHostId;
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

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getClusterRoleName() {
        return clusterRoleName;
    }

    public void setClusterRoleName(String clusterRoleName) {
        this.clusterRoleName = clusterRoleName;
    }

    public String getCloudAccountName() {
        return cloudAccountName;
    }

    public void setCloudAccountName(String cloudAccountName) {
        this.cloudAccountName = cloudAccountName;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    public String getProxyId() {
        return proxyId;
    }

    public void setProxyId(String proxyId) {
        this.proxyId = proxyId;
    }
}
