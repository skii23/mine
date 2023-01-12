package com.fit2cloud.devops.service;

import com.fit2cloud.commons.utils.UUIDUtil;
import com.fit2cloud.devops.base.domain.ApplicationDeploymentEventLog;
import com.fit2cloud.devops.base.domain.ApplicationDeploymentEventLogExample;
import com.fit2cloud.devops.base.mapper.ApplicationDeploymentEventLogMapper;
import com.fit2cloud.devops.common.consts.StatusConstants;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.fit2cloud.devops.common.consts.StatusConstants.*;

@Service
public class ApplicationDeploymentEventLogService {

    @Resource
    private ApplicationDeploymentEventLogMapper applicationDeploymentEventLogMapper;


    public ApplicationDeploymentEventLog saveApplicationDeploymentEventLog(ApplicationDeploymentEventLog applicationDeploymentEventLog) {
        if (applicationDeploymentEventLog.getId() != null) {
            applicationDeploymentEventLogMapper.updateByPrimaryKeySelective(applicationDeploymentEventLog);
        } else {
            applicationDeploymentEventLog.setId(UUIDUtil.newUUID());
            applicationDeploymentEventLogMapper.insert(applicationDeploymentEventLog);
        }
        return applicationDeploymentEventLog;
    }


    public List<ApplicationDeploymentEventLog> selectApplicationDeploymentEventLogs(String deploymentLogId) {
        ApplicationDeploymentEventLogExample applicationDeploymentEventLogExample = new ApplicationDeploymentEventLogExample();
        applicationDeploymentEventLogExample.createCriteria().andDeploymentLogIdEqualTo(deploymentLogId);
        applicationDeploymentEventLogExample.setOrderByClause("s_order asc");
        return applicationDeploymentEventLogMapper.selectByExampleWithBLOBs(applicationDeploymentEventLogExample);
    }

    public void cleanEventLog(String deploymentLogId) {
        ApplicationDeploymentEventLogExample applicationDeploymentEventLogExample = new ApplicationDeploymentEventLogExample();
        applicationDeploymentEventLogExample.createCriteria()
                .andDeploymentLogIdEqualTo(deploymentLogId);
        List<ApplicationDeploymentEventLog> applicationDeploymentEventLogs = applicationDeploymentEventLogMapper.selectByExampleWithBLOBs(applicationDeploymentEventLogExample);
        applicationDeploymentEventLogs.sort(Comparator.comparingInt(ApplicationDeploymentEventLog::getsOrder));

        for (ApplicationDeploymentEventLog applicationDeploymentEventLog : applicationDeploymentEventLogs) {
            if (!StatusConstants.isCompleted(applicationDeploymentEventLog.getStatus())) {
                applicationDeploymentEventLog.setEndTime(System.currentTimeMillis());
                applicationDeploymentEventLog.setStatus(FAIL);
                applicationDeploymentEventLog.setStdout("系统异常中断！");
                applicationDeploymentEventLogMapper.updateByPrimaryKeyWithBLOBs(applicationDeploymentEventLog);
                break;
            }
        }
    }

}
