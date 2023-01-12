package com.fit2cloud.devops.controller;

import com.fit2cloud.commons.utils.ResultHolder;
import com.fit2cloud.devops.service.ApplicationService;
import com.fit2cloud.devops.service.DevopsJiraService;
import com.netflix.ribbon.proxy.annotation.Http.Header;

import org.apache.commons.lang3.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author caiwzh
 * @date 2022/11/2
 */
@RestController
@Api(tags = "度量API")
@RequestMapping("measure")
public class DevopsMeasureController {

    @Resource
    private DevopsJiraService devopsJiraService;
    @Resource
    private ApplicationService applicationService;

    @ApiOperation("获取冲刺列表")
    @GetMapping("/sprint/list/{goPages}/{pageSize}")
    public ResultHolder getAllSprite(@PathVariable Integer goPages, @PathVariable Integer pageSize) {
        return ResultHolder.success(devopsJiraService.getAllSprite(goPages, pageSize));
    }

    @ApiOperation("获取某个冲刺详情")
    @GetMapping("/sprint/metrics/{sprintId}")
    public ResultHolder getAllSprite(@PathVariable String sprintId) {
        return ResultHolder.success(devopsJiraService.spritInfo(sprintId.split("-")[0], sprintId.split("-")[1]));
    }

    @ApiOperation("获取最新的冲刺详情")
    @GetMapping("/sprint/metrics/latest")
    public ResultHolder getLatest() {
        return ResultHolder.success(devopsJiraService.getLatest());
    }

    @ApiOperation("获取某个冲刺的需求趋势")
    @GetMapping("/sprint/{id}/issue/{time}")
    public ResultHolder group(@PathVariable String id, @PathVariable Integer time) {
        String[] split = id.split("-");
        String originBoardId = split[0], sprintId = split[1];
        return ResultHolder.success(devopsJiraService.time(time, originBoardId, sprintId));
    }

    @ApiOperation("查询应用概要")
    @GetMapping("/app/{id}/overall")
    public ResultHolder getAppCodeInfo(@PathVariable String id) {
        return ResultHolder.success(applicationService.getAppOverall(id));
    }

    @ApiOperation("查询应用代码信息")
    @GetMapping("/app/{id}/code/{goPages}/{pageSize}")
    public ResultHolder getAppCodeInfo(@PathVariable String id, @PathVariable Integer goPages, @PathVariable Integer pageSize) {
        return ResultHolder.success(applicationService.getAppCodeInfo(id, goPages, pageSize));
    }

    @ApiOperation("查询每日commit数据")
    @GetMapping("/app/{id}/code/dailyCommit/{time}")
    public ResultHolder dailyCommit(@PathVariable String id, @PathVariable Integer time, @RequestHeader(value = "repoId", required = false) String repoId) {
        if (StringUtils.isBlank(repoId)) {
            repoId = "all";
        }
        return ResultHolder.success(applicationService.groupByCommit(id, repoId, time));
    }

    @ApiOperation("查询每日commit数据")
    @GetMapping("/app/{id}/code/dailyCommitter/{time}")
    public ResultHolder dailyCommitter(@PathVariable String id, @PathVariable Integer time, @RequestHeader(value = "repoId", required = false) String repoId) {
        if (StringUtils.isBlank(repoId)) {
            repoId = "all";
        }
        return ResultHolder.success(applicationService.groupByCommitter(id, repoId, time));
    }

    @ApiOperation("查询应用构建信息")
    @GetMapping("/app/{id}/build/{goPages}/{pageSize}")
    public ResultHolder getAppBuild(@PathVariable String id, @PathVariable Integer goPages, @PathVariable Integer pageSize) {
        return ResultHolder.success(applicationService.getBuildInfo(id, goPages, pageSize));
    }

    @ApiOperation("查询每日构建次数数据")
    @GetMapping("/app/{id}/build/{repoId}/dailyBuild/{time}")
    public ResultHolder dailyBuild(@PathVariable String id, @PathVariable String repoId, @PathVariable Integer time, @RequestParam(required = false) String status) {
        return ResultHolder.success(applicationService.groupByBuild(id, repoId, time, status));
    }

    @ApiOperation("查询每日平均构建时长")
    @GetMapping("/app/{id}/build/{jobId}/dailyBuildTime/{time}")
    public ResultHolder dailyBuildTime(@PathVariable String id, @PathVariable String jobId, @PathVariable Integer time, @RequestParam(required = false) String status) {
        return ResultHolder.success(applicationService.groupByBuildTime(id, jobId, time, status));
    }

    @ApiOperation("查询部署信息")
    @GetMapping("/app/{id}/deploy/{goPages}/{pageSize}")
    public ResultHolder getAppDeploy(@PathVariable String id, @PathVariable Integer goPages, @PathVariable Integer pageSize) {
        return ResultHolder.success(applicationService.getDeployInfo(id, goPages, pageSize));
    }

    @ApiOperation("查询API测试信息")
    @GetMapping("/app/{id}/apiTest/{goPages}/{pageSize}")
    public ResultHolder getAppTest(@PathVariable String id, @PathVariable Integer goPages, @PathVariable Integer pageSize) {
        return ResultHolder.success(applicationService.getAppTestInfo(id, goPages, pageSize));
    }
}
