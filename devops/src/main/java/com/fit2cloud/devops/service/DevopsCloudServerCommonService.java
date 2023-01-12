package com.fit2cloud.devops.service;

import com.fit2cloud.devops.base.domain.DevopsCloudServer;
import com.fit2cloud.devops.base.mapper.DevopsCloudServerMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author yankaijun
 * @date 2019-08-02 16:28
 */
@Service
public class DevopsCloudServerCommonService {

    @Resource
    private DevopsCloudServerMapper devopsCloudServerMapper;

    public DevopsCloudServer get(String cloudServerId) {
        return devopsCloudServerMapper.selectByPrimaryKey(cloudServerId);
    }
}
