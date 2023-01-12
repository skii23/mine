package com.fit2cloud.devops.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.commons.server.base.domain.SystemParameter;
import com.fit2cloud.commons.server.base.domain.WorkspaceJiraRel;
import com.fit2cloud.commons.server.base.mapper.SystemParameterMapper;
import com.fit2cloud.commons.server.base.mapper.WorkspaceJiraRelMapper;
import com.fit2cloud.commons.server.exception.F2CException;
import com.fit2cloud.commons.server.utils.SessionUtils;
import com.fit2cloud.commons.utils.HttpClientConfig;
import com.fit2cloud.commons.utils.HttpClientUtil;
import com.fit2cloud.commons.utils.LogUtil;
import com.fit2cloud.devops.common.util.CommonUtils;
import com.fit2cloud.devops.vo.JiraIssue;
import com.fit2cloud.devops.vo.SprintDetail;
import com.fit2cloud.devops.vo.SprintInfo;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.java.Log;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author caiwzh
 * @date 2022/11/1
 */
@Service
@Log
public class DevopsJiraService {
    // Get all boards
    private static final String JIRA_BOARD = "/jira/rest/agile/1.0/board?projectKeyOrId=%s&type=scrum";
    // Get all sprints
    private static final String JIRA_BOARD_SPRINT = "/jira/rest/agile/1.0/board/%s/sprint";
    // Get issues for sprint
    private static final String JIRA_BOARD_SPRINT_ISSUE = "/jira/rest/agile/1.0/board/%s/sprint/%s/issue";

    // 2022-11-01T21:09:24.041+08:00
    public static final String DATE_FORMATE_XXX = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    // 2022-11-01T21:09:24.091+0800
    public static final String DATE_FORMATE_Z = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    @Resource
    private WorkspaceJiraRelMapper workspaceJiraRelMapper;

    @Resource
    private SystemParameterMapper systemParameterMapper;

    private Cache<String, SprintDetail> SprintDetailCache = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(512)
            .build();

    private Cache<String, List<SprintInfo>> SprintListCache = CacheBuilder.newBuilder()
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .maximumSize(512)
            .build();

