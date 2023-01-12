package com.fit2cloud.devops.controller;

import com.fit2cloud.commons.utils.ResultHolder;
import com.fit2cloud.devops.service.DevopsMeasureAppQualityService;
import com.fit2cloud.devops.service.DevopsSonarqubeService;
import com.fit2cloud.devops.service.jenkins.DevopsJenkinsJobHistoryService;
import com.fit2cloud.devops.service.model.SonarqubeMetrics;
import com.fit2cloud.devops.vo.JobTestCaseVO;
import com.offbytwo.jenkins.model.TestReport;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import com.fit2cloud.devops.common.util.RetUtils;
import javax.annotation.Resource;

@RestController
@Api(tags = "度量-构建任务质量指标")
@RequestMapping("measure/job/")
public class DevopsMeasureJobQualityController {

    @Resource
    private DevopsSonarqubeService sonarService;

    @Resource
    private DevopsJenkinsJobHistoryService jenkinsJobHistoryService;

    private static final Integer MAX_PAGE_SIZE = 500; // 限制破坏式查询

    @ApiOperation("通过名称获取sonarqube指标")
    @GetMapping("sonar/name/{name}")
    public ResultHolder getSonarByName(@PathVariable String name, @RequestParam(required = false) String status) {
        Boolean isNew = false;
        if (StringUtils.equalsIgnoreCase(status, "new")) {
            isNew = true;
        }
        SonarqubeMetrics rsp = sonarService.getSonarqubeMerticsByName(name, isNew);
        if (rsp == null) {
            ResultHolder.error(name);
        }
        return ResultHolder.success(rsp);
    }

    @ApiOperation("通过id获取sonarqube指标")
    @GetMapping("sonar/id/{id}")
    public ResultHolder getSonarById(@PathVariable String id, @RequestParam(required = false) String status) {
        Boolean isNew = false;
        if (StringUtils.equalsIgnoreCase(status, "new")) {
            isNew = true;
        }
        SonarqubeMetrics rsp = sonarService.getSonarqubeMerticsByName(id, isNew);
        if (rsp == null) {
            ResultHolder.error("get job by " + id + " fail.");
        }
        return ResultHolder.success(rsp);
    }

    @ApiOperation("通过名称获取unitTest指标")
    @GetMapping("unitTest/name/{name}")
    public ResultHolder getTestCaseByName(@PathVariable String name) {
        TestReport rsp = jenkinsJobHistoryService.getJenkinsJobTestReportByName(name);
        if (rsp == null) {
            ResultHolder.error(name);
        }
        JobTestCaseVO test = new JobTestCaseVO();
        test.setUniTestCaseCount(rsp.getTotalCount());
        test.setUniTestFailCount(rsp.getFailCount());
        test.setUniTestSkipCount(rsp.getSkipCount());
        return ResultHolder.success(test);
    }

    @ApiOperation("通过id获取unitTest指标")
    @GetMapping("unitTest/id/{id}")
    public ResultHolder getTestCaseById(@PathVariable String id) {
        TestReport rsp = jenkinsJobHistoryService.getJenkinsJobTestReportById(id);
        if (rsp == null) {
            ResultHolder.error(id);
        }
        JobTestCaseVO test = new JobTestCaseVO();
        test.setUniTestCaseCount(rsp.getTotalCount());
        test.setUniTestFailCount(rsp.getFailCount());
        test.setUniTestSkipCount(rsp.getSkipCount());
        return ResultHolder.success(test);
    }
}
