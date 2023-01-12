package com.fit2cloud.devops.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.fit2cloud.commons.server.base.domain.SystemParameter;
import com.fit2cloud.commons.server.base.mapper.SystemParameterMapper;
import com.fit2cloud.commons.utils.GlobalConfigurations;
import com.fit2cloud.commons.utils.LogUtil;
import com.fit2cloud.commons.utils.UUIDUtil;
import com.fit2cloud.devops.base.domain.*;
import com.fit2cloud.devops.base.mapper.*;
import com.fit2cloud.devops.common.consts.JenkinsConstants;
import com.fit2cloud.devops.common.model.BasicPipelineEvent;
import com.fit2cloud.devops.common.model.DevopsPipelineMessage;
import com.fit2cloud.devops.common.model.PipelineContext;
import com.fit2cloud.devops.common.model.PipelineEventType;
import com.fit2cloud.devops.service.jenkins.DevopsJenkinsJobHistoryService;
import com.fit2cloud.devops.service.jenkins.DevopsJenkinsService;
import com.fit2cloud.devops.service.jenkins.DevopsMultiBranchJobService;
import com.fit2cloud.devops.service.jenkins.model.event.OperationType;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.offbytwo.jenkins.model.BuildResult;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.util.NamedThreadFactory;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author caiwzh
 * jenkins任务构建历史
 * 串联unitest、sonarqube记录、自动化api测试、deploy结果
 * @date 2022/11/25
 */
@Service
public class ApplicationPipelineSevice {
    @Resource
    private DevopsJenkinsJobMapper devopsJenkinsJobMapper;

    private static final String KEY = "%s-%s";

    public static final String FORMATE = "yyyy-MM-dd HH:mm:ss";

    private SerializerFeature[] serializerFeatures = {SerializerFeature.WriteNullStringAsEmpty, SerializerFeature.WriteNullNumberAsZero};

    public static final long DEFAULT_TIME_OUT = TimeUnit.MINUTES.toMillis(5);
    //jobName+buildNumber key
    private Map<String, PipelineTask> taskList = Maps.newConcurrentMap();

    private List<String> unknowKeyList = Lists.newCopyOnWriteArrayList();

    @Resource
    private DevopsJenkinsService devopsJenkinsService;
    @Resource
    private ApplicationDeploymentMapper applicationDeploymentMapper;
    @Resource
    private DevopsJenkinsJobHistoryMapper devopsJenkinsJobHistoryMapper;
    @Resource
    private DevopsJenkinsJobHistoryService devopsJenkinsJobHistoryService;
    @Resource
    private ApplicationDeploymentEventLogMapper applicationDeploymentEventLogMapper;
    @Resource
    private ClusterService clusterService;
    @Resource
    private ClusterRoleService clusterRoleService;
    @Resource
    private DevopsCloudServerMapper devopsCloudServerMapper;
    @Resource
    private ApplicationMapper applicationMapper;
    @Resource
    private SystemParameterMapper systemParameterMapper;
    @Resource
    private DevopsJenkinsParamsMapper devopsJenkinsParamsMapper;
    @Resource
    private DevopsApplicationPipelineMapper devopsApplicationPipelineMapper;
    @Resource
    private MessageProducer messageProducer;
    @Resource
    private DevopsMultiBranchJobService devopsMultiBranchJobService;
    //jenkins 地址
    private String jenkinsAddress;
    //云管地址
    private String f2cEndpoint;
    //执行job
    private ExecutorService executorJobService = Executors.newSingleThreadExecutor(new NamedThreadFactory("ApplicationPipeline"));

    @PostConstruct
    public void init() throws InterruptedException {
        executorJobService.execute(() -> checkTaskStatus());
    }

