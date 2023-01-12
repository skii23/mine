package com.fit2cloud.devops.service;


import com.fit2cloud.commons.server.constants.RoleConstants;
import com.fit2cloud.commons.server.exception.F2CException;
import com.fit2cloud.commons.server.model.SessionUser;
import com.fit2cloud.commons.server.utils.SessionUtils;
import com.fit2cloud.commons.utils.UUIDUtil;
import com.fit2cloud.devops.base.domain.ClusterRole;
import com.fit2cloud.devops.base.domain.ClusterRoleExample;
import com.fit2cloud.devops.base.mapper.ClusterRoleMapper;
import com.fit2cloud.devops.common.util.CommonUtils;
import com.fit2cloud.devops.dao.ext.ExtClusterRoleMapper;
import com.fit2cloud.devops.dto.ClusterRoleDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class ClusterRoleService {

    @Resource
    private ClusterRoleMapper clusterRoleMapper;
    @Resource
    private ExtClusterRoleMapper extClusterRoleMapper;


    public ClusterRole saveClusterRole(ClusterRole clusterRole) {
        saveCheck(clusterRole);
        if (clusterRole.getId() != null) {
            clusterRoleMapper.updateByPrimaryKeySelective(clusterRole);
        } else {
            clusterRole.setId(UUIDUtil.newUUID());
            clusterRole.setCreatedTime(System.currentTimeMillis());
            clusterRoleMapper.insert(clusterRole);
        }
        return clusterRole;
    }

    public ClusterRole getClusterRole(String clusterRoleId) {
        return clusterRoleMapper.selectByPrimaryKey(clusterRoleId);
    }


    private void saveCheck(ClusterRole clusterRole) {
        ClusterRoleExample clusterRoleExample = new ClusterRoleExample();
        if (clusterRole.getId() != null) {
            clusterRoleExample.createCriteria()
                    .andClusterIdEqualTo(clusterRole.getClusterId())
                    .andNameEqualTo(clusterRole.getName())
                    .andIdNotEqualTo(clusterRole.getId());
        } else {
            clusterRoleExample.createCriteria()
                    .andClusterIdEqualTo(clusterRole.getClusterId())
                    .andNameEqualTo(clusterRole.getName());
        }
        List<ClusterRole> clusterRoles = clusterRoleMapper.selectByExample(clusterRoleExample);
        if (clusterRoles.size() != 0) {
            F2CException.throwException("名称：" + clusterRole.getName() + "已存在！");
        }
    }


    public List<ClusterRoleDTO> selectClusterRole(Map<String, Object> map) {
        CommonUtils.filterPermission(map);
        return extClusterRoleMapper.selectClusterRole(map);
    }

    public void deleteClusterRole(String clusterRoleId) {
        deleteCheck(clusterRoleId);
        clusterRoleMapper.deleteByPrimaryKey(clusterRoleId);
    }

    private void deleteCheck(String clusterRoleId) {
        Map params = new HashMap<>();
        params.put("id", clusterRoleId);
        List<ClusterRoleDTO> clusterRoleDTOS = extClusterRoleMapper.selectClusterRole(params);
        if (CollectionUtils.isNotEmpty(clusterRoleDTOS)) {
            ClusterRoleDTO clusterRoleDTO = clusterRoleDTOS.get(0);
            if (clusterRoleDTO.getCountServer() != 0) {
                F2CException.throwException("删除失败，" + clusterRoleDTO.getName() + "主机组下资源不为空！");
            }
        } else {
            F2CException.throwException("主机组不存在！");
        }
    }

}
