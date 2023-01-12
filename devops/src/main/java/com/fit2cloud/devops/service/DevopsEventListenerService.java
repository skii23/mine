package com.fit2cloud.devops.service;

import com.fit2cloud.commons.utils.LogUtil;
import com.fit2cloud.devops.base.domain.DevopsJenkinsJob;
import com.fit2cloud.devops.common.consts.JenkinsConstants;
import com.fit2cloud.devops.service.jenkins.DevopsMultiBranchJobService;
import com.fit2cloud.devops.service.jenkins.model.event.DevopsJenkinsJobChangeEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author caiwzh
 * @date 2022/8/25
 */
@Service
public class DevopsEventListenerService {

    //@Resource
    //private TransactionTemplate transactionTemplate;
    @Resource
    private DevopsMultiBranchJobService devopsMultiBranchJobService;

    @Async
    @EventListener({DevopsJenkinsJobChangeEvent.class})
    public void handle(DevopsJenkinsJobChangeEvent event) {
        DevopsJenkinsJob devopsJenkinsJob = event.getDevopsJenkinsJob();
        LogUtil.info("handle job type[{}],name[{}]", event.getType(), devopsJenkinsJob.getName());
        //多分支流水线需要同步子任务
        if (devopsJenkinsJob.getType().equalsIgnoreCase(JenkinsConstants.JOB_TYPE_MULTIBRANCH)) {
            switch (event.getType()) {
                //新增事件/更新事件 要更新分支与构建记录
                case ADD:
                case UPDATE:
                case SYNC:
                    devopsMultiBranchJobService.syncBranchAndMR(devopsJenkinsJob, true, event.getType());
                    break;
                case BUILD:
                    devopsMultiBranchJobService.buildWorkflowMultiBranchProject(devopsJenkinsJob.getId());
                    break;
                default:
                    break;
            }
        }
    }

}
