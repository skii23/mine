package com.fit2cloud.devops.service;

import com.fit2cloud.commons.server.base.domain.Workspace;
import com.fit2cloud.commons.server.base.mapper.ext.ExtWorkspaceCommonMapper;
import com.fit2cloud.commons.server.constants.RoleConstants;
import com.fit2cloud.commons.server.model.ChartData;
import com.fit2cloud.commons.server.model.DashBoardTextDTO;
import com.fit2cloud.commons.server.model.WorkspaceOrganization;
import com.fit2cloud.commons.server.utils.SessionUtils;
import com.fit2cloud.commons.utils.Pager;
import com.fit2cloud.devops.common.util.CommonUtils;
import com.fit2cloud.devops.dao.ext.ExtDashboardMapper;
import com.fit2cloud.devops.dao.ext.ExtWorkspaceMapper;
import com.fit2cloud.devops.dto.CountMapDTO;
import com.fit2cloud.devops.vo.GroupAnalysisVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Resource
    private ExtDashboardMapper extDashboardMapper;

    @Resource
    private ExtWorkspaceMapper extWorkspaceMapper;

    public List<DashBoardTextDTO> selectDashboardDatas() {
        Map<String, Object> params = new HashMap<>();
        CommonUtils.filterPermission(params);
        return extDashboardMapper.selectDashboard(params);
    }

    public List<ChartData> getBuildTimeDistribution(Map<String,Object> params) {
        CommonUtils.filterPermission(params);
        return extDashboardMapper.getBuildTimeDistribution(params);
    }

    public List<ChartData> getBuildSuccessTrend(Map<String, Object> params) {
        CommonUtils.filterPermission(params);
        return extDashboardMapper.getBuildSuccessTrend(params);
    }

    public List<ChartData> getBuildWeekdayDistribution(Map<String, Object> params) {
        CommonUtils.filterPermission(params);
        return extDashboardMapper.getBuildWeekdayDistribution(params);
    }

    public List<ChartData> getBuildJobCountTrend(Map<String, Object> params) {
        CommonUtils.filterPermission(params);
        return extDashboardMapper.getBuildJobCountTrend(params);
    }

    public List<ChartData> getBuildJobBuildCountTrend(Map<String, Object> params) {
        CommonUtils.filterPermission(params);
        return extDashboardMapper.getBuildJobBuildCountTrend(params);
    }

    public List<ChartData> getDeployTimeDistribution(Map<String, Object> params) {
        CommonUtils.filterPermission(params);
        return extDashboardMapper.getDeployTimeDistribution(params);
    }

    public List<ChartData> getDeploySuccessTrend(Map<String, Object> params) {
        CommonUtils.filterPermission(params);
        return extDashboardMapper.getDeploySuccessTrend(params);
    }

    public List<ChartData> getDeployWeekdayDistribution(Map<String, Object> params) {
        CommonUtils.filterPermission(params);
        return extDashboardMapper.getDeployWeekdayDistribution(params);
    }

    public List<ChartData> getDeployAppCountTrend(Map<String, Object> params) {
        CommonUtils.filterPermission(params);
        return extDashboardMapper.getDeployAppCountTrend(params);
    }

    public List<ChartData> getDeployAppDeployCountTrend(Map<String, Object> params) {
        CommonUtils.filterPermission(params);
        return extDashboardMapper.getDeployAppDeployCountTrend(params);
    }

    public Pager<List<GroupAnalysisVO>> getGroupAnalysis(int goPage, int pageSize,Map<String,Object> params) {
        List<GroupAnalysisVO> result = new ArrayList<>();
        if (StringUtils.equals(SessionUtils.getUser().getParentRoleId(), RoleConstants.Id.ORGADMIN.name())) {
            params.put("organizationId", SessionUtils.getOrganizationId());
        }
        List<WorkspaceOrganization> workspaceList = extWorkspaceMapper.getWorkspaceList(params);

        Map<String, String> jobCountMap = listToMap(extDashboardMapper.getJobCountMap(params));
        Map<String, String> jobBuildCountMap = listToMap(extDashboardMapper.getJobBuildCountMap(params));
        Map<String, String> appCountMap = listToMap(extDashboardMapper.getAppCountMap(params));
        Map<String, String> appDeployCountMap = listToMap(extDashboardMapper.getAppDeployCountMap(params));
        params.put("success", true);
        Map<String, String> jobSuccessBuildCountMap = listToMap(extDashboardMapper.getJobBuildCountMap(params));
        Map<String, String> appSuccessDeployCountMap = listToMap(extDashboardMapper.getAppDeployCountMap(params));

        workspaceList.forEach(workspace -> {
            GroupAnalysisVO groupAnalysisVO = new GroupAnalysisVO();
            groupAnalysisVO.setWorkspaceName(workspace.getWorkspaceName());
            groupAnalysisVO.setOrganizationName(workspace.getOrganizationName());
            groupAnalysisVO.setJobCount(Integer.valueOf(jobCountMap.getOrDefault(workspace.getWorkspaceId(),"0")));
            groupAnalysisVO.setBuildCount(Integer.valueOf(jobBuildCountMap.getOrDefault(workspace.getWorkspaceId(),"0")));
            groupAnalysisVO.setSuccessBuildCount(Integer.valueOf(jobSuccessBuildCountMap.getOrDefault(workspace.getWorkspaceId(),"0")));
            groupAnalysisVO.setAppCount(Integer.valueOf(appCountMap.getOrDefault(workspace.getWorkspaceId(),"0")));
            groupAnalysisVO.setDeployCount(Integer.valueOf(appDeployCountMap.getOrDefault(workspace.getWorkspaceId(),"0")));
            groupAnalysisVO.setSuccessDeployCount(Integer.valueOf(appSuccessDeployCountMap.getOrDefault(workspace.getWorkspaceId(),"0")));
            result.add(groupAnalysisVO);
        });

        return CommonUtils.setPage(result,goPage,pageSize);
    }

    private Map<String, String> listToMap(List<CountMapDTO> list) {
        Map<String, String> result = new HashMap<>();
        list.forEach(countMapDTO -> {
            if (countMapDTO != null) {
                result.put(countMapDTO.getItemKey(), countMapDTO.getItemValue());
            }
        });
        return result;
    }

}
