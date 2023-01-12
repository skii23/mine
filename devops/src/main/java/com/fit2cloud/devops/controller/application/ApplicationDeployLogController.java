package com.fit2cloud.devops.controller.application;


import com.fit2cloud.commons.utils.PageUtils;
import com.fit2cloud.commons.utils.Pager;
import com.fit2cloud.devops.base.domain.ApplicationDeploymentLog;
import com.fit2cloud.devops.common.PermissionConstants;
import com.fit2cloud.devops.service.ApplicationDeploymentEventLogService;
import com.fit2cloud.devops.service.ApplicationDeploymentLogService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("application/deploy/log")
public class ApplicationDeployLogController {

    @Resource
    private ApplicationDeploymentLogService applicationDeploymentLogService;
    @Resource
    private ApplicationDeploymentEventLogService applicationDeploymentEventLogService;

    @PostMapping("list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.APPLICATION_DEPLOY_READ)
    public Pager getApplicationDeploymentLogList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody Map<String, Object> param) {
        Page page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, applicationDeploymentLogService.seletApplicationDeploymentLogs(param));
    }

    @GetMapping("get/{deploymentLogId}")
    public ApplicationDeploymentLog getApplicationDeploymentLog(@PathVariable String deploymentLogId) {
        return applicationDeploymentLogService.getApplicationDeploymentLogById(deploymentLogId);
    }


    @GetMapping("events/{deploymentLogId}")
    public List getApplicationDeploymentEventLogList(@PathVariable String deploymentLogId) {
        return applicationDeploymentEventLogService.selectApplicationDeploymentEventLogs(deploymentLogId);
    }


}
