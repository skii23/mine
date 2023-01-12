package com.fit2cloud.devops.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel("部署情况概览查询请求")
public class DashboardDeploySearchReq {
    @ApiModelProperty("集群ID")
    private String clusterId;
    @ApiModelProperty("主机组ID")
    private String clusterRoleId;
}
