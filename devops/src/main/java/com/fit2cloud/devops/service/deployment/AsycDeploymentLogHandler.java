package com.fit2cloud.devops.service.deployment;

import com.fit2cloud.commons.server.redis.queue.AbstractQueue;
import com.fit2cloud.commons.utils.CommonThreadPool;
import com.fit2cloud.devops.base.domain.ApplicationDeploymentEventLog;
import com.fit2cloud.devops.base.domain.ApplicationDeploymentLog;
import com.fit2cloud.devops.common.consts.CodeDeploymentSteps;
import com.fit2cloud.devops.common.consts.StatusConstants;
import com.fit2cloud.devops.service.ApplicationDeploymentEventLogService;
import com.fit2cloud.devops.service.ApplicationDeploymentLogService;
import com.fit2cloud.devops.service.deployment.job.AsycDeploymentEventJob;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AsycDeploymentLogHandler extends AbstractQueue<String> {

    public AsycDeploymentLogHandler() {
        super("deployment-log", 5);
    }

    @Resource
    private ApplicationDeploymentLogService applicationDeploymentLogService;
    @Resource
    private ApplicationDeploymentEventLogService applicationDeploymentEventLogService;
    @Resource
    private AsycDeploymentEventJob asycDeploymentEventJob;
    private static final List<String> steps = CodeDeploymentSteps.supportEventTypes;
    @Resource(name = "appDeployPool")
    private CommonThreadPool commonThreadPool;

    @Override
    public void handleMessage(String deploymentLogId) {
        commonThreadPool.addTask(() -> {
            ApplicationDeploymentLog applicationDeploymentLog = applicationDeploymentLogService.getApplicationDeploymentLogById(deploymentLogId);
            applicationDeploymentLog.setStatus(StatusConstants.RUNNING);
            applicationDeploymentLog.setStartTime(System.currentTimeMillis());
            applicationDeploymentLogService.saveApplicationDeploymentLog(applicationDeploymentLog);
            int i = 0;
            Map<String, ApplicationDeploymentEventLog> map = new HashMap<>();
            for (String step : steps) {
                ApplicationDeploymentEventLog applicationDeploymentEventLog = new ApplicationDeploymentEventLog();
                applicationDeploymentEventLog.setDeploymentLogId(applicationDeploymentLog.getId());
                applicationDeploymentEventLog.setStatus(StatusConstants.PENDING);
                applicationDeploymentEventLog.setEventName(step);
                applicationDeploymentEventLog.setCloudServerId(applicationDeploymentLog.getCloudServerId());
                applicationDeploymentEventLog.setApplicationVersionId(applicationDeploymentLog.getApplicationVersionId());
                applicationDeploymentEventLog.setsOrder(++i);
                applicationDeploymentEventLogService.saveApplicationDeploymentEventLog(applicationDeploymentEventLog);
                map.put(step, applicationDeploymentEventLog);
            }
            ApplicationDeploymentEventLog tmp = null;
            try {
                int stepSum = 0;
                String stdout = "";
                for (String step : steps) {
                    tmp = map.get(step);
                    if (StringUtils.equals(CodeDeploymentSteps.INI_ENVIRONMENT, step)) {
                        stdout += asycDeploymentEventJob.initEvn(map.get(step));
                    } else if (StringUtils.equals(CodeDeploymentSteps.INSTALL, step)) {
                        stdout += asycDeploymentEventJob.install(map.get(step));
                    } else  {
                        stdout += asycDeploymentEventJob.executeScript(map.get(step));
                    }
                    Double progress = (((double) (++stepSum)) / steps.size()) * 100;
                    applicationDeploymentLog.setProgress(progress);
                    applicationDeploymentLog.setStdout(stdout);
                    applicationDeploymentLogService.saveApplicationDeploymentLog(applicationDeploymentLog);
                }
                applicationDeploymentLog.setStatus(StatusConstants.SUCCESS);
            } catch (Exception e) {
                applicationDeploymentLog.setStatus(StatusConstants.FAIL);
            } finally {
                asycDeploymentEventJob.cleanEvn(tmp);
                AsycDeploymentEventJob.downloadPath.remove(deploymentLogId);
                AsycDeploymentEventJob.appspecs.remove(deploymentLogId);
                applicationDeploymentLog.setEndTime(System.currentTimeMillis());
                applicationDeploymentLogService.saveApplicationDeploymentLog(applicationDeploymentLog);
            }
        });

    }
}
