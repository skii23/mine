package com.fit2cloud.devops.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel("时间区间查询参数")
public class DurationRequest extends BaseRequest{
    @ApiModelProperty("开始时间时间戳")
    long start;
    @ApiModelProperty("结束时间时间戳")
    long end;
}
