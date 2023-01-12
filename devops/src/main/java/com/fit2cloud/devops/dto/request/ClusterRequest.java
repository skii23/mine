package com.fit2cloud.devops.dto.request;

import com.fit2cloud.commons.annotation.FuzzyQuery;
import io.swagger.annotations.ApiModelProperty;

public class ClusterRequest {
    @ApiModelProperty(value = "集群名称,模糊匹配")
    @FuzzyQuery
    private String name;
    @ApiModelProperty(value = "工作空间ID")
    private String workspaceId;
    @ApiModelProperty(value = "组织ID")
    private String organizationId;
    @ApiModelProperty(value = "排序key", hidden = true)
    private String sort;
    @ApiModelProperty(value = "环境标签值")
    private String envValueId;
    @ApiModelProperty(value = "业务标签值")
    private String systemValueId;

    public String getEnvValueId() {
        return envValueId;
    }

    public void setEnvValueId(String envValueId) {
        this.envValueId = envValueId;
    }

    public String getSystemValueId() {
        return systemValueId;
    }

    public void setSystemValueId(String systemValueId) {
        this.systemValueId = systemValueId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }
}
