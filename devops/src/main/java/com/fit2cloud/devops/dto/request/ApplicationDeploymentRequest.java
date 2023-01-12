package com.fit2cloud.devops.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("应用部署记录查询参数")
@Getter
@Setter
public class ApplicationDeploymentRequest extends DurationRequest {

    @ApiModelProperty("应用ID")
    private String applicationId;
    @ApiModelProperty("排序列，例如按开始时间正序就传{sort:\"start_time asc\"}")
    private String sort;

}
