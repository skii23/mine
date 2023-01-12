package com.fit2cloud.devops.service;

import com.fit2cloud.commons.server.model.ChartData;
import com.fit2cloud.devops.common.model.DeployTopData;
import com.fit2cloud.devops.dao.ext.ExtApplicationDeployAnalysisMapper;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ApplicationDeployAnalysisService {

    @Resource
    private ExtApplicationDeployAnalysisMapper extApplicationDeployAnalysisMapper;

    public List<ChartData> selectDeployTrend(Map <String,Object>params) {
        return extApplicationDeployAnalysisMapper.selectDeployTrend(params);
    }

    public List<DeployTopData> selectDeployTop(Map <String,Object>params) {
        return extApplicationDeployAnalysisMapper.selectDeployTop(params);
    }
}

