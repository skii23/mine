package com.fit2cloud.devops.service;


import com.fit2cloud.commons.server.base.domain.TagMapping;
import com.fit2cloud.commons.server.constants.RoleConstants;
import com.fit2cloud.commons.server.exception.F2CException;
import com.fit2cloud.commons.server.model.SessionUser;
import com.fit2cloud.commons.server.service.TagMappingService;
import com.fit2cloud.commons.server.utils.SessionUtils;
import com.fit2cloud.commons.utils.UUIDUtil;
import com.fit2cloud.devops.base.domain.*;
import com.fit2cloud.devops.base.mapper.ApplicationRepositoryMapper;
import com.fit2cloud.devops.base.mapper.ApplicationRepositorySettingMapper;
import com.fit2cloud.devops.base.mapper.ApplicationVersionMapper;
import com.fit2cloud.devops.common.consts.ERepositoryType;
import com.fit2cloud.devops.common.model.FileTreeNode;
import com.fit2cloud.devops.common.util.CommonUtils;
import com.fit2cloud.devops.dao.ext.ExtApplicationVersionMapper;
import com.fit2cloud.devops.dto.ApplicationRepositoryDTO;
import com.fit2cloud.devops.dto.ApplicationVersionDTO;
import com.fit2cloud.devops.service.oss.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApplicationVersionService {

    @Resource
    private ApplicationVersionMapper applicationVersionMapper;
    @Resource
    private AliyunOSSService aliyunOSSService;
    @Resource
    private AWSS3Service awss3Service;
    @Resource
    private TagMappingService tagMappingService;
    @Resource
    private ApplicationSettingService applicationSettingService;
    @Resource
    private NexusService nexusService;
    @Resource
    private RepositoryService repositoryService;
    @Resource
    private ExtApplicationVersionMapper extApplicationVersionMapper;
    @Resource
    private ArtifactoryService artifactoryService;
    @Resource
    private ApplicationRepositorySettingMapper applicationRepositorySettingMapper;
    @Resource
    private ApplicationRepositoryMapper applicationRepositoryMapper;
    @Resource
    private Nexus3Service nexus3Service;

    public List<ApplicationVersionDTO> selectApplicationVersions(Map<String, Object> params) {
        CommonUtils.filterPermission(params);
        return extApplicationVersionMapper.selectApplicationVersions(params);
    }

    public ApplicationVersion saveApplicationVersion(ApplicationVersionDTO applicationVersion) {
        checkSave(applicationVersion);
        applicationVersion.setCreatedTime(System.currentTimeMillis());
        applicationVersion.setId(UUIDUtil.newUUID());
        ApplicationRepositorySettingExample applicationRepositorySettingExample = new ApplicationRepositorySettingExample();
        applicationRepositorySettingExample.createCriteria().andEnvIdEqualTo(applicationVersion.getEnvironmentValueId()).andApplicationIdEqualTo(applicationVersion.getApplicationId());
        ApplicationRepositorySetting applicationRepositorySetting = applicationRepositorySettingMapper.selectByExample(applicationRepositorySettingExample).get(0);
        applicationVersion.setApplicationRepositoryId(applicationRepositorySetting.getRepositoryId());
        if (!applicationVersion.getEnvironmentValueId().equalsIgnoreCase("ALL")) {
            TagMapping tagMapping = new TagMapping();
            tagMapping.setResourceId(applicationVersion.getId());
            tagMapping.setTagKey("environment");
            tagMapping.setResourceType("APPLICATION_VERSION");
            tagMapping.setTagValueId(applicationVersion.getEnvironmentValueId());
            try {
                tagMappingService.saveTagMapping(tagMapping);
            } catch (Exception e) {
                F2CException.throwException("添加标签失败！");
            }
        }

        applicationVersionMapper.insert(applicationVersion);
        return applicationVersion;
    }


    public void updateLastDeployTime(String applicationVersionId, Long time) {
        ApplicationVersion applicationVersion = new ApplicationVersion();
        applicationVersion.setLastDeploymentTime(time);
        applicationVersion.setId(applicationVersionId);
        applicationVersionMapper.updateByPrimaryKeySelective(applicationVersion);
    }

    public void deleteApplicationVersion(String versionId) {
        applicationVersionMapper.deleteByPrimaryKey(versionId);
    }

    public ApplicationVersion getApplicationVersion(String id) {
        return applicationVersionMapper.selectByPrimaryKey(id);
    }

    private void checkSave(ApplicationVersion applicationVersion) {

        ApplicationVersionExample applicationVersionExample = new ApplicationVersionExample();
        applicationVersionExample.createCriteria().andNameEqualTo(applicationVersion.getName())
                .andApplicationIdEqualTo(applicationVersion.getApplicationId());

        List<ApplicationVersion> applicationVersions = applicationVersionMapper.selectByExample(applicationVersionExample);
        if (applicationVersions.size() != 0) {
            F2CException.throwException("版本：" + applicationVersion.getName() + "已存在！");
        }
    }


    public String getArtifactUrl(ApplicationVersion applicationVersion) {
        ApplicationRepository applicationRepository = repositoryService.getById(applicationVersion.getApplicationRepositoryId());
        ERepositoryType type = ERepositoryType.fromValue(applicationRepository.getType());
        String url;
        switch (type) {
            case OSS:
                url = aliyunOSSService.genDownloadURL(applicationRepository, applicationVersion.getLocation());
                break;
            case S3:
                url = awss3Service.genDownloadURL(applicationRepository, applicationVersion.getLocation());
                break;
            case NEXUS:
                url = nexusService.genDownloadURL(applicationRepository, applicationVersion.getLocation());
                break;
            case NEXUS3:
                url = nexus3Service.genDownloadURL(applicationRepository, applicationVersion.getLocation());
                break;
            case ARTIFACTORY:
                url = artifactoryService.genDownloadURL(applicationRepository, applicationVersion.getLocation());
                break;
            default:
                url = null;
        }
        return url;
    }

    public FileTreeNode getFileTree(String envId, String applicationId) {
        //查询仓库
        ApplicationRepository applicationRepository = repositoryService.queryRepo(envId, applicationId);
        FileTreeNode fileTreeNode;
        ERepositoryType type = ERepositoryType.fromValue(applicationRepository.getType());
        switch (type) {
            case OSS:
                fileTreeNode = aliyunOSSService.genFileTree(applicationRepository);
                break;
            case S3:
                fileTreeNode = awss3Service.genFileTree(applicationRepository);
                break;
            case NEXUS:
                fileTreeNode = nexusService.getTree(applicationRepository);
                break;
            case NEXUS3:
                fileTreeNode = nexus3Service.genFileTree(applicationRepository);
                break;
            case ARTIFACTORY:
                fileTreeNode = artifactoryService.getTree(applicationRepository);
                break;
            default:
                fileTreeNode = null;
        }
        return fileTreeNode;
    }

    /**
     * 代理仓库目录查看，目前只支持NEXUS
     * @param envId
     * @param applicationId
     * @return
     */
    public FileTreeNode getFileProxyTree(String envId, String applicationId) {
        //查询仓库
        ApplicationRepository applicationRepository = repositoryService.queryRepo(envId, applicationId);
        FileTreeNode fileTreeNode;
        ERepositoryType type = ERepositoryType.fromValue(applicationRepository.getType());
        switch (type) {
            case NEXUS:
                fileTreeNode = nexusService.getProxyTree(applicationRepository);
                break;
            default:
                fileTreeNode = null;
        }
        return fileTreeNode;
    }
    /**
     * nexus3懒加载使用
     *
     * @return 子节点列表
     */
    public List<FileTreeNode> getSubFileNodes(ApplicationRepositoryDTO applicationRepositoryDTO) {
        if (applicationRepositoryDTO.getType().equalsIgnoreCase(ERepositoryType.NEXUS3.name())){
            return nexus3Service.getSubNodes(applicationRepositoryDTO, applicationRepositoryDTO.getNode());
        }else {
            if("fromProxy".equals(applicationRepositoryDTO.getLocationType())){
                return nexusService.getProxySubNodes(applicationRepositoryDTO, applicationRepositoryDTO.getNode());
            }else {
                return nexusService.getSubNodes(applicationRepositoryDTO, applicationRepositoryDTO.getNode());
            }

        }
    }

    public ApplicationVersion getLatestVersion(String applicationId) {
        return extApplicationVersionMapper.getLatestVersion(applicationId);
    }
}
