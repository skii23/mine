package com.fit2cloud.devops.dao.ext;

import com.fit2cloud.devops.dto.ApplicationSettingDTO;

import java.util.List;
import java.util.Map;

public interface ExtApplicationSettingMapper {
    List<ApplicationSettingDTO> selectApplicationSettings(Map<String, Object> params);
}
