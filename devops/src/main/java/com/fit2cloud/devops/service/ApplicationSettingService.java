package com.fit2cloud.devops.service;


import com.fit2cloud.commons.server.base.domain.TagValue;
import com.fit2cloud.commons.server.base.domain.TagValueExample;
import com.fit2cloud.commons.server.base.mapper.TagValueMapper;
import com.fit2cloud.commons.utils.UUIDUtil;
import com.fit2cloud.devops.base.domain.*;
import com.fit2cloud.devops.base.mapper.ApplicationRepositoryMapper;
import com.fit2cloud.devops.base.mapper.ApplicationRepositorySettingMapper;
import com.fit2cloud.devops.base.mapper.ApplicationSettingMapper;
import com.fit2cloud.devops.common.consts.ERepositoryType;
import com.fit2cloud.devops.common.model.FileTreeNode;
import com.fit2cloud.devops.dao.ext.ExtApplicationSettingMapper;
import com.fit2cloud.devops.dto.ApplicationSettingDTO;
import com.fit2cloud.devops.service.oss.AWSS3Service;
import com.fit2cloud.devops.service.oss.AliyunOSSService;
import com.fit2cloud.devops.service.oss.ArtifactoryService;
import com.fit2cloud.devops.service.oss.NexusService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApplicationSettingService {
    @Resource
    private ApplicationSettingMapper applicationSettingMapper;
    @Resource
    private AliyunOSSService aliyunOSSService;
    @Resource
    private AWSS3Service awss3Service;
    @Resource
    private RepositoryService repositoryService;
    @Resource
    private NexusService nexusService;
    @Resource
    private TagValueMapper tagValueMapper;
    @Resource
    private ExtApplicationSettingMapper extApplicationSettingMapper;
    @Resource
    private ArtifactoryService artifactoryService;
    @Resource
    private ApplicationRepositorySettingMapper applicationRepositorySettingMapper;

    @Resource
    private ApplicationRepositoryMapper applicationRepositoryMapper;

    static final String ENV_NAME = "环境";

    public void saveApplicationSettings(List<ApplicationSetting> applicationSettings, String applicationId) {
        ApplicationSettingExample applicationSettingExample = new ApplicationSettingExample();
        applicationSettingExample.createCriteria().andApplicationIdEqualTo(applicationId);

        List<ApplicationSetting> appsettings = applicationSettingMapper.selectByExample(applicationSettingExample);
        List<String> deleteIds = appsettings.stream().filter(appsetting -> {
            boolean flag = true;
            for (ApplicationSetting applicationSetting : applicationSettings) {
                if (StringUtils.equalsIgnoreCase(applicationSetting.getId(), appsetting.getId())) {
                    flag = false;
                }
            }
            return flag;
        }).map(ApplicationSetting::getId).collect(Collectors.toList());
        deleteIds.forEach(id -> applicationSettingMapper.deleteByPrimaryKey(id));
        applicationSettings.forEach(a -> {
            if (a.getNameRule() == null || a.getNameRule().equals("")) {
                a.setNameRule(".*");
            }
            if (a.getId() != null) {
                applicationSettingMapper.updateByPrimaryKey(a);
            } else {
                a.setId(UUIDUtil.newUUID());
                a.setCreatedTime(System.currentTimeMillis());
                applicationSettingMapper.insert(a);
            }
        });
    }

    private void deleteApplicationSettings(String applicationId) {
        ApplicationSettingExample applicationSettingExample = new ApplicationSettingExample();
        applicationSettingExample.createCriteria().andApplicationIdEqualTo(applicationId);
        applicationSettingMapper.deleteByExample(applicationSettingExample);
    }

    public List<ApplicationSettingDTO> getApplicationSettings(String appId) {
        Map<String, Object> params = new HashMap<>();
        params.put("applicationId", appId);
        return extApplicationSettingMapper.selectApplicationSettings(params);
    }


    public List<TagValue> getEnvsByApplicationId(String appId) {

        ApplicationRepositorySettingExample applicationRepositorySettingExample = new ApplicationRepositorySettingExample();
        applicationRepositorySettingExample.createCriteria().andApplicationIdEqualTo(appId);
        List<ApplicationRepositorySetting> applicationRepositorySettings = applicationRepositorySettingMapper.selectByExample(applicationRepositorySettingExample);

        List<TagValue> tagValues = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(applicationRepositorySettings)) {
            List<String> tagValueIds = applicationRepositorySettings.stream().map(ApplicationRepositorySetting::getEnvId).collect(Collectors.toList());
            TagValueExample tagValueExample = new TagValueExample();
            tagValueExample.createCriteria().andIdIn(tagValueIds);
            tagValues = tagValueMapper.selectByExample(tagValueExample);
            if (tagValueIds.contains("all")) {
                TagValue tagValue = new TagValue();
                tagValue.setId("all");
                tagValue.setTagValueAlias("全部环境");
                tagValues.add(tagValue);
            }
            Map<String, String> envId2RepoId = applicationRepositorySettings.stream().collect(Collectors.toMap(ApplicationRepositorySetting::getEnvId, ApplicationRepositorySetting::getRepositoryId, (k1, k2) -> k1));
            List<String> repoIds = applicationRepositorySettings.stream().map(ApplicationRepositorySetting::getRepositoryId).collect(Collectors.toList());

            ApplicationRepositoryExample applicationRepositoryExample = new ApplicationRepositoryExample();
            applicationRepositoryExample.createCriteria().andIdIn(repoIds);
            List<ApplicationRepository> applicationRepositories = applicationRepositoryMapper.selectByExample(applicationRepositoryExample);
            Map<String, String> repoMapping = applicationRepositories.stream().collect(Collectors.toMap(ApplicationRepository::getId, ApplicationRepository::getName, (k1, k2) -> k1));
            for (TagValue tagValue : tagValues) {
                if (!envId2RepoId.containsKey(tagValue.getId())) {
                    continue;
                }
                String repoId = envId2RepoId.get(tagValue.getId());
                String name = repoMapping.get(repoId);
                if (StringUtils.isNotBlank(name)) {
                    tagValue.setTagValueAlias(tagValue.getTagValueAlias() + "(" + name + ")");
                }
            }
        }
        return tagValues;
    }


    public ApplicationSetting getApplicationSetting(String appId, String envValueId) {
        ApplicationSetting applicationSetting = null;
        ApplicationSettingExample applicationSettingExample = new ApplicationSettingExample();
        applicationSettingExample.createCriteria().andApplicationIdEqualTo(appId).andEnvironmentValueIdEqualTo(envValueId);
        List<ApplicationSetting> applicationSettings = applicationSettingMapper.selectByExample(applicationSettingExample);
        if (CollectionUtils.isNotEmpty(applicationSettings)) {
            applicationSetting = applicationSettings.get(0);
        }
        return applicationSetting;
    }

    public FileTreeNode getFolderTree(String repositoryId) throws Exception {
        ApplicationRepository applicationRepository = repositoryService.getById(repositoryId);
        ERepositoryType type = ERepositoryType.fromValue(applicationRepository.getType());
        switch (type) {
            case OSS:
                return aliyunOSSService.genFileTree(applicationRepository);
            case S3:
                return awss3Service.genFileTree(applicationRepository);
            case NEXUS:
                return nexusService.getTree(applicationRepository);
            case ARTIFACTORY:
                return artifactoryService.getTree(applicationRepository);
            default:
                return null;
        }

    }
}
