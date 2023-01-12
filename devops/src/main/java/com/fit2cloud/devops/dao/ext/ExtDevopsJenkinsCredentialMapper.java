package com.fit2cloud.devops.dao.ext;

import com.fit2cloud.devops.dto.DevopsJenkinsCredentialDto;

import java.util.List;
import java.util.Map;

public interface ExtDevopsJenkinsCredentialMapper {
    List<DevopsJenkinsCredentialDto> getCredentials(Map<String, Object> params);
}
