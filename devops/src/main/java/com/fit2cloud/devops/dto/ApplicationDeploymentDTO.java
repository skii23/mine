package com.fit2cloud.devops.dto;

import com.fit2cloud.devops.base.domain.ApplicationDeployment;

import lombok.Data;

@Data
public class ApplicationDeploymentDTO extends ApplicationDeployment {

    private String ApplicationName;
    private String ApplicationVersionName;
    private String clusterName;
    private String clusterRoleName;
    private String cloudServerName;
    private String userName;
    private String testLogId;
    private String testStatus;
    private String apiTestId;
    private String apiTestUrl;
    private Integer apiTestPassRate;
}
