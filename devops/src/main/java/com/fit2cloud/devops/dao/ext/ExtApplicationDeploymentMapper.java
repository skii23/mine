package com.fit2cloud.devops.dao.ext;

import com.fit2cloud.devops.dto.ApplicationDeploymentDTO;

import java.util.List;
import java.util.Map;

public interface ExtApplicationDeploymentMapper {

     List<ApplicationDeploymentDTO> selectApplicationDeployments(Map <String,Object> params);
}
