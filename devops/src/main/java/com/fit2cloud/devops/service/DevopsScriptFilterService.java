package com.fit2cloud.devops.service;

import com.fit2cloud.commons.utils.UUIDUtil;
import com.fit2cloud.devops.base.domain.DevopsScriptFilter;
import com.fit2cloud.devops.base.mapper.DevopsScriptFilterMapper;
import com.fit2cloud.devops.dao.ext.ExtDevopsScriptFilterMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class DevopsScriptFilterService {

    @Resource
    private DevopsScriptFilterMapper devopsScriptFilterMapper;

    @Resource
    private ExtDevopsScriptFilterMapper extDevopsScriptFilterMapper;

    public List<DevopsScriptFilter> getScriptFilters(Map<String, Object> params) {
        return extDevopsScriptFilterMapper.getScriptFilters(params);
    }

    public void save(DevopsScriptFilter devopsScriptFilter) {
        if (devopsScriptFilter.getId() != null) {
            updateScriptFilter(devopsScriptFilter);
        }else {
            createScriptFilter(devopsScriptFilter);
        }
    }

    public void createScriptFilter(DevopsScriptFilter devopsScriptFilter) {
        devopsScriptFilter.setId(UUIDUtil.newUUID());
        devopsScriptFilter.setCreateTime(System.currentTimeMillis());
        devopsScriptFilterMapper.insertSelective(devopsScriptFilter);
    }

    public void updateScriptFilter(DevopsScriptFilter devopsScriptFilter) {
        devopsScriptFilterMapper.updateByPrimaryKeyWithBLOBs(devopsScriptFilter);
    }

    public void deleteScriptFilter(List<DevopsScriptFilter> devopsScriptFilters) {
        if (!CollectionUtils.isEmpty(devopsScriptFilters)) {
            devopsScriptFilters.forEach(filter -> devopsScriptFilterMapper.deleteByPrimaryKey(filter.getId()));
        }
    }
}
