package com.fit2cloud.devops.controller;

import com.fit2cloud.devops.base.domain.ApplicationDeploySettings;
import com.fit2cloud.devops.common.PermissionConstants;
import com.fit2cloud.devops.service.ApplicationDeploySettingsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author wisonic
 */
@RestController
@RequestMapping("deploy/settings")
@Api(tags = "应用部署设置")
public class ApplicationDeploySettingsController {
    @Resource
    private ApplicationDeploySettingsService applicationDeploySettingsService;

    @ApiOperation("获取每周部署设置")
    @GetMapping("weekday")
    @RequiresPermissions(PermissionConstants.APPLICATION_DEPLOY_SETTINGS_READ)
    public List<ApplicationDeploySettings> getWeekdaySettings() {
        return applicationDeploySettingsService.getWeekdaySettings();
    }

    @ApiOperation("保存每周部署设置")
    @PostMapping("weekday/save")
    @RequiresPermissions(PermissionConstants.APPLICATION_DEPLOY_SETTINGS_UPDATE)
    public void saveWeekdaySettings(@RequestBody List<ApplicationDeploySettings> applicationDeploySettings) {
        applicationDeploySettingsService.saveWeekdaySettings(applicationDeploySettings);
    }

    @ApiOperation("获取例外日期设置")
    @GetMapping("expectedDays")
    @RequiresPermissions(PermissionConstants.APPLICATION_DEPLOY_SETTINGS_READ)
    public List<ApplicationDeploySettings> getExpectedSettings() {
        return applicationDeploySettingsService.getExpectedSettings();
    }

    @ApiOperation("保存例外日期设置")
    @PostMapping("expectedDays/save")
    @RequiresPermissions(PermissionConstants.APPLICATION_DEPLOY_SETTINGS_UPDATE)
    public void saveExpectedDaySettings(@RequestBody List<ApplicationDeploySettings> applicationDeploySettings) {
        applicationDeploySettingsService.saveExpectedDaySettings(applicationDeploySettings);
    }

}
