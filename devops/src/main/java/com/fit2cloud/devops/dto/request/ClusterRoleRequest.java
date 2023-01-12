package com.fit2cloud.devops.dto.request;

import com.fit2cloud.commons.annotation.FuzzyQuery;
import io.swagger.annotations.ApiModelProperty;

public class ClusterRoleRequest {

    @ApiModelProperty(value = "主机组名称，模糊匹配")
    @FuzzyQuery
    private String name;
    @ApiModelProperty(value = "集群Id")
    private String clusterId;
    @ApiModelProperty(value = "工作空间ID")
    private String workspaceId;
    @ApiModelProperty(value = "组织ID")
    private String organizationId;
    @ApiModelProperty(value = "排序key", hidden = true)
    private String sort;
    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

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
}