    private void initParam() {
        try {
            SystemParameter systemParameter = systemParameterMapper.selectByPrimaryKey("jenkins.address");
            if (systemParameter != null && StringUtils.isNotBlank(systemParameter.getParamValue())) {
                jenkinsAddress = systemParameter.getParamValue();
            }
            DevopsJenkinsParamsExample devopsJenkinsParamsExample = new DevopsJenkinsParamsExample();
            devopsJenkinsParamsExample.createCriteria().andParamKeyEqualTo("f2c_endpoint");
            List<DevopsJenkinsParams> devopsJenkinsParams = devopsJenkinsParamsMapper.selectByExample(devopsJenkinsParamsExample);
            if (CollectionUtils.isNotEmpty(devopsJenkinsParams)) {
                f2cEndpoint = devopsJenkinsParams.get(0).getParamValue();
            }
        } catch (Exception e) {
            LogUtil.warn("initParam fail", e);
        }
    }

    private void checkTaskStatus() {
        AtomicInteger count = new AtomicInteger(0);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                List<String> finishedKeys = Lists.newArrayList();
                long nowTime = System.currentTimeMillis();
                taskList.forEach((k, v) -> {
                    DevopsJenkinsJobHistoryExample example = new DevopsJenkinsJobHistoryExample();
                    example.createCriteria().andJobIdEqualTo(v.getJobId()).andOrderNumEqualTo(Integer.valueOf(v.getBuildNumber()));
                    List<DevopsJenkinsJobHistory> devopsJenkinsJobHistories = devopsJenkinsJobHistoryMapper.selectByExample(example);
                    if (CollectionUtils.isNotEmpty(devopsJenkinsJobHistories)) {
                        DevopsJenkinsJobHistory devopsJenkinsJobHistory = devopsJenkinsJobHistories.get(0);
                        //构建结束后 还有自动化测试流程
                        if (!StringUtils.equals(devopsJenkinsJobHistory.getBuildStatus(), BuildResult.BUILDING.name())) {
                            v.setHistory(devopsJenkinsJobHistory);
                            //流水线结束标志
                            boolean deployFlag = !v.isAutoDeploy() || v.applicationDeployment != null;
                            boolean apiTestFlag = !v.isAutoApiTest() || v.apiTestLog != null;
                            if (deployFlag && apiTestFlag) {
                                v.setFinished(true);
                            }
                            //构建失败 流程结束
                            if (!StringUtils.equals(devopsJenkinsJobHistory.getBuildStatus(), BuildResult.SUCCESS.name())) {
                                v.setFinished(true);
                            }
                        }
                    }
                    if (v.isFinished()) {
                        finishedKeys.add(k);
                        if (StringUtils.equals(v.getHistory().getBuildStatus(), BuildResult.BUILDING.name())) {
                            devopsJenkinsJobHistories = devopsJenkinsJobHistoryMapper.selectByExample(example);
                            if (CollectionUtils.isNotEmpty(devopsJenkinsJobHistories)) {
                                v.setHistory(devopsJenkinsJobHistories.get(0));
                            }
                        }
                    } else {
                        long duration = nowTime - v.getStartTime();
                        if (duration > v.getTimeout()) {
                            v.setTimeout(v.getTimeout() + DEFAULT_TIME_OUT);
                            LogUtil.warn(String.format("构建任务已进行【%s】分钟，【%s】", TimeUnit.MILLISECONDS.toMinutes(duration), v));
                        }
                    }
                });
                if (CollectionUtils.isNotEmpty(finishedKeys)) {
                    initParam();
                    finishedKeys.forEach(k -> onPipelineFinish(taskList.remove(k)));
                }
                //90s同步一次
                if (CollectionUtils.isNotEmpty(unknowKeyList) && count.get() > 30) {
                    count.set(0);
                    LogUtil.info("unknowKeyList: " + unknowKeyList);
                    onUnknowKeyTask(unknowKeyList);
                }
                TimeUnit.SECONDS.sleep(3);
                count.incrementAndGet();
            } catch (Exception e) {
                LogUtil.error("ApplicationPipelineSevice checkTaskStatus err", e);
            }
        }
    }

    /**
     * jenkins触发的部分 自动同步历史记录
     *
     * @param unknowKeyList
     */
    private void onUnknowKeyTask(List<String> unknowKeyList) {
        Set<String> taskSet = Sets.newHashSet(unknowKeyList);
        unknowKeyList.clear();
        List<String> jobs = Lists.newArrayListWithCapacity(taskSet.size());
        for (String tn : taskSet) {
            String jobName = StringUtils.substringBeforeLast(tn, "_");
            jobs.add(jobName);
        }
        Example devopsJenkinsJobExample = new Example(DevopsJenkinsJob.class);
        devopsJenkinsJobExample.createCriteria().andIn("name", jobs);
        List<DevopsJenkinsJob> devopsJenkinsJobs = devopsJenkinsJobMapper.selectByExample(devopsJenkinsJobExample);
        if (CollectionUtils.isNotEmpty(devopsJenkinsJobs)) {
            devopsJenkinsJobs.forEach(e -> devopsMultiBranchJobService.syncAllBranchHistory(e, OperationType.BUILD));
        }
    }

    /**
     * 流水线结束
     *
     * @param pipelineTask
     */
    private void onPipelineFinish(PipelineTask pipelineTask) {
        //
        LogUtil.info("********************onPipelineFinish***********************");
        //unitest,sonarqube记录
        LogUtil.info(pipelineTask.getDevopsJenkinsJob().getName() + "=======================unitest,sonarqube记录===========================");
        doTestAndSonar(pipelineTask);
        DevopsPipelineMessage devopsPipelineMessage = convertToMessage(pipelineTask);
        LogUtil.info(JSON.toJSONString(devopsPipelineMessage, SerializerFeature.PrettyFormat, SerializerFeature.WriteNullStringAsEmpty));
        //Rabbit mq
        try {
            messageProducer.sendMsg(JSON.toJSONString(devopsPipelineMessage, serializerFeatures));
        } catch (Exception e) {
            LogUtil.warn("messageProducer sendMsg fail," + e.getMessage());
        }
        //SAVE DB
        DevopsApplicationPipeline devopsApplicationPipeline = new DevopsApplicationPipeline();
        devopsApplicationPipeline.setId(devopsPipelineMessage.getId());
        devopsApplicationPipeline.setCreateTime(pipelineTask.history.getBuildTime());
        devopsApplicationPipeline.setJobHistoryId(pipelineTask.history.getId());
        devopsApplicationPipeline.setJobId(pipelineTask.getJobId());
        if (pipelineTask.applicationDeployment != null) {
            devopsApplicationPipeline.setDeploymentId(pipelineTask.applicationDeployment.getId());
        }
        if (pipelineTask.devopsSonarqube != null) {
            devopsApplicationPipeline.setSonarqubeId(pipelineTask.devopsSonarqube.getId());
        }
        if (pipelineTask.devopsUnitTest != null) {
            devopsApplicationPipeline.setUnittestId(pipelineTask.devopsUnitTest.getId());
        }
        devopsApplicationPipeline.setTriggerName(devopsPipelineMessage.getUseName());
        devopsApplicationPipeline.setAppName(devopsPipelineMessage.getAppName());
        devopsApplicationPipelineMapper.insertSelective(devopsApplicationPipeline);
    }

    /**
     * 云管构建任务入列
     *
     * @param history
     */
    public void bindJobHistory(DevopsJenkinsJobHistory history) {
        String key = String.format(KEY, history.getJobName(), history.getOrderNum());
        if (taskList.containsKey(key)) {
            LogUtil.info(String.format("云管构建任务入列 key： %s，已存在", key));
            return;
        }
        String jobId = history.getJobId();
        DevopsJenkinsJob jenkinsJob = devopsJenkinsJobMapper.selectByPrimaryKey(jobId);
        if (jenkinsJob == null) {
            LogUtil.warn("job 不存在，jobId:" + jobId);
            return;
        }
        //多分支类型取父任务配置
        if (StringUtils.isNotBlank(jenkinsJob.getParentId())) {
            DevopsJenkinsJob parentJenkinsJob = devopsJenkinsJobMapper.selectByPrimaryKey(jenkinsJob.getParentId());
            jenkinsJob.setExtParam(parentJenkinsJob.getExtParam());
        }
        PipelineTask pipelineTask = PipelineTask.builder().jobId(jobId)
                .name(jenkinsJob.getName()).devopsJenkinsJob(jenkinsJob)
                .startTime(System.currentTimeMillis()).buildHistoryId(history.getId())
                .buildNumber(String.valueOf(history.getOrderNum()))
                .history(history)
                .timeout(DEFAULT_TIME_OUT)
                .build();
        JSONObject publish = devopsJenkinsService.getPublish(jenkinsJob);
        if (publish != null) {
            boolean autoDeploy = publish.getBooleanValue("autoDeploy");
            boolean autoApiTest = publish.getBooleanValue("autoApiTest");
            pipelineTask.setAutoDeploy(autoDeploy);
            pipelineTask.setAutoApiTest(autoApiTest);
        }
        String appId = jenkinsJob.getAppId();
        if (StringUtils.isNotBlank(appId) && !"none".equals(appId)) {
            Application application = applicationMapper.selectByPrimaryKey(appId);
            pipelineTask.setApplicationName(application.getName());
        }
        taskList.put(key, pipelineTask);
    }

    /**
     * 部署结束触发
     *
     * @param pipelineContext
     */
    public void onDeployComplete(PipelineContext pipelineContext) {
        if (StringUtils.isBlank(pipelineContext.getJobName())) {
            return;
        }
        String key = String.format(KEY, pipelineContext.getJobName(), pipelineContext.getBuildNumber());
        PipelineTask pipelineTask = taskList.get(key);
        if (pipelineTask != null) {
            pipelineTask.setPipelineContext(pipelineContext);
            //部署结果
            ApplicationDeployment deployment = applicationDeploymentMapper.selectByPrimaryKey(pipelineContext.getApplicationDeploymentId());
            pipelineTask.setApplicationDeployment(deployment);
            if (!pipelineTask.isAutoApiTest()) {
                pipelineTask.setFinished(true);
            }
        } else {
            unknowKeyList.add(key);
            LogUtil.warn("onDeployComplete 未找到PipelineTask，key：" + key);
        }
    }

    /**
     * 增加生成sonarqube历史数据
     *
     * @param pipelineTask
     */
    private void doTestAndSonar(PipelineTask pipelineTask) {
        try {
            DevopsSonarqube devopsSonarqube = devopsJenkinsJobHistoryService.saveSonarqubeMertics(pipelineTask.getHistory());
            pipelineTask.setDevopsSonarqube(devopsSonarqube);
        } catch (Exception e) {
            LogUtil.error("saveSonarqubeMertics error", e);
        }
        try {
            DevopsUnitTest devopsUnitTest = devopsJenkinsJobHistoryService.saveTestReport(pipelineTask.getHistory());
            pipelineTask.setDevopsUnitTest(devopsUnitTest);
        } catch (Exception e) {
            LogUtil.error("saveTestReport error", e);
        }
    }

    /**
     * 自动化api测试结束触发
     *
     * @param pipelineContext
     */
    public void onApiTestComplete(PipelineContext pipelineContext) {
        if (StringUtils.isBlank(pipelineContext.getJobName())) {
            return;
        }
        String key = String.format(KEY, pipelineContext.getJobName(), pipelineContext.getBuildNumber());
        PipelineTask pipelineTask = taskList.get(key);
        if (pipelineTask != null) {
            ApplicationDeploymentEventLogExample example = new ApplicationDeploymentEventLogExample();
            example.createCriteria().andDeploymentLogIdEqualTo(pipelineContext.getApplicationDeploymentId());
            List<ApplicationDeploymentEventLog> applicationDeploymentEventLogs = applicationDeploymentEventLogMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(applicationDeploymentEventLogs)) {
                //自动化api测试log
                ApplicationDeploymentEventLog applicationDeploymentEventLog = applicationDeploymentEventLogs.get(0);
                pipelineTask.setApiTestLog(applicationDeploymentEventLog);
                //自动化api测试数据
                pipelineTask.setApiTest(pipelineContext.getDevopsApiTest());
            }
            pipelineTask.setFinished(true);
        } else {
            unknowKeyList.add(key);
            LogUtil.warn("onApiTestComplete 未找到PipelineTask，key：" + key);
        }
    }

    public DevopsPipelineMessage convertToMessage(PipelineTask pipelineTask) {
        DevopsPipelineMessage devopsPipelineMessage = new DevopsPipelineMessage();
        devopsPipelineMessage.setAppName(pipelineTask.getApplicationName());
        devopsPipelineMessage.setEnvName(GlobalConfigurations.isReleaseMode() ? "生产" : "测试");
        devopsPipelineMessage.setUrl(f2cEndpoint);
        devopsPipelineMessage.setId(UUIDUtil.newUUID());
        List<BasicPipelineEvent> eventList = Lists.newArrayList();
        devopsPipelineMessage.setEvents(eventList);
        long deployTime = 0L;
        if (pipelineTask.applicationDeployment != null) {
            try {
                BasicPipelineEvent pipelineEvent = new BasicPipelineEvent(PipelineEventType.CMP_DEPLOY);
                pipelineEvent.setStartTime(DateFormatUtils.format(pipelineTask.applicationDeployment.getStartTime(), FORMATE));
                pipelineEvent.setEndTime(DateFormatUtils.format(pipelineTask.applicationDeployment.getEndTime(), FORMATE));
                pipelineEvent.setResult(pipelineTask.applicationDeployment.getStatus());
                deployTime = pipelineTask.applicationDeployment.getEndTime() - pipelineTask.applicationDeployment.getStartTime();
                pipelineEvent.setUrl(f2cEndpoint);
                Cluster cluster = ApplicationPipelineSevice.this.clusterService.selectClusterById(pipelineTask.applicationDeployment.getClusterId());
                ClusterRole clusterRole = ApplicationPipelineSevice.this.clusterRoleService.getClusterRole(pipelineTask.applicationDeployment.getClusterRoleId());
                DevopsCloudServer devopsCloudServer = ApplicationPipelineSevice.this.devopsCloudServerMapper.selectByPrimaryKey(pipelineTask.applicationDeployment.getCloudServerId());
                JSONObject data = new JSONObject().fluentPut("deployVersion", pipelineTask.pipelineContext.getApplicationVersion().getName()).fluentPut("artifactUrl", pipelineTask.pipelineContext.getApplicationVersion().getLocation()).fluentPut("deployTime", deployTime).fluentPut("user", pipelineTask.applicationDeployment.getUserId()).fluentPut("cluster", cluster != null ? cluster.getName() : "").fluentPut("hostGroup", clusterRole != null ? clusterRole.getName() : "").fluentPut("host", devopsCloudServer != null ? devopsCloudServer.getHost() : "").fluentPut("hostName", devopsCloudServer != null ? devopsCloudServer.getHostname() : "").fluentPut("hostManageIP", devopsCloudServer != null ? devopsCloudServer.getManagementIp() : "");
                pipelineEvent.setData(data);
                eventList.add(pipelineEvent);
            } catch (Exception e) {
                LogUtil.error("PipelineEventType.CMP_DEPLOY error", e);
            }
        }
        JSONObject actions = null;
        if (pipelineTask.history != null) {
            try {
                devopsPipelineMessage.setUseName(StringUtils.isBlank(pipelineTask.history.getTriggerUser()) ? "auto" : pipelineTask.history.getTriggerUser());
                devopsPipelineMessage.setCreateTime(DateFormatUtils.format(pipelineTask.history.getBuildTime(), FORMATE));
                BasicPipelineEvent buildEvent = new BasicPipelineEvent(PipelineEventType.JENKINS_BUILD);
                buildEvent.setStartTime(DateFormatUtils.format(pipelineTask.history.getBuildTime(), FORMATE));
                //构建持续时长 包含了部署的时长
                Long durationTime = pipelineTask.history.getDurationTime() != null ? pipelineTask.history.getDurationTime() : 0L;
                buildEvent.setEndTime(DateFormatUtils.format(pipelineTask.history.getBuildTime() + durationTime - deployTime, FORMATE));
                buildEvent.setResult(pipelineTask.history.getBuildStatus());
                buildEvent.setUrl(jenkinsAddress);
                JSONObject data = new JSONObject().fluentPut("jobName", pipelineTask.devopsJenkinsJob.getName())
                        .fluentPut("jobBuildNumber", Integer.valueOf(pipelineTask.buildNumber)).fluentPut("logUrl", pipelineTask.history.getUrl())
                        .fluentPut("jobType", pipelineTask.devopsJenkinsJob.getType()).fluentPut("buildTime", durationTime - deployTime);
                buildEvent.setData(data);
                eventList.add(buildEvent);
                if (StringUtils.isNotBlank(pipelineTask.history.getActions())) {
                    actions = JSON.parseObject(pipelineTask.history.getActions());
                    //代码信息
                    if (MapUtils.isNotEmpty(actions)) {
                        BasicPipelineEvent codeEvent = new BasicPipelineEvent(PipelineEventType.CODE);
                        codeEvent.setStartTime(DateFormatUtils.format(pipelineTask.history.getBuildTime(), FORMATE));
                        codeEvent.setEndTime(codeEvent.getStartTime());
                        codeEvent.setResult(pipelineTask.history.getBuildStatus());
                        JSONArray remoteUrls = actions.getJSONArray("remoteUrls");
                        JSONObject lastBuiltRevision = null;
                        JSONObject codeData = new JSONObject();
                        //actions 有不同的数据结构
                        if (actions.containsKey("actions")) {
                            JSONArray actionsInfo = actions.getJSONArray("actions");
                            for (int i = 0; i < actionsInfo.size(); i++) {
                                JSONObject actionsInfoJSONObject = actionsInfo.getJSONObject(i);
                                if (actionsInfoJSONObject.containsKey("lastBuiltRevision")) {
                                    lastBuiltRevision = actionsInfoJSONObject.getJSONObject("lastBuiltRevision");
                                }
                                if (actionsInfoJSONObject.containsKey("remoteUrls")) {
                                    remoteUrls = actionsInfoJSONObject.getJSONArray("remoteUrls");
                                }
                            }
                        } else {
                            lastBuiltRevision = actions.getJSONObject("lastBuiltRevision");
                        }
                        //SubversionSCM
                        String jobXml = pipelineTask.getDevopsJenkinsJob().getJobXml();
                        String type = StringUtils.contains(jobXml, "SubversionSCM") ? "SVN" : "GIT";
                        codeData.put("type", type);
                        codeData.put("platform", "gitlab");
                        codeEvent.setData(codeData);
                        if (CollectionUtils.isNotEmpty(remoteUrls)) {
                            codeEvent.setUrl(getGitHost(remoteUrls.get(0).toString()));
                            codeData.put("repo", remoteUrls.get(0).toString());
                            codeData.put("platform", codeEvent.getUrl().contains("gitea") ? "gitea" : "gitlab");
                        }
                        if (lastBuiltRevision != null) {
                            codeData.put("commitId", lastBuiltRevision.getString("SHA1"));
                            JSONArray branch = lastBuiltRevision.getJSONArray("branch");
                            if (CollectionUtils.isNotEmpty(branch)) {
                                //多分支流水线任务类型
                                if (StringUtils.equals(pipelineTask.devopsJenkinsJob.getType(), JenkinsConstants.JOB_TYPE_MULTIBRANCH)) {
                                    codeData.put("branch", StringUtils.substringAfterLast(pipelineTask.devopsJenkinsJob.getName(), "/"));
                                } else {
                                    codeData.put("branch", branch.getJSONObject(0).getString("name"));
                                }
                            }
                        }
                        eventList.add(codeEvent);
                    }
                }
            } catch (Exception e) {
                LogUtil.error("PipelineEventType.JENKINS_BUILD error", e);
            }

        }

        if (pipelineTask.apiTestLog != null) {
            try {
                BasicPipelineEvent pipelineEvent = new BasicPipelineEvent(PipelineEventType.API_TEST);
                pipelineEvent.setStartTime(DateFormatUtils.format(pipelineTask.apiTestLog.getStartTime(), FORMATE));
                pipelineEvent.setEndTime(pipelineTask.apiTestLog.getEndTime() != null ? DateFormatUtils.format(pipelineTask.apiTestLog.getEndTime(), FORMATE) : "");
                pipelineEvent.setResult(pipelineTask.apiTestLog.getStatus());
                if (pipelineTask.apiTest != null) {
                    String text = JSON.toJSONString(pipelineTask.apiTest, serializerFeatures);
                    pipelineEvent.setData(JSON.parseObject(text));
                    pipelineEvent.setUrl(StringUtils.substringBefore(pipelineTask.apiTest.getReportUrl(), "/product"));
                }
                eventList.add(pipelineEvent);
            } catch (Exception e) {
                LogUtil.error("PipelineEventType.API_TEST error", e);
            }
        }

        if (pipelineTask.devopsSonarqube != null) {
            try {
                BasicPipelineEvent pipelineEvent = new BasicPipelineEvent(PipelineEventType.SONARQUBE);
                pipelineEvent.setStartTime(DateFormatUtils.format(pipelineTask.devopsSonarqube.getCreateTime(), FORMATE));
                pipelineEvent.setEndTime(pipelineEvent.getStartTime());
                if (actions.containsKey("executingTimeMillis")) {
                    pipelineEvent.setEndTime(DateFormatUtils.format(pipelineTask.devopsSonarqube.getCreateTime() + actions.getLong("executingTimeMillis"), FORMATE));
                }
                pipelineEvent.setResult("success");
                pipelineEvent.setUrl(actions.getString("serverUrl"));
                String text = JSON.toJSONString(pipelineTask.devopsSonarqube, serializerFeatures);
                JSONObject data = JSON.parseObject(text);
                data.fluentRemove("id").fluentRemove("jobHistoryId").fluentRemove("createTime");
                pipelineEvent.setData(data);
                eventList.add(pipelineEvent);
            } catch (Exception e) {
                LogUtil.error("PipelineEventType.SONARQUBE error", e);
            }
        }

        if (pipelineTask.devopsUnitTest != null) {
            BasicPipelineEvent pipelineEvent = new BasicPipelineEvent(PipelineEventType.JENKINS_UNIT_TEST);
            pipelineEvent.setStartTime(DateFormatUtils.format(pipelineTask.devopsUnitTest.getCreateTime(), FORMATE));
            pipelineEvent.setEndTime(pipelineEvent.getStartTime());
            pipelineEvent.setResult("success");
            pipelineEvent.setUrl(jenkinsAddress);
            JSONObject data = (JSONObject) JSON.toJSON(pipelineTask.devopsUnitTest);
            data.fluentPut("jobName", pipelineTask.devopsJenkinsJob.getName())
                    .fluentPut("jobBuildNumber", Integer.valueOf(pipelineTask.getBuildNumber()))
                    .fluentRemove("id").fluentRemove("jobHistoryId").fluentRemove("createTime");
            pipelineEvent.setData(data);
            eventList.add(pipelineEvent);
        }
        eventList.sort(Comparator.comparing(BasicPipelineEvent::getEventType));
        return devopsPipelineMessage;
    }

    private static String getGitHost(String url) {
        try {
            URI uri = new URI(url);
            return url.replace(uri.getPath(), "");
        } catch (Exception e) {
        }

        return url;
    }

    @Data
    @Builder
    static class PipelineTask {

        private String jobId;

        private String name;

        private String buildNumber;

        private PipelineContext pipelineContext;

        private boolean autoDeploy;

        private boolean autoApiTest;

        private long startTime;

        private boolean finished;

        private long timeout;

        private String buildHistoryId;

        private String applicationName;

        private DevopsJenkinsJob devopsJenkinsJob;

        private DevopsJenkinsJobHistory history;

        private DevopsSonarqube devopsSonarqube;

        private DevopsUnitTest devopsUnitTest;

        private ApplicationDeployment applicationDeployment;

        private ApplicationDeploymentEventLog apiTestLog;

        private DevopsApiTest apiTest;

        @Override
        public String toString() {
            return "PipelineTask{" +
                    "jobId='" + jobId + '\'' +
                    ", name='" + name + '\'' +
                    ", buildNumber='" + buildNumber + '\'' +
                    ", applicationName='" + applicationName + '\'' +
                    '}';
        }
    }
}
