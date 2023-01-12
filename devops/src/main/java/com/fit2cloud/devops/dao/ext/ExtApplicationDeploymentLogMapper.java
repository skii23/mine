package com.fit2cloud.devops.dao.ext;

import com.fit2cloud.devops.dto.ApplicationDeploymentLogDTO;

import java.util.List;
import java.util.Map;

public interface ExtApplicationDeploymentLogMapper {
    List<ApplicationDeploymentLogDTO> selectApplicationDeploymentLog(Map<String,Object> params);
}
