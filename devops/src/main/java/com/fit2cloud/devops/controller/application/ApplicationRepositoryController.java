package com.fit2cloud.devops.controller.application;

import com.fit2cloud.commons.utils.PageUtils;
import com.fit2cloud.commons.utils.Pager;
import com.fit2cloud.devops.base.domain.ApplicationRepository;
import com.fit2cloud.devops.common.PermissionConstants;
import com.fit2cloud.devops.common.consts.ERepositoryType;
import com.fit2cloud.devops.common.model.Bucket;
import com.fit2cloud.devops.common.model.Repository;
import com.fit2cloud.devops.dto.ApplicationRepositoryDTO;
import com.fit2cloud.devops.service.oss.*;
import com.fit2cloud.devops.service.RepositoryService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections4.MapUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "制品库")
@RestController
@RequestMapping("repository")
public class ApplicationRepositoryController {

    @Resource
    private RepositoryService repositoryService;
    @Resource
    private AliyunOSSService aliyunOSSService;
    @Resource
    private AWSS3Service awss3Service;
    @Resource
    private NexusService nexusService;
    @Resource
    private ArtifactoryService artifactoryService;
    @Resource
    private Nexus3Service nexus3Service;
    @Resource
    private HarborService harborService;

    @ApiOperation("制品库列表")
    @PostMapping("list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.APPLICATION_REPOSITORY_READ)
    public Pager getRepositories(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody Map params) {
        Page page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, repositoryService.selectApplicationRepositorys(params));
    }

    @ApiOperation("创建制品库")
    @PostMapping("create")
    @RequiresPermissions(PermissionConstants.APPLICATION_REPOSITORY_CREATE)
    public void saveRepository(@RequestBody ApplicationRepository applicationRepository) {
        repositoryService.saveRepository(applicationRepository);
    }

    @ApiOperation("修改制品库")
    @PostMapping("update")
    @RequiresPermissions(PermissionConstants.APPLICATION_REPOSITORY_UPDATE)
    public void updateRepository(@RequestBody ApplicationRepository applicationRepository) {
        repositoryService.saveRepository(applicationRepository);
    }

    @ApiOperation("删除制品库")
    @PostMapping("delete")
    @RequiresPermissions(PermissionConstants.APPLICATION_REPOSITORY_DELETE)
    public void deleteRepository(@RequestBody String repositoryId) {
        repositoryService.deleteRepository(repositoryId);
    }


    @PostMapping("listAll")
    public List<ApplicationRepositoryDTO> getRepositorysAll() {
        return repositoryService.selectApplicationRepositorys(new HashMap<>());
    }

    @GetMapping("types")
    public List<String> getRepositoryTypes() {
        return repositoryService.getRepositoryTypes();
    }

    @PostMapping("check/{repositoryId}")
    public void checkRepository(@PathVariable String repositoryId) {
        repositoryService.checkRepository(repositoryId);
    }

    @PostMapping("bucket/list")
    public List<Bucket> getBuckets(@RequestBody Map body) {
        final String accesskey = MapUtils.getString(body, "accessKey");
        final String secretKey = MapUtils.getString(body, "secretKey");
        final String type = MapUtils.getString(body, "type");
        List<Bucket> buckets = null;
        ERepositoryType eRepositoryType = ERepositoryType.fromValue(type);
        switch (eRepositoryType) {
            case OSS:
                buckets = aliyunOSSService.getBuckets(accesskey, secretKey);
                break;
            case S3:
                buckets = awss3Service.listBucket(accesskey, secretKey);
                break;
            default:
                buckets = null;
        }
        return buckets;
    }

    @PostMapping("rep/list")
    public List<Repository> getRepositories(@RequestBody Map body) {
        final String accesskey = MapUtils.getString(body, "accessKey");
        final String secretKey = MapUtils.getString(body, "secretKey");
        final String path = MapUtils.getString(body, "path");
        final String type = MapUtils.getString(body, "type");
        List<Repository> repositories;
        ERepositoryType eRepositoryType = ERepositoryType.fromValue(type);
        switch (eRepositoryType) {
            case NEXUS:
                repositories = nexusService.listRepository(accesskey, secretKey, path);
                break;
            case ARTIFACTORY:
                repositories = artifactoryService.listRepository(accesskey, secretKey, path);
                break;
            case NEXUS3:
                repositories = nexus3Service.listRepository(accesskey, secretKey, path);
                break;
            case HARBOR:
                repositories = harborService.listProjects(accesskey, secretKey, path);
                break;
            default:
                repositories = null;
        }
        return repositories;
    }

    @GetMapping("query")
    public ApplicationRepository queryRepository(@RequestParam String envId, @RequestParam String applicationId) {
        return repositoryService.queryRepo(envId, applicationId);
    }

    @GetMapping("{repositoryId}")
    public ApplicationRepository getApplicationRepository(@PathVariable String repositoryId) {
        return repositoryService.getById(repositoryId);
    }

}
