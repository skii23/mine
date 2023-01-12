package com.fit2cloud.devops.service;

import com.fit2cloud.devops.base.mapper.DevopsJenkinsJobMapper;
import com.fit2cloud.commons.server.base.domain.SystemParameter;
import com.fit2cloud.commons.server.base.mapper.SystemParameterMapper;
import com.fit2cloud.commons.utils.HttpClientConfig;
import com.fit2cloud.commons.utils.HttpClientUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import tk.mybatis.mapper.entity.Example;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fit2cloud.devops.service.jenkins.DevopsJenkinsSystemConfigService;
import com.fit2cloud.devops.service.model.JenkinsJobSonarqubeParams;
import com.fit2cloud.devops.service.model.SonarqubeMetrics;
import com.fit2cloud.devops.service.jenkins.DevopsJenkinsJobService;
import com.fit2cloud.commons.utils.LogUtil;
import com.fit2cloud.devops.base.domain.DevopsJenkinsJob;
import javax.annotation.Resource;
import java.util.*;
import java.io.IOException;
import java.lang.reflect.Field;

@Service
@Transactional(rollbackFor = Exception.class)
public class DevopsSonarqubeService {

    @Resource
    private DevopsJenkinsSystemConfigService jenkinsConfigService;

    @Resource
    private DevopsJenkinsJobService jenkinsJobService;

    @Resource
    private SystemParameterMapper systemParameterMapper;

    @Resource
    private DevopsJenkinsJobMapper devopsJenkinsJobMapper;

    private Map<String, String> sonarqubeServers = new HashMap<>();

    private final static int MAX_HTTP_REQ_TIMEOUT = 3000;

    private final static String SONARQUBE_MEASURES_API = "%s/api/measures/component?component=%s&metricKeys=%s";
    private final static String SONARQUBE_DASHBOARD_API = "%s/dashboard?id=%s";

    static class MetricKey {
        public static final String BUGS = "bugs";
        public static final String VULNERABILITIES = "vulnerabilities";
        public static final String COVERAGE = "coverage";
        public static final String DEBT = "sqale_index";
        public static final String DUPLICATED = "duplicated_lines_density";
        public static final String LINE_TO_COVER = "lines_to_cover";
        public static final String CODE_LINES = "ncloc";

        public static final String NEW_BUGS = "new_bugs";
        public static final String NEW_VULNERABILITIES = "new_vulnerabilities";
        public static final String NEW_COVERAGE = "new_coverage";
        public static final String NEW_DEBT = "new_technical_debt";
        public static final String NEW_DUPLICATED = "new_duplicated_lines_density";
        public static final String NEW_LINE_TO_COVER = "new_lines_to_cover";
        public static final String NEW_CODE_LINES = "new_lines";

        public static final String VIOLATIONS = "violations";
        public static final String OPEN_ISSUE = "open_issues";
        public static final String CONFIRED_ISSUE = "confirmed_issues";
        public static final String FALSE_POSITION_ISSUE = "false_positive_issues";
    }

    public String getSonarqubeDashboardUrl(DevopsJenkinsJob job) {
        JenkinsJobSonarqubeParams sonarParams = jenkinsJobService.getSonarParamFromJobXml(job);
        if (sonarParams == null) {
            return null;
        }

        String sonarBaseServer = getSonarServersUrl(sonarParams.getServerName());
        if (StringUtils.isBlank(sonarBaseServer)) {
            LogUtil.error(String.format("无法找到对应的sonarqube[%s]服务地址.", sonarParams.getServerName()));
            return null;
        }
       
        return String.format(SONARQUBE_DASHBOARD_API, httpUrlFormat(sonarBaseServer), job.getName());
    }

    public SonarqubeMetrics getSonarqubeMerticsByName(String jobName, Boolean isNew) {
        if (StringUtils.isBlank(jobName)) {
            return null;
        }
        Example example = new Example(DevopsJenkinsJob.class);
        example.createCriteria().andEqualTo("name", jobName).andIsNotNull("appId");
        List<DevopsJenkinsJob> jobs = devopsJenkinsJobMapper.selectByExample(example);
        if (jobs.isEmpty()) {
            LogUtil.error(String.format("jobName[%s] is not exsit.", jobName));
            return null;
        }
        return getSonarqubeMertics(jobs.get(0), isNew);
    }

