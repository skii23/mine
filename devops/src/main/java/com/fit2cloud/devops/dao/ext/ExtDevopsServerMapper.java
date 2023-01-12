package com.fit2cloud.devops.dao.ext;

import com.fit2cloud.devops.base.domain.DevopsCloudServer;
import com.fit2cloud.devops.dto.ServerDTO;

import java.util.List;
import java.util.Map;

public interface ExtDevopsServerMapper {

     List<ServerDTO> selectDevopsServer(Map map);

     List<ServerDTO> checkServerInCluster(Map map);

     List<DevopsCloudServer> getConnectCheckList();
}
