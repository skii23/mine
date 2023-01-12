package com.fit2cloud.devops.controller;

import com.fit2cloud.commons.server.model.ChartData;
import com.fit2cloud.commons.server.model.DashBoardTextDTO;
import com.fit2cloud.commons.utils.BeanUtils;
import com.fit2cloud.commons.utils.Pager;
import com.fit2cloud.devops.common.PermissionConstants;
import com.fit2cloud.devops.dto.request.DashboardBuildSearchReq;
import com.fit2cloud.devops.dto.request.DashboardDeploySearchReq;
import com.fit2cloud.devops.dto.request.DashboardGroupAnalysisReq;
import com.fit2cloud.devops.service.DashboardService;
import com.fit2cloud.devops.vo.GroupAnalysisVO;
import io.swagger.annotations.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@Api(tags = {"概览"})
@RequestMapping("dashboard")
public class DashboardController {

    @Resource
    private DashboardService dashboardService;

    @ApiOperation("获取各个工作空间的构建和部署情况")
    @PostMapping("getGroupAnalysis/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.DASHBOARD_GROUP_ANALYSIS_READ)
    public Pager<List<GroupAnalysisVO>> getGroupAnalysis(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody DashboardGroupAnalysisReq params) {
        return dashboardService.getGroupAnalysis(goPage,pageSize, BeanUtils.objectToMap(params));
    }

    @PostMapping("getBuildTimeDistribution")
    @RequiresPermissions(PermissionConstants.DASHBOARD_READ)
    @ApiOperation("获取构建时间分布")
    public List<ChartData> getBuildTimeDistribution(@RequestBody DashboardBuildSearchReq params) {
        return dashboardService.getBuildTimeDistribution(BeanUtils.objectToMap(params));
    }

    @PostMapping("getBuildSuccessTrend")
    @ApiOperation("获取构建成功率趋势图")
    @RequiresPermissions(PermissionConstants.DASHBOARD_READ)
    public List<ChartData> getBuildSuccessTrend(@RequestBody DashboardBuildSearchReq params) {
        return dashboardService.getBuildSuccessTrend(BeanUtils.objectToMap(params));
    }

    @PostMapping("getBuildWeekdayDistribution")
    @RequiresPermissions(PermissionConstants.DASHBOARD_READ)
    @ApiOperation("获取构建星期分布图")
    public List<ChartData> getBuildWeekdayDistribution(@RequestBody DashboardBuildSearchReq params) {
        return dashboardService.getBuildWeekdayDistribution(BeanUtils.objectToMap(params));
    }

    @PostMapping("getBuildJobCountTrend")
    @RequiresPermissions(PermissionConstants.DASHBOARD_READ)
    @ApiOperation("获取构建任务数量趋势图")
    public List<ChartData> getBuildJobCountTrend(@RequestBody DashboardBuildSearchReq params) {
        return dashboardService.getBuildJobCountTrend(BeanUtils.objectToMap(params));
    }

    @PostMapping("getBuildJobBuildCountTrend")
    @RequiresPermissions(PermissionConstants.DASHBOARD_READ)
    @ApiOperation("获取构建任务构建次数趋势图")
    public List<ChartData> getBuildJobBuildCountTrend(@RequestBody DashboardBuildSearchReq params) {
        return dashboardService.getBuildJobBuildCountTrend(BeanUtils.objectToMap(params));
    }

    @PostMapping("getDeployTimeDistribution")
    @RequiresPermissions(PermissionConstants.DASHBOARD_READ)
    @ApiOperation("获取部署时间分布图")
    public List<ChartData> getDeployTimeDistribution(@RequestBody DashboardDeploySearchReq params) {
        return dashboardService.getDeployTimeDistribution(BeanUtils.objectToMap(params));
    }

    @PostMapping("getDeploySuccessTrend")
    @RequiresPermissions(PermissionConstants.DASHBOARD_READ)
    @ApiOperation("获取部署成功率趋势图")
    public List<ChartData> getDeploySuccessTrend(@RequestBody DashboardDeploySearchReq params) {
        return dashboardService.getDeploySuccessTrend(BeanUtils.objectToMap(params));
    }

    @PostMapping("getDeployWeekdayDistribution")
    @RequiresPermissions(PermissionConstants.DASHBOARD_READ)
    @ApiOperation("获取部署星期分布图")
    public List<ChartData> getDeployWeekdayDistribution(@RequestBody DashboardDeploySearchReq params) {
        return dashboardService.getDeployWeekdayDistribution(BeanUtils.objectToMap(params));
    }

    @PostMapping("getDeployAppCountTrend")
    @RequiresPermissions(PermissionConstants.DASHBOARD_READ)
    @ApiOperation("获取应用数量趋势图")
    public List<ChartData> getDeployAppCountTrend(@RequestBody DashboardDeploySearchReq params) {
        return dashboardService.getDeployAppCountTrend(BeanUtils.objectToMap(params));
    }

    @PostMapping("getDeployAppDeployCountTrend")
    @RequiresPermissions(PermissionConstants.DASHBOARD_READ)
    @ApiOperation("获取应用部署次数趋势图")
    public List<ChartData> getDeployAppDeployCountTrend(@RequestBody DashboardDeploySearchReq params) {
        return dashboardService.getDeployAppDeployCountTrend(BeanUtils.objectToMap(params));
    }


}