    private Cache<String, Map<Long, AtomicInteger>> SprintTimeCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS)
            .maximumSize(512)
            .build();

    public List<SprintInfo> getAllSprite(Integer goPages, Integer pageSize) {
        List<SprintInfo> allSprite = this.getAllSprite();
        return CommonUtils.page(allSprite, goPages, pageSize);
    }

    public List<SprintInfo> getAllSprite() {
        String workspaceId = SessionUtils.getUser().getWorkspaceId();
        Example example = new Example(WorkspaceJiraRel.class);
        example.createCriteria().andEqualTo("workspaceId", workspaceId);
        List<WorkspaceJiraRel> workspaceJiraRels = workspaceJiraRelMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(workspaceJiraRels)) {
            // F2CException.throwException("该工作空间未绑定jira项目");
            return Collections.EMPTY_LIST;
        }
        WorkspaceJiraRel workspaceJiraRel = workspaceJiraRels.get(0);
        List<SprintInfo> allSprite = SprintListCache.getIfPresent(workspaceJiraRel.getJiraProjectId());
        if (allSprite != null) {
            return allSprite;
        }
        String jiraHost = getNotNullValue("jira.host");
        String boradResponse = HttpClientUtil
                .get(jiraHost + String.format(JIRA_BOARD, workspaceJiraRel.getJiraProjectKey()), buildConfig());
        JSONObject boradInfo = JSON.parseObject(boradResponse);
        JSONArray boradValues = boradInfo.getJSONArray("values");
        Set<String> duplicateKey = new HashSet<>();
        allSprite = Lists.newArrayList();
        for (int i = 0; i < boradValues.size(); i++) {
            JSONObject val = boradValues.getJSONObject(i);
            String spritResponse = HttpClientUtil.get(jiraHost + String.format(JIRA_BOARD_SPRINT, val.getString("id")),
                    buildConfig());
            JSONArray spritValues = JSON.parseObject(spritResponse).getJSONArray("values");
            for (int j = 0; j < spritValues.size(); j++) {
                JSONObject spritInfoData = spritValues.getJSONObject(j);
                String id = spritInfoData.getString("id");
                if (duplicateKey.add(id)) {
                    SprintInfo sprintInfo = new SprintInfo();
                    sprintInfo.setId(spritInfoData.getString("originBoardId") + "-" + id);
                    sprintInfo.setOriginSpritId(id);
                    sprintInfo.setName(spritInfoData.getString("name"));
                    sprintInfo.setStatus(spritInfoData.getString("state"));
                    sprintInfo.setOriginBoardId(spritInfoData.getString("originBoardId"));
                    allSprite.add(sprintInfo);
                }
            }
        }
        allSprite.sort(Comparator.comparing(SprintInfo::getOriginSpritId).reversed());
        SprintListCache.put(workspaceJiraRel.getJiraProjectId(), allSprite);
        return allSprite;
    }

    public SprintDetail spritInfo(String originBoardId, String sprintId) {
        SprintDetail sprintDetail = SprintDetailCache.getIfPresent(originBoardId + sprintId);
        if (sprintDetail != null) {
            return sprintDetail;
        }
        sprintDetail = new SprintDetail();
        String jiraHost = getNotNullValue("jira.host");
        String spritResponse = HttpClientUtil.get(jiraHost + String.format(JIRA_BOARD_SPRINT, originBoardId),
                buildConfig());
        JSONArray spritValues = JSON.parseObject(spritResponse).getJSONArray("values");
        for (int j = 0; j < spritValues.size(); j++) {
            try {
                JSONObject spritInfoData = spritValues.getJSONObject(j);
                String id = spritInfoData.getString("id");
                if (StringUtils.equals(sprintId, id)) {
                    sprintDetail.setId(id);
                    sprintDetail.setName(spritInfoData.getString("name"));
                    sprintDetail.setStatus(spritInfoData.getString("state"));
                    sprintDetail.setOriginBoardId(spritInfoData.getString("originBoardId"));
                    sprintDetail.setStartTime(
                            DateUtils.parseDate(spritInfoData.getString("startDate"), DATE_FORMATE_XXX).getTime());
                    sprintDetail.setEndTime(
                            DateUtils.parseDate(spritInfoData.getString("endDate"), DATE_FORMATE_XXX).getTime());
                    break;
                }
            } catch (Exception e) {
                // IGNORE
            }
        }
        JiraIssue jiraIssue = new JiraIssue();
        String response = HttpClientUtil.get(jiraHost + String.format(JIRA_BOARD_SPRINT_ISSUE, originBoardId, sprintId),
                buildConfig());
        JSONArray issues = JSON.parseObject(response).getJSONArray("issues");
        long total = issues.size();
        jiraIssue.setTotal(total);
        long finishCount = 0, inProcessCount = 0, finisIssueTime = 0;
        Map<String, AtomicInteger> groupForIssuetype = Maps.newHashMap();
        Map<String, AtomicInteger> groupForStatus = Maps.newHashMap();
        for (int i = 0; i < total; i++) {
            JSONObject issueInfo = issues.getJSONObject(i);
            JSONObject fields = issueInfo.getJSONObject("fields");
            if (fields.containsKey("issuetype")) {
                JSONObject issuetype = fields.getJSONObject("issuetype");
                String issuetypeName = issuetype.getString("name");
                groupForIssuetype.computeIfAbsent(issuetypeName, k -> new AtomicInteger(1)).incrementAndGet();
            }
            if (fields.containsKey("status")) {
                JSONObject status = fields.getJSONObject("status");
                String statusName = status.getString("name");
                if (StringUtils.equalsAnyIgnoreCase(statusName, "已完成", "已上线")) {
                    // resolutiondate "created": "2022-10-31T14:45:53.000+0800"
                    finishCount++;
                    try {
                        finisIssueTime += DateUtils.parseDate(fields.getString("resolutiondate"), DATE_FORMATE_Z)
                                .getTime() - DateUtils.parseDate(fields.getString("created"), DATE_FORMATE_Z).getTime();
                    } catch (Exception e) {
                    }
                } else {
                    inProcessCount++;
                }
                groupForStatus.computeIfAbsent(statusName, k -> new AtomicInteger(1)).incrementAndGet();
            }
        }
        LogUtil.info("======groupForIssuetype: " + groupForIssuetype);
        LogUtil.info("======groupForStatus: " + groupForStatus);
        jiraIssue.setAvgSpendTime(finishCount > 0 ? finisIssueTime / finishCount : 0);
        jiraIssue.setFinishCount(finishCount);
        jiraIssue.setInProcessCount(inProcessCount);
        jiraIssue.setBugCount(groupForIssuetype.getOrDefault("bug", new AtomicInteger(0)).get());
        jiraIssue.setFeatureCount(groupForIssuetype.getOrDefault("feature", new AtomicInteger(0)).get());
        jiraIssue.setStroyCount(groupForIssuetype.getOrDefault("story", new AtomicInteger(0)).get());
        jiraIssue.setTestCount(groupForIssuetype.getOrDefault("test", new AtomicInteger(0)).get());
        sprintDetail.setIssue(jiraIssue);
        SprintDetailCache.put(originBoardId + sprintId, sprintDetail);
        return sprintDetail;
    }

    public Map<Long, AtomicInteger> time(int time, String originBoardId, String sprintId) {

        Map<Long, AtomicInteger> group = SprintTimeCache.getIfPresent(String.valueOf(time) + originBoardId + sprintId);
        if (group != null) {
            return group;
        }
        group = CommonUtils.defaultGroupResult(time);
        try {
            String jiraHost = getNotNullValue("jira.host");
            String response = HttpClientUtil
                    .get(jiraHost + String.format(JIRA_BOARD_SPRINT_ISSUE, originBoardId, sprintId), buildConfig());
            JSONArray issues = JSON.parseObject(response).getJSONArray("issues");
            int total = issues.size();
            for (int i = 0; i < total; i++) {
                JSONObject issueInfo = issues.getJSONObject(i);
                JSONObject fields = issueInfo.getJSONObject("fields");
                Long created = DateUtils.parseDate(fields.getString("created"), DATE_FORMATE_Z).getTime();
                group.forEach((k, v) -> {
                    if (created >= k && created < (k + DateUtils.MILLIS_PER_DAY)) {
                        v.incrementAndGet();
                    }
                });
            }
            SprintTimeCache.put(String.valueOf(time) + originBoardId + sprintId, group);
        } catch (Exception e) {
            LogUtil.error("DevopsJiraService time error", e);
        }
        return group;
    }

    public SprintDetail getLatest() {
        List<SprintInfo> allSprite = this.getAllSprite();
        allSprite.sort(Comparator.comparing(SprintInfo::getOriginSpritId).reversed());
        SprintInfo latest = allSprite.get(0);
        return this.spritInfo(latest.getOriginBoardId(), latest.getOriginSpritId());
    }

    private HttpClientConfig buildConfig() {
        String jiraUser = getNotNullValue("jira.user");
        String jiraPassword = getNotNullValue("jira.password");
        String token = jiraUser + ":" + jiraPassword;
        HttpClientConfig config = new HttpClientConfig();
        config.addHeader("Content-Type", "application/json");
        config.addHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(token.getBytes()));
        return config;
    }

    private String getNotNullValue(String key) {
        SystemParameter systemParameter = systemParameterMapper.selectByPrimaryKey(key);
        if (systemParameter == null) {
            F2CException.throwException(key + "未设置");
        }
        String value = systemParameter.getParamValue();
        if (StringUtils.isBlank(value)) {
            F2CException.throwException(key + "未设置");
        }
        return value;
    }
}
