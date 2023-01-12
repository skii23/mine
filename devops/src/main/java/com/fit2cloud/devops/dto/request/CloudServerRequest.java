package com.fit2cloud.devops.dto.request;

import com.fit2cloud.commons.annotation.FuzzyQuery;
import io.swagger.annotations.ApiModelProperty;

public class CloudServerRequest {

    @ApiModelProperty("实例名称,模糊匹配")
    @FuzzyQuery
    private String instanceName;
    @ApiModelProperty("云账号ID")
    private String accountId;
    @ApiModelProperty("实例状态")
    private String instanceStatus;
    @ApiModelProperty("集群ID")
    private String clusterId;
    @ApiModelProperty("主机组ID")
    private String clusterRoleId;
    @ApiModelProperty("IP地址")
    private String ipAddress;
    @ApiModelProperty(value = "排序key", hidden = true)
    private String sort;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getInstanceStatus() {
        return instanceStatus;
    }

    public void setInstanceStatus(String instanceStatus) {
        this.instanceStatus = instanceStatus;
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
}
