package com.fit2cloud.devops.controller;

import com.fit2cloud.commons.server.base.domain.TagValue;
import com.fit2cloud.commons.server.constants.RoleConstants;
import com.fit2cloud.commons.server.service.TagService;
import com.fit2cloud.commons.server.utils.SessionUtils;
import com.fit2cloud.devops.common.util.CommonUtils;
import com.fit2cloud.devops.dao.ext.ExtClusterMapper;
import com.fit2cloud.devops.dao.ext.ExtClusterRoleMapper;
import com.fit2cloud.devops.service.ApplicationService;
import com.fit2cloud.devops.service.DevopsScriptService;
import com.fit2cloud.devops.service.ServerService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("condition")
public class DevopsConditionController {

    @Resource
    private ExtClusterMapper extClusterMapper;
    @Resource
    private ExtClusterRoleMapper extClusterRoleMapper;
    @Resource
    private ApplicationService applicationService;
    @Resource
    private DevopsScriptService devopsScriptService;
    @Resource
    private ServerService serverService;
    @Resource
    private TagService tagService;


    @RequestMapping("cluster")
    public List getClusters() {
        HashMap<String, Object> params = new HashMap<>();
        CommonUtils.filterPermission(params);
        return extClusterMapper.selectCluster(params);
    }

    @RequestMapping("clusterRole")
    public List getClusterRoles() {
        Map<String,Object> params = new HashMap<>();
        CommonUtils.filterPermission(params);
        return extClusterRoleMapper.selectClusterRole(params);
    }

    @RequestMapping("application")
    public List getApplications() {
        return applicationService.selectApplications(new HashMap());
    }

    @RequestMapping("script")
    public List getScripts() {
        return devopsScriptService.selectScripts(new HashMap<>());
    }

    @RequestMapping("cloudServer")
    public List getCloudServers() {
        return serverService.selectServerDto(new HashMap<>());
    }

    @RequestMapping("envTag")
    public List<TagValue> getEnvTags() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("tagKey", "environment");
        return tagService.selectTagValues(params);
    }

    @RequestMapping("bizTag")
    public List<TagValue> getBizTags() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("tagKey", "business");
        return tagService.selectTagValues(params);
    }
}
