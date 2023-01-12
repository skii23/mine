package com.fit2cloud.devops.controller.application;


import com.fit2cloud.commons.utils.BeanUtils;
import com.fit2cloud.commons.utils.PageUtils;
import com.fit2cloud.commons.utils.Pager;
import com.fit2cloud.devops.base.domain.ApplicationVersion;
import com.fit2cloud.devops.common.PermissionConstants;
import com.fit2cloud.devops.common.model.FileTreeNode;
import com.fit2cloud.devops.dto.ApplicationRepositoryDTO;
import com.fit2cloud.devops.dto.ApplicationVersionDTO;
import com.fit2cloud.devops.dto.request.ApplicationVersionRequest;
import com.fit2cloud.devops.service.ApplicationVersionService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Api(tags = "应用版本")
@RestController
@RequestMapping("application/version")
public class ApplicationVersionController {

    @Resource
    private ApplicationVersionService applicationVersionService;

    @ApiOperation("应用版本列表")
    @RequiresPermissions(PermissionConstants.APPLICATION_VERSION_READ)
    @PostMapping("list/{goPage}/{pageSize}")
    public Pager getApplicationVersions(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ApplicationVersionRequest applicationVersionRequest) {
        Page page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, applicationVersionService.selectApplicationVersions(BeanUtils.objectToMap(applicationVersionRequest)));
    }


    @ApiOperation("保存应用版本")
    @RequiresPermissions(PermissionConstants.APPLICATION_VERSION_CREATE)
    @PostMapping("save")
    public ApplicationVersion saveApplicationVersion(@RequestBody ApplicationVersionDTO applicationVersion) {
        return applicationVersionService.saveApplicationVersion(applicationVersion);
    }

    @RequiresPermissions(PermissionConstants.APPLICATION_VERSION_CREATE)
    @GetMapping("file/tree")
    public FileTreeNode genFileTree(@RequestParam String envId, @RequestParam String applicationId) {
        return applicationVersionService.getFileTree(envId, applicationId);
    }

    @RequiresPermissions(PermissionConstants.APPLICATION_VERSION_CREATE)
    @GetMapping("file/proxytree")
    public FileTreeNode genFileProxyTree(@RequestParam String envId, @RequestParam String applicationId) {
        return applicationVersionService.getFileProxyTree(envId, applicationId);
    }
    @ApiOperation("删除应用版本")
    @RequiresPermissions(PermissionConstants.APPLICATION_VERSION_DELETE)
    @PostMapping("delete")
    public void deleteApplicationVersion(@RequestBody String applicationVersionId) throws Exception {
        applicationVersionService.deleteApplicationVersion(applicationVersionId);
    }

    @RequiresPermissions(PermissionConstants.APPLICATION_VERSION_CREATE)
    @PostMapping("file/tree/subNodes")
    public List<FileTreeNode> getSubNodes(@RequestBody ApplicationRepositoryDTO applicationRepositoryDTO) {
        return applicationVersionService.getSubFileNodes(applicationRepositoryDTO);
    }

    @ApiOperation("获取应用部署成功的最新版本")
    @RequiresPermissions(PermissionConstants.APPLICATION_VERSION_READ)
    @GetMapping("getLatestVersion")
    public ApplicationVersion getLatestVersion(@RequestParam String applicationId) {
        return applicationVersionService.getLatestVersion(applicationId);
    }
}
