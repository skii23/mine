package com.fit2cloud.devops.service;

import com.fit2cloud.commons.server.base.domain.User;
import com.fit2cloud.commons.server.base.domain.UserExample;
import com.fit2cloud.commons.server.base.mapper.UserMapper;
import com.fit2cloud.commons.server.constants.RoleConstants;
import com.fit2cloud.commons.server.exception.F2CException;
import com.fit2cloud.commons.server.utils.SessionUtils;
import com.fit2cloud.commons.utils.UUIDUtil;
import com.fit2cloud.devops.base.domain.DevopsScriptFilter;
import com.fit2cloud.devops.base.domain.DevopsScriptFilterExample;
import com.fit2cloud.devops.base.domain.Script;
import com.fit2cloud.devops.base.domain.ScriptExample;
import com.fit2cloud.devops.base.mapper.DevopsScriptFilterMapper;
import com.fit2cloud.devops.base.mapper.ScriptMapper;
import com.fit2cloud.devops.common.consts.ScopeConstants;
import com.fit2cloud.devops.common.consts.ScriptConstants;
import com.fit2cloud.devops.common.model.ScriptJob;
import com.fit2cloud.devops.common.util.CommonUtils;
import com.fit2cloud.devops.dao.ext.ExtScriptMapper;
import com.fit2cloud.devops.dto.ScriptDTO;
import com.fit2cloud.devops.dto.ServerDTO;
import com.fit2cloud.devops.service.script.AnsibleAdhocService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@Transactional(rollbackFor = Exception.class)
public class DevopsScriptService {

    @Resource
    private ExtScriptMapper extScriptMapper;
    @Resource
    private ScriptMapper scriptMapper;
    @Resource
    private AnsibleAdhocService ansibleAdhocService;
    @Resource
    private ServerService serverService;
    @Resource
    private DictionaryService dictionaryService;
    @Resource
    private UserMapper userMapper;
    @Resource
    private DevopsScriptFilterMapper devopsScriptFilterMapper;

    public List<ScriptDTO> selectScripts(Map<String, Object> params) {
        CommonUtils.filterPermission(params);
        return extScriptMapper.selectScripts(params);
    }

    public List<Map> getOsList() {
        return dictionaryService.getOsList();
    }

    public List<String> getOsVersions(String os) {
        return dictionaryService.getOsVersions(os);
    }

    public Script saveScript(Script script) {
        saveCheck(script);
        if (script.getId() != null) {
            scriptMapper.updateByPrimaryKeySelective(script);
        } else {
            script.setId(UUIDUtil.newUUID());
            script.setCreatedTime(System.currentTimeMillis());

            if (StringUtils.equalsIgnoreCase(SessionUtils.getUser().getParentRoleId(), RoleConstants.Id.ORGADMIN.name())) {
                script.setScope(ScopeConstants.ORG);
                script.setOrganizationId(SessionUtils.getOrganizationId());
            } else if (StringUtils.equalsIgnoreCase(SessionUtils.getUser().getParentRoleId(), RoleConstants.Id.ADMIN.name())) {
                script.setScope(ScopeConstants.GLOBAL);
            } else {
                script.setScope(ScopeConstants.WORKSPACE);
                script.setWorkspaceId(SessionUtils.getWorkspaceId());
                script.setOrganizationId(SessionUtils.getOrganizationId());
            }
            scriptMapper.insert(script);
        }
        return script;
    }


    private void saveCheck(Script script) {
        ScriptExample scriptExample = new ScriptExample();
        if (script.getId() != null) {
            scriptExample.createCriteria().andNameEqualTo(script.getName()).andIdNotEqualTo(script.getId());
        } else {
            scriptExample.createCriteria().andNameEqualTo(script.getName());
        }
        List<Script> scripts = scriptMapper.selectByExample(scriptExample);
        if (scripts.size() != 0) {
            F2CException.throwException("名称：" + script.getName() + "已存在！");
        }
    }


    public void deleteScripts(String id) {
        scriptMapper.deleteByPrimaryKey(id);
    }


    public void implementScript(ScriptJob scriptJob) {
        Map<String, Object> params = new HashMap();
        List<ServerDTO> serverDTOS = new ArrayList<>();
        if (!scriptJob.getServerId().equalsIgnoreCase("ALL")) {
            params.put("id", scriptJob.getServerId());
            serverDTOS.addAll(serverService.selectServerDto(params));
        } else if (!scriptJob.getClusterRoleId().equalsIgnoreCase("ALL")) {
            params.put("clusterRoleId", scriptJob.getClusterRoleId());
            params.put("clusterId", scriptJob.getClusterId());
            serverDTOS.addAll(serverService.selectServerDto(params));
        } else if (scriptJob.getClusterRoleId().equalsIgnoreCase("ALL")) {
            params.put("clusterId", scriptJob.getClusterId());
            serverDTOS.addAll(serverService.selectServerDto(params));
        }

        if (CollectionUtils.isNotEmpty(serverDTOS)) {
            if (serverDTOS.size() >= 2) {
                if (StringUtils.isBlank(scriptJob.getPassword())) {
                    F2CException.throwException("请输入密码！");
                    return;
                }
                UserExample userExample = new UserExample();
                userExample.createCriteria()
                        .andIdEqualTo(SessionUtils.getUser().getId())
                        .andPasswordEqualTo(DigestUtils.md5Hex(scriptJob.getPassword()));
                List<User> users = userMapper.selectByExample(userExample);
                if (CollectionUtils.isEmpty(users)) {
                    F2CException.throwException("密码错误！请检查密码是否正确！");
                    return;
                }
            }
            DevopsScriptFilterExample devopsScriptFilterExample = new DevopsScriptFilterExample();
            devopsScriptFilterExample.createCriteria().andActiveEqualTo(true);
            List<DevopsScriptFilter> filters = devopsScriptFilterMapper.selectByExampleWithBLOBs(devopsScriptFilterExample);
            if (CollectionUtils.isNotEmpty(filters)) {
                for (DevopsScriptFilter filter : filters) {
                    switch (filter.getType()) {
                        case ScriptConstants.SCRIPT_FILTER_TYPE_KEYWORD: {
                            String[] keywords = filter.getValue().split(",");
                            for (String keyword : keywords) {
                                if (scriptJob.getContent().contains(keyword)) {
                                    F2CException.throwException("脚本不符合过滤规则" + filter.getName());
                                    return;
                                }
                            }
                            break;
                        }
                        case ScriptConstants.SCRIPT_FILTER_TYPE_REGEX: {
                            if (Pattern.matches(filter.getValue(), scriptJob.getContent())) {
                                F2CException.throwException("脚本不符合过滤规则" + filter.getName());
                                return;
                            }
                            break;
                        }
                        default: {
                        }
                    }
                }
            }
            ansibleAdhocService.createRunAdhoc(scriptJob, scriptJob.getExecutePath(), serverDTOS);
        } else {
            F2CException.throwException("主机数量为0");
        }
    }

}
