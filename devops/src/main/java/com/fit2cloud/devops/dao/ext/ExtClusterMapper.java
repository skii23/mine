package com.fit2cloud.devops.dao.ext;

import com.fit2cloud.devops.dto.ClusterDTO;

import java.util.List;
import java.util.Map;

public interface ExtClusterMapper {

     List<ClusterDTO> selectCluster(Map params);
}
