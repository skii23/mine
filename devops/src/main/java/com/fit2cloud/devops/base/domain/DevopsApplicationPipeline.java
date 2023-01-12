package com.fit2cloud.devops.base.domain;

import lombok.Data;

import javax.persistence.Id;

/**
 * @author caiwzh
 * @date 2022/12/1
 */
@Data
public class DevopsApplicationPipeline {
    @Id
    private String id;
    private Long createTime;
    private String jobHistoryId;
    private String jobId;
    private String deploymentId;
    private String sonarqubeId;
    private String unittestId;
    private String triggerName;
    private String appName;
}