    public SonarqubeMetrics getSonarqubeMerticsById(String jobId, Boolean isNew) {
        if (StringUtils.isBlank(jobId)) {
            return null;
        }
        DevopsJenkinsJob job = devopsJenkinsJobMapper.selectByPrimaryKey(jobId);
        if (job == null) {
            LogUtil.error(String.format("jobId[%s] is not exsit.", jobId));
            return null;
        }
        return getSonarqubeMertics(job, isNew);
    }

    public SonarqubeMetrics getSonarqubeMertics(DevopsJenkinsJob job, boolean isNew) {
        JenkinsJobSonarqubeParams sonarParams = jenkinsJobService.getSonarParamFromJobXml(job);
        if (sonarParams == null) {
            return null;
        }

        String sonarBaseServer = getSonarServersUrl(sonarParams.getServerName());
        if (StringUtils.isBlank(sonarBaseServer)) {
            LogUtil.error(String.format("无法找到对应的sonarqube[%s]服务地址.", sonarParams.getServerName()));
            return null;
        }
        List<String> keys = new ArrayList<>();
        for (Field field : MetricKey.class.getFields()) {
            try {
                keys.add(field.get(null).toString());
            } catch (IllegalAccessException e) {
                LogUtil.error(String.format("遍历常量[%s]异常.", field.getName()));
            }
        }
        Map<String, Long> maps = getMeasureMetrics(sonarBaseServer, job.getName(), keys, isNew);
        if (maps == null) {
            LogUtil.error(
                    String.format("请求sonar=[%s]指标=[%s]数据失败.", job.getName(), convertKeyListToString(keys, isNew)));
            return null;
        }

        SonarqubeMetrics metrics = getStructMetrics(maps);
        metrics.setNew(isNew);
        return metrics;
    }

    private SonarqubeMetrics getStructMetrics(Map<String, Long> maps) {
        SonarqubeMetrics out = new SonarqubeMetrics();
        maps.forEach((key, val) -> {
            if (val == null) {
                return;
            }
            switch (key) {
                case  MetricKey.NEW_BUGS:
                case MetricKey.BUGS:
                    out.setBugs(val);
                    break;
                case MetricKey.NEW_VULNERABILITIES:
                case MetricKey.VULNERABILITIES:
                    out.setVulnerabilities(val);
                    break;
                case MetricKey.NEW_COVERAGE:
                case MetricKey.COVERAGE:
                    out.setTestCoverage(val);
                    break;
                case MetricKey.NEW_DEBT:
                case MetricKey.DEBT:
                    out.setDebt(val);
                    break;
                case MetricKey.NEW_CODE_LINES:
                case MetricKey.CODE_LINES:
                        out.setCodeLines(val);
                        break;
                case MetricKey.NEW_DUPLICATED:
                case MetricKey.DUPLICATED:
                    out.setDuplicatedRate(val);
                    break;
                case MetricKey.NEW_LINE_TO_COVER:
                case MetricKey.LINE_TO_COVER:
                    out.setCoverageCodeLine(val);
                    break;
                case MetricKey.VIOLATIONS:
                    out.setIssue(val);
                    break;
                case MetricKey.OPEN_ISSUE:
                    out.setOpenIssue(val);
                    break;
                case MetricKey.CONFIRED_ISSUE:
                    out.setConfiredIssue(val);
                    break;
                case MetricKey.FALSE_POSITION_ISSUE:
                    out.setFalsePositionIssue(val);
                    break;
                default:
                    LogUtil.error(String.format("未知类型指标：%s=%l.", key, val));
                    break;
            }
        });
        return out;
    }

    private Map<String, Long> getMeasureMetrics(String baseUrl, String projectKey, List<String> keys, boolean isNew) {
        String requestUrl = String.format(SONARQUBE_MEASURES_API, httpUrlFormat(baseUrl), projectKey, convertKeyListToString(keys, isNew));
        HttpClientConfig reqConfig = getApiConfig(null);
        if (reqConfig == null) {
            LogUtil.error(String.format("请求参数获取失败,url=%s, 确认已配置token", requestUrl));
            return null;
        }
        String rsp = null;
        try {
            rsp = HttpClientUtil.get(requestUrl, reqConfig);
            if (rsp == null) {
                throw new IOException("respone empty.");
            }
        } catch (Exception e) {
            LogUtil.error(String.format("请求url=%s 数据失败:%s", requestUrl), e.getMessage());
            return null;
        }
        JSONObject rspJson = null;
        try {
            rspJson = JSON.parseObject(rsp);
            if (rspJson == null) {
                throw new JSONException("invalid json data");
            }
        } catch (JSONException e) {
            LogUtil.error(String.format("json解析返回值失败,data=%s, url=%s", rsp, requestUrl));
            return null;
        }
        return getMetricByJson(rspJson, projectKey, isNew);
    }

