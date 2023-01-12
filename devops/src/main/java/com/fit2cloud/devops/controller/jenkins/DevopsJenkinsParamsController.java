package com.fit2cloud.devops.controller.jenkins;

import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.commons.utils.PageUtils;
import com.fit2cloud.commons.utils.Pager;
import com.fit2cloud.devops.base.domain.DevopsJenkinsParams;
import com.fit2cloud.devops.common.PermissionConstants;
import com.fit2cloud.devops.service.jenkins.DevopsJenkinsParamsService;
import com.fit2cloud.devops.service.jenkins.DevopsJenkinsSystemConfigService;
import com.fit2cloud.devops.service.jenkins.model.sysconfig.JenkinsSystemConfigParser;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("jenkins/params")
@Api(tags = "Jenkins参数")
public class DevopsJenkinsParamsController {
    @Resource
    private DevopsJenkinsParamsService devopsJenkinsParamsService;

    @Resource
    private DevopsJenkinsSystemConfigService devopsJenkinsSystemConfigService;

    @ApiOperation("分页获取参数")
    @PostMapping("list/{goPage}/{pageSize}")
    public Pager listParams(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody DevopsJenkinsParams devopsJenkinsParams) {
        Page page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, devopsJenkinsParamsService.getParams(devopsJenkinsParams));
    }

    @ApiOperation("获取参数")
    @PostMapping("getParams")
    public List<DevopsJenkinsParams> getParams(@RequestBody DevopsJenkinsParams devopsJenkinsParams) {
        return devopsJenkinsParamsService.getParams(devopsJenkinsParams);
    }

    @ApiOperation("保存或者编辑参数")
    @PostMapping("save")
    @RequiresPermissions({PermissionConstants.JENKINS_PARAMS_UPDATE, PermissionConstants.JENKINS_PARAMS_CREATE})
    public void saveParams(@RequestBody List<JSONObject> data) {
        devopsJenkinsParamsService.saveParams(data);
    }

    @ApiOperation("删除参数")
    @PostMapping("delete")
    @RequiresPermissions(PermissionConstants.JENKINS_PARAMS_DELETE)
    public void deleteParams(@RequestBody List<DevopsJenkinsParams> params) {
        devopsJenkinsParamsService.deleteParams(params);
    }

    @ApiOperation("获取多分支流水线仓库")
    @PostMapping("{type}/getServers")
    public Object getServers(@PathVariable String type){
        if(StringUtils.equalsIgnoreCase(type, JenkinsSystemConfigParser.GITLAB.name())){
            return devopsJenkinsSystemConfigService.getDbGitLabServers();
        }
        if(StringUtils.equalsIgnoreCase(type, JenkinsSystemConfigParser.GITEA.name())){
            return devopsJenkinsSystemConfigService.getDBGiteaServers();
        }
        return null;
    }

    @PostMapping("/fillProjectPathItems")
    public Object fillProjectPathItems(@RequestBody JSONObject data){
        return devopsJenkinsSystemConfigService.fillProjectPathItems(data);
    }

    @GetMapping("/getSonarParams")
    public Map<String,List<String>> getSonarParams(){
        return devopsJenkinsSystemConfigService.getSonarParams();
    }

    @GetMapping("/getToolParams")
    public Object getToolParams(){
        return devopsJenkinsSystemConfigService.getToolParams();
    }
}
