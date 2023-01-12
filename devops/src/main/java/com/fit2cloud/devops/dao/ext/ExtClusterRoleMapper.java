package com.fit2cloud.devops.dao.ext;

import com.fit2cloud.devops.dto.ClusterRoleDTO;

import java.util.List;
import java.util.Map;

public interface ExtClusterRoleMapper {

    List<ClusterRoleDTO> selectClusterRole(Map params);
}
