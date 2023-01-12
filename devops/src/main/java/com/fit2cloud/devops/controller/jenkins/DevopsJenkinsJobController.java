package com.fit2cloud.devops.controller.jenkins;

import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.commons.server.base.domain.Organization;
import com.fit2cloud.commons.server.base.domain.Workspace;
import com.fit2cloud.commons.utils.CommonThreadPool;
import com.fit2cloud.commons.utils.PageUtils;
import com.fit2cloud.commons.utils.Pager;
import com.fit2cloud.devops.base.domain.Application;
import com.fit2cloud.devops.base.domain.DevopsJenkinsJob;
import com.fit2cloud.devops.common.PermissionConstants;
import com.fit2cloud.devops.common.util.MD5Utils;
import com.fit2cloud.devops.dto.ApplicationDTO;
import com.fit2cloud.devops.dto.DevopsJenkinsJobDto;
import com.fit2cloud.devops.request.JobWorkspaceRequest;
import com.fit2cloud.devops.service.jenkins.DevopsJenkinsJobService;
import com.fit2cloud.devops.vo.FileNodeVO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Api(tags = "Jenkins构建任务")
@RequestMapping("jenkins/job")
public class DevopsJenkinsJobController {

    @Resource
    private DevopsJenkinsJobService devopsJenkinsJobService;

    @Resource
    private CommonThreadPool commonThreadPool;

    @ApiOperation("分页查看构建任务")
    @PostMapping("list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.JENKINS_JOB_READ)
    public Pager listJenkins(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody Map<String, Object> map) {
        Page page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, devopsJenkinsJobService.listAllJenkinsJob(map));
    }

    @ApiOperation("查看构建任务")
    @PostMapping("getJobs")
    @RequiresPermissions(PermissionConstants.JENKINS_JOB_READ)
    public List<DevopsJenkinsJobDto> getJobs(@RequestBody Map<String, Object> params) {
        return devopsJenkinsJobService.listAllJenkinsJob(params);
    }

    @ApiOperation("同步构建任务")
    @PostMapping("syncJobs")
    @RequiresPermissions(PermissionConstants.JENKINS_JOB_UPDATE)
    public void syncJenkinsJob(@RequestBody List<DevopsJenkinsJob> devopsJenkinsJobs) {
        devopsJenkinsJobService.syncJobs(devopsJenkinsJobs);
    }

    @ApiOperation("获得所有组织")
    @GetMapping("getOrganizations")
    @RequiresPermissions(PermissionConstants.JENKINS_JOB_GRANT)
    public List<Organization> getAllOrganization() {
        return devopsJenkinsJobService.getAllOrganization();
    }

    @ApiOperation("获得工作空间")
    @PostMapping("getWorkspaces")
    @RequiresPermissions(PermissionConstants.JENKINS_JOB_GRANT)
    public List<Workspace> getWorkspaces(@RequestBody String[] organizationIds) {
        return devopsJenkinsJobService.getWorkspace(organizationIds);
    }

    @ApiOperation("授权工作空间")
    @PostMapping("jobGrant")
    @RequiresPermissions(PermissionConstants.JENKINS_JOB_GRANT)
    public void jobGrant(@RequestBody List<DevopsJenkinsJob> devopsJenkinsJobs) {
        devopsJenkinsJobService.jobGrant(devopsJenkinsJobs);
    }

    @ApiOperation("执行构建任务")
    @PostMapping("buildJobs")
    @RequiresPermissions(PermissionConstants.JENKINS_JOB_BUILD)
    public void buildJob(@RequestBody List<DevopsJenkinsJob> devopsJenkinsJobs) {
        devopsJenkinsJobService.buildJobs(devopsJenkinsJobs);
    }

    @ApiOperation("执行参数化构建任务")
    @PostMapping("buildWithParametersJob")
    @RequiresPermissions(PermissionConstants.JENKINS_JOB_BUILD)
    public void buildWithParametersJob(@RequestBody DevopsJenkinsJob devopsJenkinsJob) {
        // springmvc 不支持 post 多参数请求对象接收，因webconfig是由第三方包引入，无法使用自定义解析器的注解方式解决 只能扩展值对象
        devopsJenkinsJobService.buildWithParametersJob(devopsJenkinsJob);
    }

    @ApiOperation("创建或编辑构建任务")
    @PostMapping("save")
    @RequiresPermissions({PermissionConstants.JENKINS_JOB_CREATE, PermissionConstants.JENKINS_JOB_UPDATE})
    public void save(@RequestBody JSONObject jsonObject) {
        devopsJenkinsJobService.saveJenkinsJob(jsonObject);
    }

    @ApiOperation("删除构建任务")
    @PostMapping("deleteJob")
    @RequiresPermissions(PermissionConstants.JENKINS_JOB_DELETE)
    public void deleteJob(@RequestBody List<DevopsJenkinsJob> devopsJenkinsJobs) {
        devopsJenkinsJobService.deleteJenkinsJob(devopsJenkinsJobs);
    }

    @ApiOperation("获取构建任务")
    @RequiresPermissions(PermissionConstants.JENKINS_JOB_READ)
    @GetMapping("{devopsJenkinsJobId}")
    public Object getProject(@PathVariable String devopsJenkinsJobId) throws Exception {
        return devopsJenkinsJobService.getJobById(devopsJenkinsJobId);
    }

    @ApiOperation("同步所有构建任务")
    @PostMapping("sync")
    @RequiresPermissions(PermissionConstants.JENKINS_PARAMS_UPDATE)
    public void syncJenkinsJob() {
        commonThreadPool.addTask(() -> devopsJenkinsJobService.syncAllJenkinsJobs());
    }

    @ApiOperation("获取job对应的应用")
    @PostMapping("getApplication")
    @RequiresPermissions({PermissionConstants.JENKINS_JOB_READ, PermissionConstants.APPLICATION_VERSION_READ})
    public ApplicationDTO getApplicationByJob(@RequestBody DevopsJenkinsJob devopsJenkinsJob) {
        return devopsJenkinsJobService.getApplicationByJob(devopsJenkinsJob);
    }

    @ApiOperation("获取job的工作空间")
    @PostMapping("getJobWorkspace")
    @RequiresPermissions(PermissionConstants.JENKINS_JOB_READ)
    public List<FileNodeVO> getJobWorkspace(@RequestBody JobWorkspaceRequest jobWorkspaceRequest) {
        return devopsJenkinsJobService.getJobWorkspace(jobWorkspaceRequest);
    }

    @ApiOperation("下载job工作空间中的文件")
    @PostMapping("getWorkspaceFile")
    @RequiresPermissions(PermissionConstants.JENKINS_JOB_READ)
    public void getWorkspaceFile(@RequestBody JobWorkspaceRequest jobWorkspaceRequest, HttpServletResponse response) {
        devopsJenkinsJobService.getWorkspaceFile(jobWorkspaceRequest,response);
    }

    @ApiOperation("清理工作空间")
    @DeleteMapping("cleanWorkspace/{jobName}")
    @RequiresPermissions(PermissionConstants.JENKINS_JOB_BUILD)
    public void cleanWorkspace(@PathVariable String jobName)throws Exception {
        devopsJenkinsJobService.cleanWorkspace(jobName);
    }

    @ApiOperation("获取随机生成的秘钥")
    @GetMapping("generateSecretToken")
    @RequiresPermissions({PermissionConstants.JENKINS_JOB_CREATE, PermissionConstants.JENKINS_JOB_UPDATE})
    public String generateSecretToken() {
        String token = MD5Utils.doGenerateSecretToken(16);
        return token;
    }

}
