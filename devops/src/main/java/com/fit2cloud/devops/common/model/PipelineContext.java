package com.fit2cloud.devops.common.model;

import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.devops.base.domain.Application;
import com.fit2cloud.devops.base.domain.ApplicationVersion;
import com.fit2cloud.devops.base.domain.DevopsApiTest;
import lombok.Builder;
import lombok.Data;

/**
 * @author caiwzh
 * @date 2022/11/29
 */
@Data
@Builder
public class PipelineContext {
    private String jobName;
    private String buildNumber;
    private String applicationDeploymentId;
    private Application application;
    private ApplicationVersion applicationVersion;
    private DevopsApiTest devopsApiTest;
    private JSONObject publish;
}
