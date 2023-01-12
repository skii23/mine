package com.fit2cloud.devops.service.deployment;

import com.fit2cloud.commons.server.redis.queue.AbstractQueue;
import com.fit2cloud.devops.base.domain.ApplicationDeploymentLog;
import com.fit2cloud.devops.common.consts.StatusConstants;
import com.fit2cloud.devops.service.ApplicationDeploymentLogService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class AsyncDeploymentHandler extends AbstractQueue<String> {
    public AsyncDeploymentHandler() {
        super("deployment", 5);
    }

    @Resource
    private ApplicationDeploymentLogService applicationDeploymentLogService;
    @Resource
    private AsycDeploymentLogHandler asycDeploymentLogHandler;

    @Override
    public void handleMessage(String applicationDeploymentLogId) {
        ApplicationDeploymentLog applicationDeploymentLog = applicationDeploymentLogService.getApplicationDeploymentLogById(applicationDeploymentLogId);
        applicationDeploymentLog = applicationDeploymentLogService.saveApplicationDeploymentLog(applicationDeploymentLog);
        //发送单台机器部署消息
        asycDeploymentLogHandler.push(applicationDeploymentLog.getId());
    }


}