    private String convertKeyListToString(List<String> keys, boolean isNew) {
        String out = "";
        for (String key : keys) {
            String name = key;
            if (isNew) {
                if (!name.contains("new_")) {
                    continue;
                }
            } else {
                if (name.contains("new_")) {
                    continue;
                }
            }
            if (StringUtils.isNotBlank(out)) {
                out = String.format("%s,%s", out, name);
            } else {
                out = name;
            }
        }
        return out;
    }

    private Map<String, Long> getMetricByJson(@NonNull JSONObject data, String projectKey, boolean isNew) {
        Map<String, Long> out = new HashMap<String, Long>();
        try {
            JSONArray measures = data.getJSONObject("component").getJSONArray("measures");
            for (int i = 0; i < measures.size(); i++) {
                JSONObject metrics = measures.getJSONObject(i);
                String key = metrics.getString("metric");
                Double val = 0.0;
                if (isNew) {
                    val = metrics.getJSONObject("period").getDoubleValue("value");
                } else {
                    val = metrics.getDoubleValue("value");
                }
                if (val < 1 && val > 0) {
                    val = (val * 100);
                }
                out.put(key, val.longValue());
            }
        } catch (Exception e) {
            LogUtil.error(String.format("json解析失败,data=%s, error:%s", data.toJSONString(), e.getMessage()));
            return null;
        }
        LogUtil.warn(String.format("get sonarqube metrics:%s", out.toString()));
        return out;
    }

    private HttpClientConfig getApiConfig(Map<String, String> headers) {
        String token = getParamValue("sonarqube.token");
        if (StringUtils.isBlank(token)) {
            String User = getParamValue("sonarqube.user");
            String Password = getParamValue("sonarqube.password");
            token = String.format("%s:%s", User, Password);
        } else {
            token = token + ":";
        }
        if (StringUtils.isBlank(token)) {
            return null;
        }
        HttpClientConfig config = new HttpClientConfig();
        config.addHeader("Content-Type", "application/json");
        // config.addHeader("Authorization", "Basic " +
        // Base64.getEncoder().encodeToString(token.getBytes())); // 暂时匿名访问
        if (headers != null) {
            headers.forEach((key, value) -> {
                config.addHeader(key, value);
            });
        }
        config.setConnectTimeout(MAX_HTTP_REQ_TIMEOUT);
        config.setConnectionRequestTimeout(MAX_HTTP_REQ_TIMEOUT);
        config.setCocketTimeout(MAX_HTTP_REQ_TIMEOUT + 500);
        return config;
    }

    private String getParamValue(String key) {
        SystemParameter systemParameter = systemParameterMapper.selectByPrimaryKey(key);
        if (systemParameter == null) {
            return null;
        }
        String value = systemParameter.getParamValue();
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return value;
    }

    private void setParamValue(String key, String value) {
        SystemParameter systemParameter = new SystemParameter();
        systemParameter.setParamKey(key);
        systemParameter.setParamValue(value);
        systemParameter.setType("text");
        systemParameterMapper.insert(systemParameter);
        return;
    }

    private String getSonarServersUrl(String serverName) {
        if (sonarqubeServers.containsKey(serverName)) {
            return sonarqubeServers.get(serverName);
        }

        String url = getParamValue(serverName);
        if (StringUtils.isNotBlank(url)) {
            sonarqubeServers.put(serverName, url);
            return url;
        }
        synchronized (sonarqubeServers) {
            LogUtil.warn("++++++++++++++++++++++++++++++++++entry lock:" + serverName);
            if (sonarqubeServers.containsKey(serverName)) {
                return sonarqubeServers.get(serverName);
            }
            Map<String, String> servers = jenkinsConfigService.getSonarServers();
            for (String key : servers.keySet()) {
                setParamValue(key, servers.get(key));
                sonarqubeServers.put(key, servers.get(key));
            }
            LogUtil.warn("-----------------------------------lease lock:" + serverName);
        }
        if (sonarqubeServers.containsKey(serverName)) {
            return sonarqubeServers.get(serverName);
        }
        return null;
    }

    private String httpUrlFormat(String url) {
        if (url.endsWith("/")) {
            url = url.substring(0,url.length()-1);
        }
        return url;
    }

}
