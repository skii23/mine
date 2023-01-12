package com.fit2cloud.devops.service.deployment;

import com.fit2cloud.devops.service.ApplicationDeploymentService;
import com.fit2cloud.devops.service.deployment.job.AsycDeployJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class BeanPostProcessor {

    @Autowired
    private AsycDeployJob asycDeployJob;
    @Autowired
    private ApplicationDeploymentService applicationDeploymentService;

    @PostConstruct
    public void init() {
        asycDeployJob.setApplicationDeploymentService(applicationDeploymentService);
        applicationDeploymentService.setAsycDeployJob(asycDeployJob);
    }
}
