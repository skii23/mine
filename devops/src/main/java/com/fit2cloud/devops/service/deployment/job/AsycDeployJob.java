package com.fit2cloud.devops.service.deployment.job;

import com.fit2cloud.commons.server.base.domain.CloudServer;
import com.fit2cloud.commons.server.exception.F2CException;
import com.fit2cloud.devops.base.domain.ApplicationDeployment;
import com.fit2cloud.devops.base.domain.ApplicationDeploymentLog;
import com.fit2cloud.devops.base.domain.ApplicationVersion;
import com.fit2cloud.devops.base.mapper.ApplicationDeploymentMapper;
import com.fit2cloud.devops.common.consts.CodeDeployPolicys;
import com.fit2cloud.devops.common.consts.StatusConstants;
import com.fit2cloud.devops.dto.ServerDTO;
import com.fit2cloud.devops.service.ApplicationDeploymentLogService;
import com.fit2cloud.devops.service.ApplicationDeploymentService;
import com.fit2cloud.devops.service.ApplicationVersionService;
import com.fit2cloud.devops.service.ServerService;
import com.fit2cloud.devops.service.deployment.AsyncDeploymentHandler;
import com.fit2cloud.devops.service.deployment.exception.DeployTimeoutException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用于异步执行部署任务
 */
@Component
public class AsycDeployJob {

    @Resource
    private ServerService serverService;
    @Resource
    private ApplicationVersionService applicationVersionService;
    private ApplicationDeploymentService applicationDeploymentService;
    @Resource
    private ApplicationDeploymentLogService applicationDeploymentLogService;
    @Resource
    private AsyncDeploymentHandler asyncDeploymentHandler;
    @Resource
    private ApplicationDeploymentMapper applicationDeploymentMapper;

    public ApplicationDeploymentService getApplicationDeploymentService() {
        return applicationDeploymentService;
    }

    public void setApplicationDeploymentService(ApplicationDeploymentService applicationDeploymentService) {
        this.applicationDeploymentService = applicationDeploymentService;
    }

    @Async("syncExecutor")
    public void depoly(ApplicationDeployment applicationDeployment) {
        try {
            String policy = applicationDeployment.getPolicy();

            ApplicationVersion applicationVersion = applicationVersionService.getApplicationVersion(applicationDeployment.getApplicationVersionId());
            List<ServerDTO> serverDTOS = getServers(applicationDeployment);

            switch (policy) {
                case CodeDeployPolicys.ALL:
                    deployAll(serverDTOS, applicationVersion, applicationDeployment);
                    break;
                case CodeDeployPolicys.HALF:
                    deployHalf(serverDTOS, applicationVersion, applicationDeployment);
                    break;
                case CodeDeployPolicys.SINGLE:
                    deploySingle(serverDTOS, applicationVersion, applicationDeployment);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            applicationDeployment.setStatus(StatusConstants.FAIL);
            applicationDeploymentMapper.updateByPrimaryKeySelective(applicationDeployment);
            e.printStackTrace();
        }
    }


    private void deployAll(List<ServerDTO> serverDTOS, ApplicationVersion applicationVersion, ApplicationDeployment applicationDeployment) {

        Map<ApplicationDeploymentLog, ServerDTO> map = initApplicationDeploymentLogs(serverDTOS, applicationDeployment, applicationVersion);
        try {
            for (ApplicationDeploymentLog applicationDeploymentLog : map.keySet()) {
                asyncDeploymentHandler.push(applicationDeploymentLog.getId());
            }
            List<String> serverIds = serverDTOS.stream().map(CloudServer::getId).collect(Collectors.toList());

            updateProgress(applicationDeployment, serverDTOS.size(), serverIds);
            updateStatus(applicationDeployment);
        } catch (DeployTimeoutException e) {
            applicationDeployment.setStatus(StatusConstants.TIMEOUT);
            applicationDeploymentService.saveApplicationDeployment(applicationDeployment);
        }


    }


    private void deployHalf(List<ServerDTO> serverDTOS, ApplicationVersion applicationVersion, ApplicationDeployment applicationDeployment) {
        Map<ApplicationDeploymentLog, ServerDTO> map = initApplicationDeploymentLogs(serverDTOS, applicationDeployment, applicationVersion);
        int serverSum = serverDTOS.size();
        int mid = serverSum >> 1;
        int block = 0;
        List<String> perServerIds = new ArrayList<>();
        List<String> lastServerIds = new ArrayList<>();

        try {
            for (ApplicationDeploymentLog applicationDeploymentLog : map.keySet()) {
                asyncDeploymentHandler.push(applicationDeploymentLog.getId());
                --serverSum;
                if (serverSum >= mid) {
                    perServerIds.add(applicationDeploymentLog.getCloudServerId());
                } else {
                    lastServerIds.add(applicationDeploymentLog.getCloudServerId());
                }
                if (serverSum == mid) {
                    block = serverDTOS.size() - serverSum;
                    updateProgress(applicationDeployment, block, perServerIds);
                } else if (serverSum == 0) {
                    block = serverDTOS.size() - block;
                    updateProgress(applicationDeployment, block, lastServerIds);
                }

            }
            updateStatus(applicationDeployment);
        } catch (DeployTimeoutException e) {
            applicationDeployment.setStatus(StatusConstants.TIMEOUT);
            applicationDeploymentService.saveApplicationDeployment(applicationDeployment);
        }


    }


    private void deploySingle(List<ServerDTO> serverDTOS, ApplicationVersion applicationVersion, ApplicationDeployment applicationDeployment) {
        Map<ApplicationDeploymentLog, ServerDTO> map = initApplicationDeploymentLogs(serverDTOS, applicationDeployment, applicationVersion);

        try {
            for (ApplicationDeploymentLog applicationDeploymentLog : map.keySet()) {
                asyncDeploymentHandler.push(applicationDeploymentLog.getId());
                updateProgress(applicationDeployment, 1, Collections.singletonList(applicationDeploymentLog.getCloudServerId()));
            }
            updateStatus(applicationDeployment);
        } catch (DeployTimeoutException e) {
            applicationDeployment.setStatus(StatusConstants.TIMEOUT);
            applicationDeploymentService.saveApplicationDeployment(applicationDeployment);
        }

    }


    private void updateProgress(ApplicationDeployment applicationDeployment, Integer block, List<String> serverIds) {
        int timeout = 60 * 15;
        while (--timeout != 0) {

            int completed = applicationDeploymentLogService.getCompletedServerSum(applicationDeployment.getId(), serverIds);

            if (block == completed) break;
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                F2CException.throwException(e);
            }
        }
        if (timeout == 0) {
            F2CException.throwException(new DeployTimeoutException());
        }
    }


