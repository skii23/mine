package com.fit2cloud.devops.dao.ext;

import com.fit2cloud.devops.base.domain.DevopsScriptFilter;

import java.util.List;
import java.util.Map;

public interface ExtDevopsScriptFilterMapper {
    List<DevopsScriptFilter> getScriptFilters(Map<String, Object> params);
}
