package com.fit2cloud.devops.dao.ext;

import com.fit2cloud.devops.dto.DevopsJenkinsViewDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ExtDevopsJenkinsViewMapper {
    List<DevopsJenkinsViewDTO> getViewList(Map<String, Object> params);

    DevopsJenkinsViewDTO getViewById(@Param("id") String id);
}
