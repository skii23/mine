package com.fit2cloud.devops.dto.request;

import io.swagger.annotations.ApiModelProperty;

public class VariableRequest {

    @ApiModelProperty("环境变量名称,模糊匹配")
    private String name;
    @ApiModelProperty("集群ID")
    private String clusterId;
    @ApiModelProperty("主机组ID")
    private String clusterRoleId;
    @ApiModelProperty("云主机ID")
    private String cloudServerId;
    @ApiModelProperty(value = "排序key", hidden = true)
    private String sort;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getCloudServerId() {
        return cloudServerId;
    }

    public void setCloudServerId(String cloudServerId) {
        this.cloudServerId = cloudServerId;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }
}
