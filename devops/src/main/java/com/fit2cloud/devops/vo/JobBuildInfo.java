package com.fit2cloud.devops.vo;

import lombok.Data;

/**
 * @author caiwzh
 * @date 2022/11/8
 */
@Data
public class JobBuildInfo {
    private String id;
    private String name;
    private String latesStatus;
    private String latesJobSn;
    private Long latesBuildTime;
    private int buildCount;
    private Long buildTime;
    private Integer buildFailRate;
}
