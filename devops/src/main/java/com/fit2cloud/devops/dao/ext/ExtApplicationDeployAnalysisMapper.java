package com.fit2cloud.devops.dao.ext;

import com.fit2cloud.commons.server.model.ChartData;
import com.fit2cloud.devops.common.model.DeployTopData;

import java.util.List;
import java.util.Map;

public interface ExtApplicationDeployAnalysisMapper {


    List<ChartData> selectDeployTrend(Map<String,Object> params);

    List<DeployTopData> selectDeployTop(Map<String,Object>params);
}
