package com.fit2cloud.devops.service.jenkins;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.commons.server.exception.F2CException;
import com.fit2cloud.commons.utils.LogUtil;
import com.fit2cloud.commons.utils.PageUtils;
import com.fit2cloud.commons.utils.Pager;
import com.fit2cloud.commons.utils.UUIDUtil;
import com.fit2cloud.devops.base.domain.DevopsJenkinsJob;
import com.fit2cloud.devops.base.domain.DevopsJenkinsJobHistory;
import com.fit2cloud.devops.base.domain.DevopsJenkinsJobHistoryExample;
import com.fit2cloud.devops.base.domain.DevopsSonarqube;
import com.fit2cloud.devops.base.domain.DevopsUnitTest;
import com.fit2cloud.devops.base.mapper.DevopsJenkinsJobHistoryMapper;
import com.fit2cloud.devops.base.mapper.DevopsJenkinsJobMapper;
import com.fit2cloud.devops.base.mapper.DevopsSonarqubeMapper;
import com.fit2cloud.devops.base.mapper.DevopsUnitTestMapper;
import com.fit2cloud.devops.common.consts.JenkinsConstants;
import com.fit2cloud.devops.dao.ext.ExtDevopsJenkinsJobHistoryMapper;
import com.fit2cloud.devops.dto.DevopsJenkinsJobHistoryDto;
import com.fit2cloud.devops.service.DevopsSonarqubeService;
import com.fit2cloud.devops.service.model.SonarqubeMetrics;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.client.JenkinsHttpConnection;
import com.offbytwo.jenkins.model.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class DevopsJenkinsJobHistoryService {

    @Resource
    private DevopsJenkinsJobHistoryMapper devopsJenkinsJobHistoryMapper;
    @Resource
    private ExtDevopsJenkinsJobHistoryMapper extDevopsJenkinsJobHistoryMapper;
    @Resource
    private DevopsJenkinsJobMapper devopsJenkinsJobMapper;
    @Resource
    private DevopsJenkinsService devopsJenkinsService;
    @Resource
    private DevopsMultiBranchJobService devopsMultiBranchJobService;
    @Resource
    private DevopsSonarqubeService sonarqubeService;
    @Resource
    private DevopsSonarqubeMapper sonarqubeMapper;
    @Resource
    private DevopsUnitTestMapper unitTestMapper;

    public Pager listAllJenkinsJob(Map<String, Object> map, int goPage, int pageSize) {
        String jobId = map.getOrDefault("jobId", "").toString();
        List<String> ids = Lists.newArrayList(jobId);
        if (StringUtils.isNotBlank(jobId)) {
            // 包括子任务的历史
            Example example = new Example(DevopsJenkinsJob.class);
            example.createCriteria().andEqualTo("parentId", jobId);
            List<DevopsJenkinsJob> childJob = devopsJenkinsJobMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(childJob)) {
                ids.addAll(childJob.stream().map(DevopsJenkinsJob::getId).collect(Collectors.toList()));
            }
        }
        map.put("jobIds", ids);
        Page page = PageHelper.startPage(goPage, pageSize, true);
        List<DevopsJenkinsJobHistoryDto> jobHistoryDtos = extDevopsJenkinsJobHistoryMapper
                .listDevopsJenkinsHistoryJob(map);
        jobHistoryDtos.forEach(e -> {
            if (StringUtils.isNotBlank(e.getActions())) {
                JSONObject actions = JSON.parseObject(e.getActions());
                e.setSonarqubeDashboardUrl(actions.getString("sonarqubeDashboardUrl"));
            }
        });
        return PageUtils.setPageInfo(page, jobHistoryDtos);
    }

    /**
     * 同步构建任务历史记录
     *
     * @param jenkinsJob 要同步的构建任务
     */
    public void syncJobHistory(DevopsJenkinsJob jenkinsJob) {
        if (StringUtils.equalsIgnoreCase(JenkinsConstants.SyncStatus.IN_SYNC.name(), jenkinsJob.getSyncStatus())) {
            return;
        }

        // 多分支流水线同步分支记录
        if (StringUtils.equals(JenkinsConstants.JOB_TYPE_MULTIBRANCH, jenkinsJob.getType())) {
            return;
        }
        jenkinsJob.setSyncStatus(JenkinsConstants.SyncStatus.IN_SYNC.name());
        devopsJenkinsJobMapper.updateByPrimaryKeySelective(jenkinsJob);
        try {
            JenkinsServer jenkinsServer = devopsJenkinsService.getJenkinsServer();
            Job job = jenkinsServer.getJob(jenkinsJob.getName());
            List<Build> serverBuildHistories = job.details().getAllBuilds();

            DevopsJenkinsJobHistoryExample devopsJenkinsJobHistoryExample = new DevopsJenkinsJobHistoryExample();
            devopsJenkinsJobHistoryExample.createCriteria().andJobIdEqualTo(jenkinsJob.getId());
            List<DevopsJenkinsJobHistory> localBuildHistories = devopsJenkinsJobHistoryMapper
                    .selectByExample(devopsJenkinsJobHistoryExample);
            final Map<Integer, DevopsJenkinsJobHistory> localBuildHistoriesMap = new HashMap<>();
            localBuildHistories.forEach(localHistory -> {
                if (localBuildHistoriesMap.get(localHistory.getOrderNum()) != null) {
                    devopsJenkinsJobHistoryMapper.deleteByPrimaryKey(localHistory.getId());
                } else {
                    localBuildHistoriesMap.put(localHistory.getOrderNum(), localHistory);
                }
            });
            serverBuildHistories.forEach(serverBuild -> {
                try {
                    BuildWithDetails buildWithDetails = serverBuild.details();
                    // 这里就是更新
                    if (localBuildHistoriesMap.get(serverBuild.getNumber()) != null) {
                        DevopsJenkinsJobHistory history = setJobHistory(
                                localBuildHistoriesMap.get(serverBuild.getNumber()), buildWithDetails);
                        history.setJobName(jenkinsJob.getName());
                        devopsJenkinsJobHistoryMapper.updateByPrimaryKeySelective(history);
                    } else {
                        // 这里就是新增或者是在同步时阻塞构建插入了记录，就需要更新
                        DevopsJenkinsJobHistoryExample example = new DevopsJenkinsJobHistoryExample();
                        example.createCriteria()
                                .andJobIdEqualTo(jenkinsJob.getId())
                                .andOrderNumEqualTo(serverBuild.getNumber());
                        List<DevopsJenkinsJobHistory> tmpHistories = devopsJenkinsJobHistoryMapper
                                .selectByExample(example);

                        DevopsJenkinsJobHistory history = new DevopsJenkinsJobHistory();
                        setJobHistory(history, buildWithDetails);
                        history.setJobName(jenkinsJob.getName());
                        history.setJobId(jenkinsJob.getId());
                        if (CollectionUtils.isEmpty(tmpHistories)) {
                            history.setId(UUIDUtil.newUUID());
                            devopsJenkinsJobHistoryMapper.insert(history);
                        } else {
                            DevopsJenkinsJobHistory tmpHistory = tmpHistories.get(0);
                            history.setId(tmpHistory.getId());
                            devopsJenkinsJobHistoryMapper.updateByPrimaryKeySelective(history);
                        }
                    }
                } catch (Exception e) {
                    LogUtil.error(jenkinsJob.getName() + " 历史构建 " + serverBuild.getNumber() + "获取详情异常");
                    e.printStackTrace();
                } finally {
                    localBuildHistoriesMap.remove(serverBuild.getNumber());
                }
            });
            // 这里剩下的就是要删除的了
            localBuildHistoriesMap.values().parallelStream()
                    .forEach(history -> devopsJenkinsJobHistoryMapper.deleteByPrimaryKey(history.getId()));

            jenkinsJob.setSyncStatus(JenkinsConstants.SyncStatus.END_SYNC.name());
            jenkinsJob.setBuildSize(serverBuildHistories.size());
        } catch (Exception e) {
            jenkinsJob.setSyncStatus(JenkinsConstants.SyncStatus.ERROR_SYNC.name());
            LogUtil.error(String.format("获取构建历史异常[%s],Error: %s", jenkinsJob.getName(), e.getMessage()));
            e.printStackTrace();
        } finally {
            if (StringUtils.equalsIgnoreCase(jenkinsJob.getSyncStatus(), JenkinsConstants.SyncStatus.IN_SYNC.name())) {
                jenkinsJob.setSyncStatus(JenkinsConstants.SyncStatus.ERROR_SYNC.name());
            }
            devopsJenkinsJobMapper.updateByPrimaryKeySelective(jenkinsJob);
        }
    }

    public DevopsJenkinsJobHistory setJobHistory(DevopsJenkinsJobHistory jobHistory,
            BuildWithDetails buildWithDetails) {
        jobHistory.setName(buildWithDetails.getDisplayName());
        jobHistory.setOrderNum(buildWithDetails.getNumber());
        jobHistory.setUrl(buildWithDetails.getUrl());
        jobHistory.setBuildTime(buildWithDetails.getTimestamp());
        jobHistory.setDurationTime(buildWithDetails.getDuration());
        jobHistory.setIsBuilding(buildWithDetails.isBuilding());
        jobHistory.setDescription(buildWithDetails.getDescription());

        // Add: 转换为json字符串格式 持久化
        List<Map<String, Object>> acts = (List<Map<String, Object>>) buildWithDetails.getActions();
        Map<String, Object> newMap = new HashMap<>();
        for (Map<String, Object> act : acts) {
            if (!act.isEmpty()) {
                newMap.putAll(act);
            }
        }
        JSONObject actions = new JSONObject(newMap);
        jobHistory.setActions(actions.toJSONString());

        // BUG: 构建时buildWithDetails.getResult()返回的是SUCCESS 但状态依然为构建中
        if (buildWithDetails.isBuilding()) {
            jobHistory.setBuildStatus(BuildResult.BUILDING.name());
        } else {
            jobHistory.setBuildStatus(buildWithDetails.getResult().name());
        }
        jobHistory.setSyncTime(System.currentTimeMillis());
        return jobHistory;
    }

    public String getOutputText(String historyId) {
        DevopsJenkinsJobHistory history = devopsJenkinsJobHistoryMapper.selectByPrimaryKey(historyId);
        try {
            if (history == null) {
                DevopsJenkinsJob devopsJenkinsJob = devopsJenkinsJobMapper.selectByPrimaryKey(historyId);
                return devopsMultiBranchJobService.getIndexOutputText(devopsJenkinsJob.getName());
            }
            DevopsJenkinsJob devopsJenkinsJob = devopsJenkinsJobMapper.selectByPrimaryKey(history.getJobId());
            if (StringUtils.equals(JenkinsConstants.JOB_TYPE_MULTIBRANCH, devopsJenkinsJob.getType())) {
                return devopsMultiBranchJobService.getChildOutputText(history);
            }
            AtomicReference<String> output = new AtomicReference<>();
            JenkinsServer jenkinsServer = devopsJenkinsService.getJenkinsServer();
            JobWithDetails job = jenkinsServer.getJob(history.getJobName());
            for (Build tmpBuild : job.getAllBuilds()) {
                if (history.getOrderNum().equals(tmpBuild.getNumber())) {
                    output.set(tmpBuild.details().getConsoleOutputText());
                    break;
                }
            }
            return output.get();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.error("查询日志输出失败！" + e.getMessage());
        }
        return StringUtils.EMPTY;
    }

    // 持久化的数据获取
    public String getActions(String historyId) {
        DevopsJenkinsJobHistory history = devopsJenkinsJobHistoryMapper.selectByPrimaryKey(historyId);
        return history.getActions();
    }

    // 通过api接口调用jenkins获取
    public String getActions(String historyId, int buildNumber) {

        DevopsJenkinsJobHistory history = devopsJenkinsJobHistoryMapper.selectByPrimaryKey(historyId);

        try {
            JenkinsServer jenkinsServer = devopsJenkinsService.getJenkinsServer();
            JobWithDetails job = jenkinsServer.getJob(history.getJobName());
            List<Build> builds = job.getAllBuilds();
            int finalBuildNumber = buildNumber;
            Optional<Build> optBuild = builds.stream().filter(build -> build.getNumber() == finalBuildNumber)
                    .findFirst();

            if (optBuild.isPresent()) {
                List<Map<String, Object>> acts = (List<Map<String, Object>>) optBuild.get().details().getActions();
                Map<String, Object> newMap = new HashMap<>();
                for (Map<String, Object> act : acts) {
                    if (!act.isEmpty()) {
                        newMap.putAll(act);
                    }
                }
                JSONObject json = new JSONObject(newMap);
                return json.toJSONString();
            }

        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.error("查询变更记录失败！" + e.getMessage());
        }
        return null;
    }

    public List<BuildChangeSetItem> showUpdateRecording(String historyId) {
        DevopsJenkinsJobHistory history = devopsJenkinsJobHistoryMapper.selectByPrimaryKey(historyId);
        try {
            DevopsJenkinsJob devopsJenkinsJob = devopsJenkinsJobMapper.selectByPrimaryKey(history.getJobId());
            if (StringUtils.equals(JenkinsConstants.JOB_TYPE_MULTIBRANCH, devopsJenkinsJob.getType())) {
                return null;
            }
            List<BuildChangeSetItem> list = new ArrayList<>();
            JenkinsServer jenkinsServer = devopsJenkinsService.getJenkinsServer();
            JobWithDetails job = jenkinsServer.getJob(history.getJobName());

            for (Build tmpBuild : job.getAllBuilds()) {
                if (history.getOrderNum().equals(tmpBuild.getNumber())) {
                    List<BuildChangeSet> changeSets = tmpBuild.details().getChangeSets();
                    for (BuildChangeSet buildChangeSet : changeSets) {
                        list = buildChangeSet.getItems();
                    }
                    break;
                }
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.error("查询变更记录失败！" + e.getMessage());
        }
        return null;
    }

    public void deleteJobHistory(String jenkinsJobId) {
        if (StringUtils.isBlank(jenkinsJobId)) {
            F2CException.throwException("删除构建历史，构建任务ID不能为空");
        }
        DevopsJenkinsJobHistoryExample historyExample = new DevopsJenkinsJobHistoryExample();
        historyExample.createCriteria().andJobIdEqualTo(jenkinsJobId);
        List<DevopsJenkinsJobHistory> historyList = devopsJenkinsJobHistoryMapper.selectByExample(historyExample);
        if (!historyList.isEmpty()) {
            for (DevopsJenkinsJobHistory history : historyList) {
                devopsJenkinsJobHistoryMapper.deleteByPrimaryKey(history.getId());
            }
        }
    }

    // 正常构建后 同步状态
    public void syncBuildHistoryStatus(List<DevopsJenkinsJobHistory> histories) {
        JenkinsServer jenkinsServer = devopsJenkinsService.getJenkinsServer();
        if (!Optional.ofNullable(jenkinsServer).isPresent()) {
            LogUtil.error(String.format("get Jenkins server fail."));
            return;
        }
        Integer index = 0;
        for (DevopsJenkinsJobHistory history: histories) {
            index++;
            try {
                if (StringUtils.equalsIgnoreCase(history.getSyncStatus(), JenkinsConstants.SyncStatus.IN_SYNC.name())) {
                    continue;
                }
                // 排除多分支流水线构建类型(分支流水线构建类型触发构建就会同步历史记录)
                DevopsJenkinsJob devopsJenkinsJob = devopsJenkinsJobMapper.selectByPrimaryKey(history.getJobId());
                if (devopsJenkinsJob != null
                        && StringUtils.equals(JenkinsConstants.JOB_TYPE_MULTIBRANCH, devopsJenkinsJob.getType())) {
                    continue;
                }
                JobWithDetails job = jenkinsServer.getJob(history.getJobName());
                if (job == null) {
                    LogUtil.warn(String.format("jenkins job[%s] not exist,delete history[%s]", history.getJobName(), history.getId()));
                    try {
                        devopsJenkinsJobHistoryMapper.deleteByPrimaryKey(history.getId());
                    } catch (Exception e) {
                        LogUtil.error(String.format("delete jenkins history build[%s] fail.", history.getId()));
                    }
                    continue;
                }
                history.setSyncStatus(JenkinsConstants.SyncStatus.IN_SYNC.name());
                Optional<Build> targetBuild = job.getBuildByNumber(history.getOrderNum().intValue());
                if (targetBuild.isPresent()) {
                    try {
                        setJobHistory(history, targetBuild.get().details());
                        history.setSyncStatus(JenkinsConstants.SyncStatus.END_SYNC.name());
                        saveSonarqubeMertics(history); // 增加生成sonarqube历史数据
                        saveTestReport(history);
                    } catch (Exception e) {
                        LogUtil.error(String.format("update jenkins history build[%s][%s] fail.", history.getJobName(), history.getOrderNum().toString()));
                        history.setSyncStatus(JenkinsConstants.SyncStatus.NO_SYNC.name());
                    }
                    devopsJenkinsJobHistoryMapper.updateByPrimaryKeySelective(history);                   
                } else {
                    LogUtil.error(String.format("jenkins job[%s] can't find number of [%s] build.", history.getJobName(), history.getOrderNum().toString()));
                    try {
                        devopsJenkinsJobHistoryMapper.deleteByPrimaryKey(history.getId());
                    } catch (Exception e) {
                        LogUtil.error(String.format("delete jenkins build[%s] fail.", history.getId()));
                    }
                }
            } catch (Exception e) {
                history.setSyncStatus(JenkinsConstants.SyncStatus.ERROR_SYNC.name());
                devopsJenkinsJobHistoryMapper.updateByPrimaryKeySelective(history);
                LogUtil.error(String.format("获取构建任务[%s]历史详情失败[%s],Error: %s", history.getJobName(), history.getName(),
                        e.getMessage()));
                e.printStackTrace();
            }
            if (index % 10 == 0) {
                try {
                    LogUtil.warn(String.format("已同步历史构建任务[%s]进行中...等待下一次调度", String.valueOf(index)));
                    TimeUnit.MILLISECONDS.sleep(800); //避免批量执行造成数据压力
                } catch (Exception e) {
                    LogUtil.error("sleep fail");
                }
            }
        }
    }

    // 正常构建后 同步状态
    public void syncBuildHistoryStatusV2(List<DevopsJenkinsJobHistory> histories) {
        JenkinsServer jenkinsServer = devopsJenkinsService.getJenkinsServer();
        histories.forEach(history -> Optional.ofNullable(jenkinsServer).ifPresent(server -> {
            try {
                if (StringUtils.equalsIgnoreCase(history.getSyncStatus(), JenkinsConstants.SyncStatus.IN_SYNC.name())) {
                    return;
                }
                //排除多分支流水线构建类型(分支流水线构建类型触发构建就会同步历史记录)
                DevopsJenkinsJob devopsJenkinsJob = devopsJenkinsJobMapper.selectByPrimaryKey(history.getJobId());
                if(devopsJenkinsJob != null && StringUtils.equals(JenkinsConstants.JOB_TYPE_MULTIBRANCH,devopsJenkinsJob.getType())){
                    return;
                }
                history.setSyncStatus(JenkinsConstants.SyncStatus.IN_SYNC.name());
                devopsJenkinsJobHistoryMapper.updateByPrimaryKeySelective(history);

                JobWithDetails job = jenkinsServer.getJob(history.getJobName());
                List<Build> allBuilds = job.getAllBuilds();
                for (Build build : allBuilds) {
                    if (history.getOrderNum().equals(build.getNumber())) {
                        setJobHistory(history, build.details());
                        history.setSyncStatus(JenkinsConstants.SyncStatus.END_SYNC.name());
                        devopsJenkinsJobHistoryMapper.updateByPrimaryKey(history);
                        saveSonarqubeMertics(history); // 增加生成sonarqube历史数据
                        saveTestReport(history);
                        return;
                    }
                }
            } catch (Exception e) {
                history.setSyncStatus(JenkinsConstants.SyncStatus.ERROR_SYNC.name());
                devopsJenkinsJobHistoryMapper.updateByPrimaryKeySelective(history);
                LogUtil.info(String.format("获取构建任务[%s]历史详情失败[%s],Error: %s", history.getJobName(), history.getName(), e.getMessage()));
                e.printStackTrace();
            }
        }));
    }

    /**
     * 删除构建历史
     *
     * @param devopsJenkinsJobHistories 需要删除的构建历史
     */
    public void deleteJobHistories(List<DevopsJenkinsJobHistory> devopsJenkinsJobHistories) {
        if (CollectionUtils.isNotEmpty(devopsJenkinsJobHistories)) {
            JenkinsServer jenkinsServer = devopsJenkinsService.getJenkinsServer();
            devopsJenkinsJobHistories.forEach(buildHistory -> {
                try {
                    devopsJenkinsJobHistoryMapper.deleteByPrimaryKey(buildHistory.getId());
                    JobWithDetails job = jenkinsServer.getJob(buildHistory.getJobName());
                    Optional.ofNullable(job).flatMap(tmpJob -> tmpJob.getBuildByNumber(buildHistory.getOrderNum()))
                            .ifPresent(build -> {
                                try {
                                    JenkinsHttpConnection client = job.getClient();
                                    client.post(build.getUrl() + "doDelete", true);
                                    DevopsJenkinsJob devopsJenkinsJob = devopsJenkinsJobMapper
                                            .selectByPrimaryKey(buildHistory.getJobId());
                                    devopsJenkinsJob.setBuildSize(job.getAllBuilds().size());
                                    devopsJenkinsJobMapper.updateByPrimaryKeySelective(devopsJenkinsJob);
                                } catch (Exception e) {
                                    F2CException.throwException("删除历史记录失败！" + e.getMessage());
                                    e.printStackTrace();
                                }
                            });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public TestReport getJenkinsJobTestReport(DevopsJenkinsJob jenkinsJob) {
        JenkinsServer jenkinsServer = devopsJenkinsService.getJenkinsServer();
        try {
            JobWithDetails job = jenkinsServer.getJob(jenkinsJob.getName());
            Build jobLastBuild = job.getLastCompletedBuild();
            return jobLastBuild.getTestReport();
        } catch (Exception e) {
            LogUtil.error(String.format("获取构建任务[%s]测试报告失败", jenkinsJob.getName()));
            return null;
        }
    }

    public TestReport getJenkinsJobTestReportById(String jobId) {
        return (getJenkinsJobTestReport(devopsJenkinsJobMapper.selectByPrimaryKey(jobId)));
    }

    public TestReport getJenkinsJobTestReportByName(String jobName) {
        Example example = new Example(DevopsJenkinsJob.class);
        example.createCriteria().andEqualTo("name", jobName).andIsNotNull("appId");
        List<DevopsJenkinsJob> jobs = devopsJenkinsJobMapper.selectByExample(example);
        if (jobs.isEmpty()) {
            LogUtil.error(String.format("jobName[%s] is not exsit.", jobName));
            return null;
        }
        return getJenkinsJobTestReport(jobs.get(0));
    }

    public DevopsUnitTest saveTestReport(DevopsJenkinsJobHistory history) {
        JenkinsServer jenkinsServer = devopsJenkinsService.getJenkinsServer();
        try {
            JobWithDetails job = jenkinsServer.getJob(history.getJobName());
            if (job == null) {
                LogUtil.error(String.format("获取构建任务[%s]失败，请检查任务是否存在", history.getJobName()));
                return null;
            }
            Build jobBuild = job.getLastBuild();
            if (jobBuild == null) {
                LogUtil.error(
                        String.format("获取构建历史任务[%s-%s] 不存在", history.getName(), history.getOrderNum().toString()));
                return null;
            }
            try {
                TestReport result = jobBuild.getTestReport();
                LogUtil.info(String.format("构建任务[%s-%s] 测试报告生成", job.getName(), String.valueOf(jobBuild.getNumber())));
                DevopsUnitTest test = new DevopsUnitTest();
                test.setCreateTime(history.getBuildTime());
                test.setId(history.getId());
                test.setJobHistoryId(history.getId());
                test.setAllCount(Long.valueOf(result.getTotalCount()));
                test.setSkipCount(Long.valueOf(result.getSkipCount()));
                test.setFailCount(Long.valueOf(result.getFailCount()));
                test.setUrl(jobBuild.getUrl());
                if (!unitTestMapper.existsWithPrimaryKey(history.getId())) {
                    unitTestMapper.insert(test);
                }
                return test;
            } catch (Exception e) {
                return null;
            }
        } catch (Exception e) {
            LogUtil.error(String.format("获取构建历史任务[%s-%s]测试报告异常，error:%s", history.getJobName(),
                    history.getOrderNum().toString(), e.getMessage()));
            return null;
        }
    }

    public DevopsSonarqube saveSonarqubeMertics(DevopsJenkinsJobHistory history) {
        if (history == null) {
            LogUtil.error("saveSonarqubeMertics but job is null.");
            return null;
        }

        DevopsJenkinsJob jenkinsJob = devopsJenkinsJobMapper.selectByPrimaryKey(history.getJobId());
        if (jenkinsJob == null) {
            LogUtil.error(String.format("get job [%s] with history[%s] fail.", history.getJobName(), history.getId()));
            return null;
        }
        return saveSonarqubeMertics(jenkinsJob, history.getId());
    }

    public DevopsSonarqube saveSonarqubeMertics(DevopsJenkinsJob jenkinsJob, String historyId) {
        SonarqubeMetrics mertics = sonarqubeService.getSonarqubeMertics(jenkinsJob, false);
        if (mertics == null) {
            return null;
        }
        SonarqubeMetrics newMertics = sonarqubeService.getSonarqubeMertics(jenkinsJob, true);
        if (mertics == null) {
            LogUtil.error(String.format("get Sonarqube new Mertics with job [%s]", jenkinsJob.getName()));
            return null;
        }
        DevopsSonarqube sonarOut = new DevopsSonarqube();
        sonarOut.setId(historyId);
        sonarOut.setCreateTime(System.currentTimeMillis());
        sonarOut.setJobHistoryId(historyId);
        sonarOut.setProjectKey(jenkinsJob.getName());
        sonarOut.setBugs(mertics.getBugs());
        sonarOut.setDebt(mertics.getDebt());
        sonarOut.setNcloc(mertics.getCodeLines());
        sonarOut.setVulnerabilities(mertics.getVulnerabilities());
        sonarOut.setTestCoverage(((Double) (0.01 * mertics.getTestCoverage())).floatValue());
        sonarOut.setTestLine(mertics.getCoverageCodeLine());
        sonarOut.setDuplicatedRate(((Double) (0.01 * mertics.getDuplicatedRate())).floatValue());
        sonarOut.setIssues(mertics.getIssue());
        sonarOut.setOpenIssue(mertics.getOpenIssue());
        sonarOut.setFalsePositionIssue(mertics.getFalsePositionIssue());
        sonarOut.setConfiredIssue(mertics.getConfiredIssue());
        sonarOut.setNewLines(newMertics.getCodeLines());
        sonarOut.setNewTestCoverage(((Double) (0.01 * newMertics.getTestCoverage())).floatValue());
        sonarOut.setNewTestLine(newMertics.getCoverageCodeLine());
        sonarOut.setNewVulnerabilities(newMertics.getVulnerabilities());
        sonarOut.setUrl(sonarqubeService.getSonarqubeDashboardUrl(jenkinsJob));
        if (!sonarqubeMapper.existsWithPrimaryKey(historyId)) {
            sonarqubeMapper.insert(sonarOut);
        }
        return sonarOut;
    }

}