    private void updateStatus(ApplicationDeployment applicationDeployment) {
        List<ApplicationDeploymentLog> applicationDeploymentLogs = applicationDeploymentLogService.getApplicationDeploymentLogByDeploymentId(applicationDeployment.getId());
        boolean status = true;
        for (ApplicationDeploymentLog applicationDeploymentLog : applicationDeploymentLogs) {
            if (status) {
                status = applicationDeploymentLog.getStatus().equals(StatusConstants.SUCCESS);
            }
        }
        applicationDeployment.setEndTime(System.currentTimeMillis());
        if (status) {
            applicationDeployment.setStatus(StatusConstants.SUCCESS);
        } else {
            applicationDeployment.setStatus(StatusConstants.FAIL);
        }
        applicationDeploymentService.saveApplicationDeployment(applicationDeployment);
    }

    private Map<ApplicationDeploymentLog, ServerDTO> initApplicationDeploymentLogs(List<ServerDTO> serverDTOS, ApplicationDeployment applicationDeployment, ApplicationVersion applicationVersion) {
        applicationDeployment.setStatus(StatusConstants.RUNNING);
        applicationDeployment.setStartTime(System.currentTimeMillis());
        applicationDeploymentService.saveApplicationDeployment(applicationDeployment);

        Map<ApplicationDeploymentLog, ServerDTO> map = new HashMap<>();

        for (ServerDTO serverDTO : serverDTOS) {
            ApplicationDeploymentLog applicationDeploymentLog = new ApplicationDeploymentLog();
            applicationDeploymentLog.setStatus(StatusConstants.PENDING);
            applicationDeploymentLog.setCloudServerId(serverDTO.getId());
            applicationDeploymentLog.setDeploymentId(applicationDeployment.getId());
            applicationDeploymentLog.setApplicationVersionId(applicationVersion.getId());
            applicationDeploymentLog.setProgress(.0);
            applicationDeploymentLog = applicationDeploymentLogService.saveApplicationDeploymentLog(applicationDeploymentLog);
            map.put(applicationDeploymentLog, serverDTO);
        }
        return map;
    }


    private List<ServerDTO> getServers(ApplicationDeployment applicationDeployment) {
        List<ServerDTO> serverDTOS = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        params.put("sort", "instance_name");
        if (!applicationDeployment.getCloudServerId().equalsIgnoreCase("ALL")) {
            params.put("ids", Arrays.asList(applicationDeployment.getCloudServerId().split(",")));
            serverDTOS.addAll(serverService.selectServerDto(params));
        } else if (!applicationDeployment.getClusterRoleId().equalsIgnoreCase("ALL")) {
            params.put("clusterRoleId", applicationDeployment.getClusterRoleId());
            serverDTOS.addAll(serverService.selectServerDto(params));
        } else if (applicationDeployment.getClusterRoleId().equalsIgnoreCase("ALL")) {
            params.put("clusterId", applicationDeployment.getClusterId());
            serverDTOS.addAll(serverService.selectServerDto(params));
        }
        return serverDTOS;
    }


}
