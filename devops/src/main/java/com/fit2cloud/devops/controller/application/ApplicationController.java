package com.fit2cloud.devops.controller.application;

import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.commons.server.base.domain.TagValue;
import com.fit2cloud.commons.server.model.ExcelExportRequest;
import com.fit2cloud.commons.server.service.TagService;
import com.fit2cloud.commons.utils.BeanUtils;
import com.fit2cloud.commons.utils.PageUtils;
import com.fit2cloud.commons.utils.Pager;
import com.fit2cloud.commons.utils.ResultHolder;
import com.fit2cloud.devops.base.domain.Application;
import com.fit2cloud.devops.common.PermissionConstants;
import com.fit2cloud.devops.dto.ApplicationDTO;
import com.fit2cloud.devops.dto.request.ApplicationRequest;
import com.fit2cloud.devops.service.ApplicationService;
import com.fit2cloud.devops.service.ApplicationSettingService;
import com.fit2cloud.devops.service.jenkins.DevopsJenkinsService;
import com.fit2cloud.devops.service.openapi.XOceanOpenApiService;
import com.fit2cloud.devops.service.openapi.model.Environment;
import com.fit2cloud.devops.service.openapi.model.Product;
import com.fit2cloud.devops.service.openapi.model.TestPlan;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Api(tags = "应用")
@RequestMapping("application")
public class ApplicationController {

    @Resource
    private ApplicationService applicationService;
    @Resource
    private ApplicationSettingService applicationSettingService;
    @Resource
    private TagService tagService;
    @Resource
    private DevopsJenkinsService devopsJenkinsService;
    @Resource
    private XOceanOpenApiService xOceanOpenApiService;


    @ApiOperation("创建应用")
    @PostMapping("save")
    @RequiresPermissions(PermissionConstants.APPLICATION_CREATE)
    public Application createApplication(@RequestBody ApplicationDTO applicationDTO) {
        return applicationService.saveApplication(applicationDTO);
    }

    @GetMapping("{id}")
    @RequiresPermissions(PermissionConstants.APPLICATION_READ)
    public Application getApp(@PathVariable String id){
        return applicationService.queryById(id);
    }

    @ApiOperation("编辑应用")
    @PostMapping("update")
    @RequiresPermissions(PermissionConstants.APPLICATION_UPDATE)
    public Application updateApplication(@RequestBody ApplicationDTO applicationDTO) {
        return applicationService.saveApplication(applicationDTO);
    }

    @ApiOperation("查看应用列表")
    @PostMapping("list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.APPLICATION_READ)
    public Pager getApps(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ApplicationRequest applicationRequest) {
        Page page = PageHelper.startPage(goPage, pageSize, true);

        return PageUtils.setPageInfo(page, applicationService.selectApplications(BeanUtils.objectToMap(applicationRequest)));
    }

    @ApiOperation("获取应用信息")
    @GetMapping("detail")
    public ResultHolder detail(HttpServletRequest httpServletRequest) {
        String jobName = httpServletRequest.getHeader("jobName");
        if(StringUtils.isBlank(jobName)){
            jobName = httpServletRequest.getParameter("jobName");
            if(StringUtils.isBlank(jobName)) {
                return ResultHolder.error("jobName 不能为空");
            }
        }
        return devopsJenkinsService.applicationInfo(jobName);
    }

    @ApiOperation("构建后操作")
    @PostMapping("buildResult")
    public ResultHolder postBuildApps(@RequestBody JSONObject data) {
        return devopsJenkinsService.postBuildApps(data);
    }

    @ApiOperation("删除应用")
    @PostMapping("delete")
    @RequiresPermissions(PermissionConstants.APPLICATION_DELETE)
    public void deleteApplication(@RequestBody String applicationId) throws Exception {
        applicationService.deleteApplication(applicationId);
    }

    @GetMapping("list")
    @RequiresPermissions(PermissionConstants.APPLICATION_READ)
    public List<ApplicationDTO> getApps() {
        return applicationService.selectApplications(new HashMap());
    }


    @GetMapping("setting/env/list")
    public List<TagValue> getEnvTag() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("tagKey", "environment");
        return tagService.selectTagValues(params);
    }


    @GetMapping("env/list")
    public List<TagValue> getEnvs(@RequestParam String applicationId) {
        return applicationSettingService.getEnvsByApplicationId(applicationId);
    }

    @PostMapping("export")
    @RequiresPermissions(PermissionConstants.APPLICATION_READ)
    public ResponseEntity<byte[]> exportApplication(@RequestBody ExcelExportRequest excelExportRequest) throws Exception {
        byte[] bytes = applicationService.exportCloudServers(excelExportRequest);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "导出");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .headers(headers)
                .body(bytes);
    }

    @GetMapping("test/prod/list")
    public List<Product> getProds() {
        return xOceanOpenApiService.getProductList();
    }

    @GetMapping("test/plan/list")
    public List<TestPlan> getPlans(@RequestParam String prodId) {
        return xOceanOpenApiService.getTestPlan(prodId);
    }

    @GetMapping("test/env/list")
    public List<Environment> getTestEnv(@RequestParam String prodId) {
        return xOceanOpenApiService.getEnvironment(prodId);
    }

    @GetMapping("test/check")
    public Object checkTestParam(String id){
        return applicationService.checkTestParam(id);
    }
}
