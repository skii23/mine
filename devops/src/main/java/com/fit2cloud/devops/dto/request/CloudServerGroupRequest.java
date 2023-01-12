package com.fit2cloud.devops.dto.request;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class CloudServerGroupRequest {

    @ApiModelProperty("角色名称,模糊匹配")
    private List<String> cloudServerIds;
    private String clusterRoleId;
    private String sort;

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public List<String> getCloudServerIds() {
        return cloudServerIds;
    }

    public void setCloudServerIds(List<String> cloudServerIds) {
        this.cloudServerIds = cloudServerIds;
    }

    public String getClusterRoleId() {
        return clusterRoleId;
    }

    public void setClusterRoleId(String clusterRoleId) {
        this.clusterRoleId = clusterRoleId;
    }
}
