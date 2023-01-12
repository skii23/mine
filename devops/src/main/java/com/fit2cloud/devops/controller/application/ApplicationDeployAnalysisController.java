package com.fit2cloud.devops.controller.application;

import com.fit2cloud.commons.server.model.ChartData;

import com.fit2cloud.devops.common.PermissionConstants;
import com.fit2cloud.devops.common.model.DeployTopData;
import com.fit2cloud.devops.service.ApplicationDeployAnalysisService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("deploy/analysis")
public class ApplicationDeployAnalysisController {


    @Resource
    private ApplicationDeployAnalysisService applicationDeployAnalysisService;

    @PostMapping("/trend")
    @RequiresPermissions(PermissionConstants.DASHBOARD_READ)
    public List<ChartData> getDeployTrend(@RequestBody Map<String, Object> params) {
        return applicationDeployAnalysisService.selectDeployTrend(params);
    }


    @PostMapping("/top")
    @RequiresPermissions(PermissionConstants.DASHBOARD_READ)
    public List<DeployTopData> getDeployTop(@RequestBody Map<String, Object> params) {
        return applicationDeployAnalysisService.selectDeployTop(params);
    }


}
