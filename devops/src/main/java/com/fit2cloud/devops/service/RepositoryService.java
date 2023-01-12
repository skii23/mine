package com.fit2cloud.devops.service;


import com.fit2cloud.commons.server.constants.RoleConstants;
import com.fit2cloud.commons.server.exception.F2CException;
import com.fit2cloud.commons.server.model.SessionUser;
import com.fit2cloud.commons.server.utils.SessionUtils;
import com.fit2cloud.commons.utils.UUIDUtil;
import com.fit2cloud.devops.base.domain.ApplicationRepository;
import com.fit2cloud.devops.base.domain.ApplicationRepositoryExample;
import com.fit2cloud.devops.base.domain.ApplicationRepositorySetting;
import com.fit2cloud.devops.base.domain.ApplicationRepositorySettingExample;
import com.fit2cloud.devops.base.mapper.ApplicationRepositoryMapper;
import com.fit2cloud.devops.base.mapper.ApplicationRepositorySettingMapper;
import com.fit2cloud.devops.common.consts.ERepositoryType;
import com.fit2cloud.devops.common.consts.ScopeConstants;
import com.fit2cloud.devops.common.consts.StatusConstants;
import com.fit2cloud.devops.dao.ext.ExtApplicationRepositoryMapper;
import com.fit2cloud.devops.dto.ApplicationRepositoryDTO;
import com.fit2cloud.devops.service.oss.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class RepositoryService {
    @Resource
    private ExtApplicationRepositoryMapper extApplicationRepositoryMapper;
    @Resource
    private ApplicationRepositoryMapper applicationRepositoryMapper;
    @Resource
    private AliyunOSSService aliyunOSSService;
    @Resource
    private AWSS3Service awss3Service;
    @Resource
    private NexusService nexusService;
    @Resource
    private ArtifactoryService artifactoryService;
    @Resource
    private ApplicationRepositorySettingMapper applicationRepositorySettingMapper;
    @Resource
    private Nexus3Service nexus3Service;
    @Resource
    private HarborService harborService;

    public static Map<String, ApplicationRepository> repositoryMap = new HashMap<>();

    public List<ApplicationRepositoryDTO> selectApplicationRepositorys(Map<String, Object> params) {
        SessionUser user = SessionUtils.getUser();
        if (user.getParentRoleId().equals(RoleConstants.Id.USER.name()) || user.getParentRoleId().equals(RoleConstants.Id.ORGADMIN.name())) {
            params.put("organizationId", user.getOrganizationId());
        }
        return extApplicationRepositoryMapper.selectApplicationRepository(params);
    }

    public List<String> getRepositoryTypes() {
        return ERepositoryType.listAllValue();
    }


    public void saveRepository(ApplicationRepository applicationRepository) {
        saveCheck(applicationRepository);
        if (applicationRepository.getId() != null) {
            applicationRepositoryMapper.updateByPrimaryKeySelective(applicationRepository);
        } else {
            applicationRepository.setId(UUIDUtil.newUUID());
            applicationRepository.setCreatedTime(System.currentTimeMillis());
            if (StringUtils.equalsIgnoreCase(SessionUtils.getUser().getParentRoleId(), RoleConstants.Id.ADMIN.name())) {
                applicationRepository.setOrganizationId("ROOT");
                applicationRepository.setScope(ScopeConstants.GLOBAL);
            } else {
                applicationRepository.setOrganizationId(SessionUtils.getOrganizationId());
                applicationRepository.setScope(ScopeConstants.ORG);
            }
            applicationRepository.setStatus(StatusConstants.VALID);
            applicationRepositoryMapper.insert(applicationRepository);
        }
    }

    private void saveCheck(ApplicationRepository applicationRepository) {
        ApplicationRepositoryExample applicationRepositoryExample = new ApplicationRepositoryExample();
        if (applicationRepository.getId() != null) {
            applicationRepositoryExample.createCriteria().andNameEqualTo(applicationRepository.getName()).andIdNotEqualTo(applicationRepository.getId());
        } else {
            applicationRepositoryExample.createCriteria().andNameEqualTo(applicationRepository.getName());
        }
        List<ApplicationRepository> scripts = applicationRepositoryMapper.selectByExample(applicationRepositoryExample);
        if (scripts.size() != 0) {
            F2CException.throwException("名称：" + applicationRepository.getName() + "已存在！");
        }
    }


    public void checkRepository(String repositoryId) {
        ApplicationRepository applicationRepository = getById(repositoryId);
        ERepositoryType type = ERepositoryType.fromValue(applicationRepository.getType());
        boolean status;
        switch (type) {
            case OSS:
                status = aliyunOSSService.check(applicationRepository);
                break;
            case S3:
                status = awss3Service.check(applicationRepository);
                break;
            case NEXUS:
                status = nexusService.check(applicationRepository);
                break;
            case NEXUS3:
                status = nexus3Service.check(applicationRepository);
                break;
            case ARTIFACTORY:
                status = artifactoryService.check(applicationRepository);
                break;
            case HARBOR:
                status = harborService.check(applicationRepository);
                break;
            default:
                status = false;
        }
        applicationRepository.setStatus(status ? StatusConstants.VALID : StatusConstants.INVALID);
        applicationRepositoryMapper.updateByPrimaryKeySelective(applicationRepository);
    }


    public void deleteRepository(String id) {
        applicationRepositoryMapper.deleteByPrimaryKey(id);
    }

    public ApplicationRepository getById(String repositoryId) {
        return applicationRepositoryMapper.selectByPrimaryKey(repositoryId);
    }

    /**
     * 根据环境id和应用id查询应用仓库
     * @param envId 环境ID
     * @param applicationId 应用ID
     * @return 仓库
     */
    public ApplicationRepository queryRepo(String envId, String applicationId) {
        ApplicationRepositorySettingExample applicationRepositorySettingExample = new ApplicationRepositorySettingExample();
        applicationRepositorySettingExample.createCriteria().andApplicationIdEqualTo(applicationId).andEnvIdEqualTo(envId);
        ApplicationRepositorySetting applicationRepositorySetting = applicationRepositorySettingMapper.selectByExample(applicationRepositorySettingExample).get(0);
        return getById(applicationRepositorySetting.getRepositoryId());
    }
}
