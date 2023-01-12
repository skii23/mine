package com.fit2cloud.devops.service;

import com.fit2cloud.commons.utils.UUIDUtil;
import com.fit2cloud.devops.base.domain.ApplicationDeployment;
import com.fit2cloud.devops.base.domain.ApplicationDeploymentEventLog;
import com.fit2cloud.devops.base.domain.ApplicationDeploymentLog;
import com.fit2cloud.devops.base.domain.ApplicationDeploymentLogExample;
import com.fit2cloud.devops.base.mapper.ApplicationDeploymentLogMapper;
import com.fit2cloud.devops.dao.ext.ExtApplicationDeploymentLogMapper;
import com.fit2cloud.devops.dto.ApplicationDeploymentLogDTO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.fit2cloud.devops.common.consts.StatusConstants.*;

@Service
public class ApplicationDeploymentLogService {
    @Resource
    private ApplicationDeploymentLogMapper applicationDeploymentLogMapper;
    @Resource
    private ExtApplicationDeploymentLogMapper extApplicationDeploymentLogMapper;
    @Resource
    private ApplicationDeploymentEventLogService applicationDeploymentEventLogService;


    public ApplicationDeploymentLog saveApplicationDeploymentLog(ApplicationDeploymentLog applicationDeploymentLog) {
        if (applicationDeploymentLog.getId() != null) {
            applicationDeploymentLogMapper.updateByPrimaryKeySelective(applicationDeploymentLog);
        } else {
            applicationDeploymentLog.setId(UUIDUtil.newUUID());
            applicationDeploymentLogMapper.insert(applicationDeploymentLog);
        }
        return applicationDeploymentLog;
    }

    public List<ApplicationDeploymentLogDTO> seletApplicationDeploymentLogs(Map<String, Object> params) {
        return extApplicationDeploymentLogMapper.selectApplicationDeploymentLog(params);
    }

    public ApplicationDeploymentLog getApplicationDeploymentLogById(String id) {
        return applicationDeploymentLogMapper.selectByPrimaryKey(id);
    }


    public Integer getCompletedServerSum(String deploymentId, List<String> serverIds) {
        ApplicationDeploymentLogExample applicationDeploymentLogExample = new ApplicationDeploymentLogExample();
        applicationDeploymentLogExample.createCriteria()
                .andCloudServerIdIn(serverIds)
                .andDeploymentIdEqualTo(deploymentId)
                .andStatusIn(Arrays.asList(FAIL, SUCCESS, ERROR, TIMEOUT));
        return applicationDeploymentLogMapper.selectByExample(applicationDeploymentLogExample).size();
    }

    public List<ApplicationDeploymentLog> getApplicationDeploymentLogByDeploymentId(String deploymentId) {
        ApplicationDeploymentLogExample applicationDeploymentLogExample = new ApplicationDeploymentLogExample();
        applicationDeploymentLogExample.createCriteria().andDeploymentIdEqualTo(deploymentId);
        return applicationDeploymentLogMapper.selectByExample(applicationDeploymentLogExample);
    }

    public void cleanDeployLog(String deploymentId) {
        ApplicationDeploymentLogExample applicationDeploymentLogExample = new ApplicationDeploymentLogExample();
        applicationDeploymentLogExample.createCriteria()
                .andDeploymentIdEqualTo(deploymentId)
                .andStatusNotIn(Arrays.asList(FAIL, SUCCESS, ERROR, TIMEOUT));
        List<ApplicationDeploymentLog> applicationDeploymentLogs = applicationDeploymentLogMapper.
                selectByExampleWithBLOBs(applicationDeploymentLogExample);
        applicationDeploymentLogs.forEach(eventLog -> {
            eventLog.setEndTime(System.currentTimeMillis());
            eventLog.setStatus(FAIL);
            eventLog.setStdout("系统异常中断！");
            applicationDeploymentEventLogService.cleanEventLog(eventLog.getId());
            applicationDeploymentLogMapper.updateByPrimaryKeyWithBLOBs(eventLog);
        });
    }
}
