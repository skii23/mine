package com.fit2cloud.devops.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel("基础查询参数")
public class BaseRequest {
    @ApiModelProperty("工作空间ID")
    String workspaceId;
    @ApiModelProperty("组织ID")
    String organizationId;
}
