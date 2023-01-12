package com.fit2cloud.devops.controller.jenkins;

import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.commons.utils.CommonThreadPool;
import com.fit2cloud.commons.utils.PageUtils;
import com.fit2cloud.commons.utils.Pager;
import com.fit2cloud.commons.utils.ResultHolder;
import com.fit2cloud.devops.base.domain.DevopsJenkinsCredential;
import com.fit2cloud.devops.base.domain.DevopsJenkinsCredentialWithBLOBs;
import com.fit2cloud.devops.common.PermissionConstants;
import com.fit2cloud.devops.dto.DevopsJenkinsCredentialDto;
import com.fit2cloud.devops.service.jenkins.DevopsJenkinsCredentialService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@Api(tags = "Jenkins凭据")
@RequestMapping("jenkins/credential")
public class DevopsJenkinsCredentialController {

    @Resource
    private DevopsJenkinsCredentialService devopsJenkinsCredentialService;

    @Resource
    private CommonThreadPool commonThreadPool;

    @ApiOperation("分页查看凭据")
    @RequiresPermissions(PermissionConstants.JENKINS_CREDENTIAL_READ)
    @PostMapping("list/{goPage}/{pageSize}")
    public Pager<List<DevopsJenkinsCredentialDto>> listCredentials(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody Map<String, Object> params) {
        Page page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, devopsJenkinsCredentialService.getCredential(params));
    }

    @ApiOperation("查看凭据")
    @RequiresPermissions(PermissionConstants.JENKINS_CREDENTIAL_READ)
    @PostMapping("getCredentials")
    public List<DevopsJenkinsCredentialDto> getCredentials(@RequestBody Map<String, Object> params) {
        return devopsJenkinsCredentialService.getCredential(params);
    }

    @ApiOperation("同步凭据")
    @RequiresPermissions(PermissionConstants.JENKINS_CREDENTIAL_UPDATE)
    @PostMapping("syncCredentials")
    public void syncCredentials(@RequestBody List<DevopsJenkinsCredentialWithBLOBs> credentials) {
        commonThreadPool.addTask(() -> devopsJenkinsCredentialService.syncCredentials(credentials));
    }

    @ApiOperation("同步所有凭据")
    @RequiresPermissions(PermissionConstants.JENKINS_CREDENTIAL_UPDATE)
    @GetMapping("syncAllCredentials")
    public void syncAllCredentials() {
        commonThreadPool.addTask(() -> devopsJenkinsCredentialService.syncAllCredentials());
    }

    @ApiOperation("授权工作空间")
    @PostMapping("grantCredential")
    @RequiresPermissions(PermissionConstants.JENKINS_CREDENTIAL_GRANT)
    public void grantCredential(@RequestBody List<DevopsJenkinsCredentialWithBLOBs> devopsJenkinsCredentials) {
        devopsJenkinsCredentialService.grantCredential(devopsJenkinsCredentials);
    }

    @ApiOperation("保存凭据")
    @PostMapping("save")
    @RequiresPermissions({PermissionConstants.JENKINS_CREDENTIAL_CREATE,PermissionConstants.JENKINS_CREDENTIAL_UPDATE})
    public ResultHolder createCredential(@RequestBody JSONObject jsonObject) {
        return devopsJenkinsCredentialService.saveCredential(jsonObject);
    }

    @ApiOperation("删除凭据")
    @PostMapping("delete")
    @RequiresPermissions(PermissionConstants.JENKINS_CREDENTIAL_DELETE)
    public void deleteCredentials(@RequestBody List<DevopsJenkinsCredential> devopsJenkinsCredentials) {
        devopsJenkinsCredentialService.deleteCredentials(devopsJenkinsCredentials);
    }

}
