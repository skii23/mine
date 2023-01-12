package com.fit2cloud.devops.dao.ext;

import com.fit2cloud.devops.dto.DevopsJenkinsJobDto;

import java.util.List;
import java.util.Map;

public interface ExtDevopsJenkinsJobMapper {
    List<DevopsJenkinsJobDto> listDevopsJenkinsJob(Map params);
}
