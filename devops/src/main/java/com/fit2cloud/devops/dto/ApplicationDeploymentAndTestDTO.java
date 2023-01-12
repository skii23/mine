package com.fit2cloud.devops.dto;

import com.fit2cloud.devops.base.domain.ApplicationDeployment;
import lombok.Data;

@Data
public class ApplicationDeploymentAndTestDTO extends ApplicationDeployment {
    private String jobName;
    private String buildNumber;
    private Boolean autoApiTest;
    private String testPollingTimeoutSec;
    private String testPollingFreqSec;
    private Boolean onlyUnPassed;
    private Boolean needReport;
    private String biz;
    private String pollingTimeoutSec;
    private String testProdId;
    private String testPlanId;
    private String testEvn;
}
