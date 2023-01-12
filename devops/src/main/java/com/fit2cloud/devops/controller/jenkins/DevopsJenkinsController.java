package com.fit2cloud.devops.controller.jenkins;

import com.fit2cloud.commons.server.base.domain.SystemParameter;
import com.fit2cloud.commons.utils.ResultHolder;
import com.fit2cloud.devops.common.PermissionConstants;
import com.fit2cloud.devops.service.jenkins.DevopsJenkinsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * jenkins
 *
 * @author makai
 */
@RestController
@Api(tags = "Jenkins配置信息")
@RequestMapping("/jenkins")
public class DevopsJenkinsController {
    @Resource
    private DevopsJenkinsService devopsJenkinsService;

    @ApiOperation("验证jenkins账号")
    @PostMapping("validate")
    @RequiresPermissions(PermissionConstants.JENKINS_PARAMS_READ)
    public ResultHolder validateJenkins(@RequestBody List<SystemParameter> systemParameters) {
        return devopsJenkinsService.validateSystemParams(systemParameters);
    }

    @ApiOperation("获取jenkins配置参数")
    @GetMapping("jenkinsParams/systemParams")
    @RequiresPermissions(PermissionConstants.JENKINS_PARAMS_READ)
    public List<SystemParameter> getJenkinsSystemParams() {
        return devopsJenkinsService.getJenkinsSystemParams();
    }

    @ApiOperation("保存jenkins配置参数")
    @PostMapping("jenkinsParams/systemParams")
    @RequiresPermissions(PermissionConstants.JENKINS_PARAMS_UPDATE)
    public Object saveJenkinsSystemParams(@RequestBody List<SystemParameter> systemParameters) {
        return devopsJenkinsService.saveJenkinsSystemParams(systemParameters);
    }

    @ApiOperation("获取同步状态")
    @GetMapping("syncStatus")
    @RequiresPermissions(PermissionConstants.JENKINS_PARAMS_READ)
    public String getSyncStatus() {
        return devopsJenkinsService.getSyncStatus();
    }


}
