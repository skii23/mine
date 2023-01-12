package com.fit2cloud.devops.dao.ext;

import com.fit2cloud.devops.base.domain.ApplicationRepository;
import com.fit2cloud.devops.base.domain.ApplicationRepositorySetting;
import com.fit2cloud.devops.dto.ApplicationDTO;
import com.fit2cloud.devops.dto.ApplicationRepositorySettingDTO;

import java.util.List;
import java.util.Map;

public interface ExtApplicationMapper {

    List<ApplicationDTO> selectApplications(Map params);

    List<ApplicationRepositorySettingDTO> selectRepositorySettings(String applicationId);
}
