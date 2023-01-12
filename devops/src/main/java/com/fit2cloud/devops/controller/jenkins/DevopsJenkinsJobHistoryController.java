package com.fit2cloud.devops.controller.jenkins;

import com.fit2cloud.commons.utils.Pager;
import com.fit2cloud.devops.base.domain.DevopsJenkinsJobHistory;
import com.fit2cloud.devops.common.PermissionConstants;
import com.fit2cloud.devops.service.jenkins.DevopsJenkinsJobHistoryService;
import com.offbytwo.jenkins.model.BuildChangeSetItem;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("jenkins/job/history")
@Api(tags = "Jenkins 构建历史")
public class DevopsJenkinsJobHistoryController {

    @Resource
    private DevopsJenkinsJobHistoryService devopsJenkinsJobHistoryService;

    @ApiOperation("获取构建历史")
    @PostMapping("list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.JENKINS_JOB_HISTORY_READ)
    public Pager listJenkins(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody Map<String,Object> map) {
        return devopsJenkinsJobHistoryService.listAllJenkinsJob(map,goPage,pageSize);
    }

    @ApiOperation("获取构建输出")
    @PostMapping("output")
    @RequiresPermissions(PermissionConstants.JENKINS_JOB_HISTORY_READ)
    public String getOutputText(@RequestBody String historyId) {
        return devopsJenkinsJobHistoryService.getOutputText(historyId);
    }

    @ApiOperation("删除构建历史")
    @PostMapping("deleteHistories")
    @RequiresPermissions(PermissionConstants.JENKINS_JOB_HISTORY_DELETE)
    public void deleteBuildHistory(@RequestBody List<DevopsJenkinsJobHistory> devopsJenkinsJobHistories) {
        devopsJenkinsJobHistoryService.deleteJobHistories(devopsJenkinsJobHistories);
    }

    @ApiOperation("获取变更记录")
    @PostMapping("showUpdateRecording")
    @RequiresPermissions(PermissionConstants.JENKINS_JOB_HISTORY_READ)
    public List<BuildChangeSetItem>  showUpdateRecording(@RequestBody String historyId) {
        return devopsJenkinsJobHistoryService.showUpdateRecording(historyId);
    }

    @ApiOperation("获取构建历史执行数据")
    @RequestMapping("showActions")
    @RequiresPermissions(PermissionConstants.JENKINS_JOB_HISTORY_READ)
    public String showActions(@RequestParam String historyId,@RequestParam int buildNumber) {
        //从api获取
        //return devopsJenkinsJobHistoryService.getActions(historyId,buildNumber);
        return devopsJenkinsJobHistoryService.getActions(historyId);
    }
}
