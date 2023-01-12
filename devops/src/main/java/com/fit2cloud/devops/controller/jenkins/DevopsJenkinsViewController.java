package com.fit2cloud.devops.controller.jenkins;

import com.fit2cloud.devops.base.domain.DevopsJenkinsView;
import com.fit2cloud.devops.common.PermissionConstants;
import com.fit2cloud.devops.dto.DevopsJenkinsViewDTO;
import com.fit2cloud.devops.dto.request.JobViewRequest;
import com.fit2cloud.devops.service.jenkins.DevopsJenkinsViewService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("jenkins/view")
@Api(tags = "Jenkins视图")
public class DevopsJenkinsViewController {
    @Resource
    private DevopsJenkinsViewService devopsJenkinsViewService;

    @ApiOperation("获取视图列表")
    @PostMapping("getViews")
    @RequiresPermissions(PermissionConstants.JENKINS_VIEW_READ)
    public List<DevopsJenkinsViewDTO> getViews(Map<String, Object> params) {
        return devopsJenkinsViewService.getViews(params);
    }

    @ApiOperation("编辑或新建视图")
    @PostMapping("save")
    @RequiresPermissions({PermissionConstants.JENKINS_VIEW_UPDATE,PermissionConstants.JENKINS_VIEW_CREATE})
    public DevopsJenkinsViewDTO saveJobView(@RequestBody DevopsJenkinsViewDTO devopsJenkinsViewDTO) {
        return devopsJenkinsViewService.saveJobView(devopsJenkinsViewDTO);
    }

    @ApiOperation("删除视图")
    @PostMapping("delete")
    @RequiresPermissions(PermissionConstants.JENKINS_VIEW_DELETE)
    public void deleteViews(@RequestBody List<DevopsJenkinsView> devopsJenkinsViews) {
        devopsJenkinsViewService.deleteViews(devopsJenkinsViews);
    }

    @ApiOperation("添加任务到视图")
    @PostMapping("addJobsToViews")
    @RequiresPermissions(PermissionConstants.JENKINS_VIEW_UPDATE)
    public void addJobsToViews(@RequestBody JobViewRequest jobViewRequest) {
        devopsJenkinsViewService.addJobsToViews(jobViewRequest);
    }
}
