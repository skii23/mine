package com.fit2cloud.devops.service;

import com.fit2cloud.commons.server.constants.RoleConstants;
import com.fit2cloud.commons.server.exception.F2CException;
import com.fit2cloud.commons.server.model.SessionUser;
import com.fit2cloud.commons.server.utils.SessionUtils;
import com.fit2cloud.commons.utils.UUIDUtil;
import com.fit2cloud.devops.base.domain.CloudServerDevops;
import com.fit2cloud.devops.base.domain.CloudServerDevopsExample;
import com.fit2cloud.devops.base.domain.Variable;
import com.fit2cloud.devops.base.mapper.CloudServerDevopsMapper;
import com.fit2cloud.devops.base.mapper.VariableMapper;
import com.fit2cloud.devops.dao.ext.ExtVariableMapper;
import com.fit2cloud.devops.dto.VariableDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VariableService {


    @Resource
    private VariableMapper variableMapper;
    @Resource
    private ExtVariableMapper extVariableMapper;
    @Resource
    private CloudServerDevopsMapper cloudServerDevopsMapper;

    public Variable saveVariable(Variable variable) {
        checkName(variable);
        if (variable.getId() == null) {
            variable.setId(UUIDUtil.newUUID());
            variable.setCreatedTime(System.currentTimeMillis());
            variableMapper.insert(variable);
        } else {
            variableMapper.updateByPrimaryKeySelective(variable);
        }
        return variable;
    }

    private void checkName(Variable variable) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", variable.getName());
        List<VariableDTO> variableDTOS = this.selectVariables(params);
        if (variable.getId() == null) {
            if (variableDTOS.size() > 0) {
                F2CException.throwException("变量:" + variable.getName() + "已存在！");
            }
        } else {
            variableDTOS.forEach(variableDTO -> {
                if (StringUtils.equalsIgnoreCase(variableDTO.getId(), variable.getId())) {
                    F2CException.throwException("变量:" + variable.getName() + "已存在！");
                }
            });

        }
    }


    public Map<String, String> getHostVariable(String cloudServerId) {
        Map<String, String> vars = new HashMap<>();
        CloudServerDevops cloudServerDevops = cloudServerDevopsMapper.selectByPrimaryKey(cloudServerId);
        if (cloudServerDevops != null) {
            Map<String, Object> params = new HashMap<>();
            params.put("resourceIds",
                    Arrays.asList(cloudServerId, cloudServerDevops.getClusterId(), cloudServerDevops.getClusterRoleId()));
            List<VariableDTO> variableDTOS = selectVariables(params);

            variableDTOS.forEach(variableDTO -> {
                vars.put(variableDTO.getName(), variableDTO.getValue());
            });
        }
        return vars;
    }

    public List<VariableDTO> selectVariables(Map<String, Object> params) {
        SessionUser user = SessionUtils.getUser();
        if (user != null) {
            if (StringUtils.equalsIgnoreCase(user.getParentRoleId(), RoleConstants.Id.ORGADMIN.name())) {
                params.put("organizationId", user.getOrganizationId());
            } else if (StringUtils.equalsIgnoreCase(user.getParentRoleId(), RoleConstants.Id.USER.name())) {
                params.put("workspaceId", user.getWorkspaceId());
            }
        }
        return extVariableMapper.selectVariables(params);

    }


    public void deleteVariable(String variableId) {
        variableMapper.deleteByPrimaryKey(variableId);
    }


}
