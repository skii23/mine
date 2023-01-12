package com.fit2cloud.devops.dao.ext;


import com.fit2cloud.devops.base.domain.ApplicationVersion;
import com.fit2cloud.devops.dto.ApplicationVersionDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ExtApplicationVersionMapper {

     List<ApplicationVersionDTO> selectApplicationVersions(Map<String,Object> params);

     ApplicationVersion getLatestVersion(@Param("applicationId") String applicationId);
}
