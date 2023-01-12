package com.fit2cloud.devops.dao.ext;

import com.fit2cloud.commons.server.model.ChartData;
import com.fit2cloud.commons.server.model.DashBoardTextDTO;
import com.fit2cloud.devops.dto.CountMapDTO;

import java.util.List;
import java.util.Map;

public interface ExtDashboardMapper {

    List<DashBoardTextDTO> selectDashboard(Map<String, Object> params);

    List<ChartData> getBuildTimeDistribution(Map<String, Object> params);

    List<ChartData> getBuildSuccessTrend(Map<String, Object> params);

    List<ChartData> getBuildWeekdayDistribution(Map<String, Object> params);

    List<ChartData> getBuildJobCountTrend(Map<String, Object> params);

    List<ChartData> getBuildJobBuildCountTrend(Map<String, Object> params);

    List<ChartData> getDeployTimeDistribution(Map<String, Object> params);

    List<ChartData> getDeploySuccessTrend(Map<String, Object> params);

    List<ChartData> getDeployWeekdayDistribution(Map<String, Object> params);

    List<ChartData> getDeployAppCountTrend(Map<String, Object> params);

    List<ChartData> getDeployAppDeployCountTrend(Map<String, Object> params);

    List<CountMapDTO> getJobCountMap(Map<String,Object> params);

    List<CountMapDTO> getJobBuildCountMap(Map<String, Object> params);

    List<CountMapDTO> getAppCountMap(Map<String, Object> params);

    List<CountMapDTO> getAppDeployCountMap(Map<String, Object> params);

}
