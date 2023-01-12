package com.fit2cloud.devops.controller.application;


import com.fit2cloud.commons.utils.PageUtils;
import com.fit2cloud.commons.utils.Pager;
import com.fit2cloud.commons.utils.ResultHolder;
import com.fit2cloud.devops.base.domain.ApplicationDeployment;
import com.fit2cloud.devops.common.PermissionConstants;
import com.fit2cloud.devops.common.consts.CodeDeployPolicys;
import com.fit2cloud.devops.dto.ApplicationDeploymentAndTestDTO;
import com.fit2cloud.devops.dto.ClusterDTO;
import com.fit2cloud.devops.service.ApplicationDeploymentService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Api(tags = "应用部署")
@RestController
@RequestMapping("application/deploy")
public class ApplicationDeployController {

    @Resource
    private ApplicationDeploymentService applicationDeploymentService;

    @GetMapping("clusters/{versionId}")
    public List<ClusterDTO> getClustersByVersion(@PathVariable String versionId) {
        return applicationDeploymentService.getClustersByVersion(versionId);
    }

    @GetMapping("policies")
    public List<String> getPolicies() {
        return CodeDeployPolicys.getAll();
    }

    @ApiOperation("创建部署")
    @RequiresPermissions(PermissionConstants.APPLICATION_DEPLOY_CREATE)
    @PostMapping("save")
    public ResultHolder deploy(@RequestBody ApplicationDeploymentAndTestDTO applicationDeployment) {
        return applicationDeploymentService.saveApplicationDeployment(applicationDeployment);
    }

    @ApiOperation("获取部署任务")
    @RequiresPermissions(PermissionConstants.APPLICATION_DEPLOY_READ)
    @GetMapping("get")
    public ApplicationDeployment getApplicationDeployment(@RequestParam String applicationDeploymentId) {
        return applicationDeploymentService.getApplicationDeployment(applicationDeploymentId);
    }

    @ApiOperation("部署记录列表")
    @RequiresPermissions(PermissionConstants.APPLICATION_DEPLOY_READ)
    @PostMapping("list/{goPage}/{pageSize}")
    public Pager getApplicationDeployments(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody Map<String, Object> params) {
        Page page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, applicationDeploymentService.selectApplicationDeployment(params));
    }

    @ApiOperation("检测现在是否能部署")
    @GetMapping("checkDeploy")
    public boolean checkDeploy() {
        return applicationDeploymentService.checkDeploySettings();
    }

}
