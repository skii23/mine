package com.fit2cloud.devops.service;

import com.fit2cloud.commons.utils.UUIDUtil;
import com.fit2cloud.devops.base.domain.ApplicationRepositorySetting;
import com.fit2cloud.devops.base.domain.ApplicationRepositorySettingExample;
import com.fit2cloud.devops.base.mapper.ApplicationRepositorySettingMapper;
 import com.fit2cloud.devops.dto.ApplicationRepositorySettingDTO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ApplicationRepositorySettingService {

    @Resource
    private ApplicationRepositorySettingMapper applicationRepositorySettingMapper;


    public void save(List<ApplicationRepositorySettingDTO> repositorySettings, String applicationId) {

        repositorySettings.forEach(applicationRepositorySetting -> {
            applicationRepositorySetting.setApplicationId(applicationId);
            if (applicationRepositorySetting.getId() == null) {
                applicationRepositorySetting.setId(UUIDUtil.newUUID());
                applicationRepositorySettingMapper.insert(applicationRepositorySetting);
            } else {
                applicationRepositorySettingMapper.updateByPrimaryKey(applicationRepositorySetting);
            }
        });

        ApplicationRepositorySettingExample applicationRepositorySettingExample = new ApplicationRepositorySettingExample();
        applicationRepositorySettingExample.createCriteria().andApplicationIdEqualTo(applicationId);

        applicationRepositorySettingMapper.selectByExample(applicationRepositorySettingExample)
                .stream().
                filter(applicationRepositorySetting -> {
                    boolean flag = false;
                    for (ApplicationRepositorySetting repositorySetting : repositorySettings) {
                        if (repositorySetting.getId().equals(applicationRepositorySetting.getId())) {
                            flag = true;
                        }
                    }
                    return !flag;
                }).collect(Collectors.toList()).forEach(applicationRepositorySetting -> {
            applicationRepositorySettingMapper.deleteByPrimaryKey(applicationRepositorySetting.getId());
        });
    }



}
