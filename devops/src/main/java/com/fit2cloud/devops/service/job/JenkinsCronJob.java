package com.fit2cloud.devops.service.job;

import com.fit2cloud.commons.utils.CommonThreadPool;
import com.fit2cloud.devops.base.domain.DevopsJenkinsJob;
import com.fit2cloud.devops.base.domain.DevopsJenkinsJobHistory;
import com.fit2cloud.devops.base.domain.DevopsJenkinsJobHistoryExample;
import com.fit2cloud.devops.base.mapper.DevopsJenkinsJobHistoryMapper;
import com.fit2cloud.devops.base.mapper.DevopsJenkinsJobMapper;
import com.fit2cloud.devops.service.jenkins.DevopsJenkinsJobHistoryService;
import com.fit2cloud.devops.service.jenkins.DevopsJenkinsJobService;
import com.fit2cloud.devops.service.jenkins.DevopsMultiBranchJobService;
import com.fit2cloud.quartz.anno.QuartzScheduled;
import com.offbytwo.jenkins.model.BuildResult;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * @author shaochuan.wu
 */
@Component
public class JenkinsCronJob {

    @Resource
    private DevopsJenkinsJobHistoryMapper devopsJenkinsJobHistoryMapper;

    @Resource(name = "devopsJobPool")
    private CommonThreadPool commonThreadPool;

    @Resource
    private DevopsJenkinsJobHistoryService devopsJenkinsJobHistoryService;

    @Resource
    private DevopsJenkinsJobMapper devopsJenkinsJobMapper;

    @Resource
    private DevopsJenkinsJobService devopsJenkinsJobService;

    @Resource
    private DevopsMultiBranchJobService devopsMultiBranchJobService;

    @QuartzScheduled(cron = "${sync.jenkins.history.status.scan}")
    public void syncJenkinsJobHistoryStatus() {
        DevopsJenkinsJobHistoryExample example = new DevopsJenkinsJobHistoryExample();
        example.createCriteria().andBuildStatusIn(Arrays.asList(BuildResult.BUILDING.name(),BuildResult.REBUILDING.name()));
        List<DevopsJenkinsJobHistory> histories = devopsJenkinsJobHistoryMapper.selectByExample(example);
        if (!histories.isEmpty()) {
            commonThreadPool.addTask(() -> devopsJenkinsJobHistoryService.syncBuildHistoryStatusV2(histories));
        }
    }
    @QuartzScheduled(cron = "${sync.jenkins.job.status.scan}")
    public void syncJenkinsJobBuildStatus() {
        Example devopsJenkinsJobExample = new Example(DevopsJenkinsJob.class);
        devopsJenkinsJobExample.createCriteria().andIn("buildStatus",Arrays.asList(BuildResult.BUILDING.name(), BuildResult.REBUILDING.name()));
        List<DevopsJenkinsJob> devopsJenkinsJobs = devopsJenkinsJobMapper.selectByExample(devopsJenkinsJobExample);
        if (CollectionUtils.isNotEmpty(devopsJenkinsJobs)) {
            commonThreadPool.addTask(() -> devopsJenkinsJobService.syncJobBuildStatus(devopsJenkinsJobs));
        }
    }

    @QuartzScheduled(cron = "0/15 * * * * ?")
    public void syncJenkinsMultiBranchJobHistory() {
        Example devopsJenkinsJobExample = new Example(DevopsJenkinsJob.class);
        devopsJenkinsJobExample.createCriteria().andIsNotNull("parentId");
        List<DevopsJenkinsJob> devopsJenkinsJobs = devopsJenkinsJobMapper.selectByExample(devopsJenkinsJobExample);
        if (!devopsJenkinsJobs.isEmpty()) {
            commonThreadPool.addTask(() -> devopsMultiBranchJobService.syncHistoryTriggerFromJenkins(devopsJenkinsJobs));
        }
    }

}
