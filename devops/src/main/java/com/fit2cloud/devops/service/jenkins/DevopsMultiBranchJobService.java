package com.fit2cloud.devops.service.jenkins;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.commons.server.exception.F2CException;
import com.fit2cloud.commons.server.utils.SessionUtils;
import com.fit2cloud.commons.utils.BeanUtils;
import com.fit2cloud.commons.utils.LogUtil;
import com.fit2cloud.commons.utils.UUIDUtil;
import com.fit2cloud.devops.base.domain.DevopsJenkinsJob;
import com.fit2cloud.devops.base.domain.DevopsJenkinsJobHistory;
import com.fit2cloud.devops.base.domain.DevopsJenkinsJobHistoryExample;
import com.fit2cloud.devops.base.mapper.DevopsJenkinsJobHistoryMapper;
import com.fit2cloud.devops.base.mapper.DevopsJenkinsJobMapper;
import com.fit2cloud.devops.common.consts.JenkinsConstants;
import com.fit2cloud.devops.common.util.RetryWhenErrorUtil;
import com.fit2cloud.devops.service.ApplicationPipelineSevice;
import com.fit2cloud.devops.service.jenkins.model.event.OperationType;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.offbytwo.jenkins.model.BuildResult;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author caiwzh
 * ?????????????????? 1. ?????????????????? 2.?????????????????? 3.?????????????????????????????? 4.??????????????????????????????
 * @date 2022/8/30
 */
@Service
public class DevopsMultiBranchJobService {

    /****  ??????????????????????????????url  ****/
    private static final String BUILD_URL = "/job/%s/build?delay=0sec";
    private static final String JOB_STATUS_URL = "/job/%s/wfapi/runs?since=%s&fullStages=true&_=%s";
    private static final String CHILD_JOB_CONSOLE_TEXT = "/job/%s/%s/consoleText";
    private static final String INDEXING_JOB_CONSOLE_TEXT = "/job/%s/indexing/consoleText";
    private static final String JOB_BUILD_HISTORY = "/job/%s/?tree=allBuilds[number[*],url[*],queueId[*]]";
    private static final String JOB_EXECUTORS_URL = "/manage/computer/%s/builds";
    /****  ??????????????????????????????url  ****/

    private static final int LONGVAR_MAX_LEN = 1024*4;
    @Resource
    private DevopsJenkinsJobHistoryMapper devopsJenkinsJobHistoryMapper;

    @Resource
    private DevopsJenkinsService devopsJenkinsService;

    @Resource
    private DevopsJenkinsJobMapper devopsJenkinsJobMapper;

    @Resource
    private ApplicationPipelineSevice applicationPipelineSevice;

    @Resource
    private DevopsJenkinsJobHistoryService devopsJenkinsJobHistoryService;

    @Resource
    private DevopsJenkinsSystemConfigService devopsJenkinsSystemConfigService;

    //jenkins ????????????????????????
    private Cache<String, List<String>> NODE_CACHE = CacheBuilder.newBuilder().expireAfterWrite(12, TimeUnit.HOURS)
            .maximumSize(128).build();

    private Map<String, ReentrantLock> syncFromJenkinsLock = new ConcurrentHashMap<>();

    private Map<String, BuildResult> statusMapping = ImmutableMap.<String, BuildResult>builder()
            .put("job-status-red", BuildResult.FAILURE)
            .put("job-status-blue-anime", BuildResult.BUILDING)
            .put("job-status-blue", BuildResult.SUCCESS)
            .build();

    /**
     * ??????????????????????????????
     *
     * @param id
     */
    public void buildWorkflowMultiBranchProject(String id) {
        DevopsJenkinsJob devopsJenkinsJob = devopsJenkinsJobMapper.selectByPrimaryKey(id);
        if (!StringUtils.equalsIgnoreCase(devopsJenkinsJob.getType(), JenkinsConstants.JOB_TYPE_MULTIBRANCH)) {
            return;
        }
        //????????????????????????????????????????????????Scan GitLab Project Log
        if (devopsJenkinsJob.getParentId() == null) {
            devopsJenkinsJob.setBuildStatus(BuildResult.BUILDING.name());
            devopsJenkinsJobMapper.updateByPrimaryKeySelective(devopsJenkinsJob);
            //eventListenerService.syncBranchAndMR(devopsJenkinsJob,false);
        } else {
            //?????????????????????
            buildWithNotDelay(devopsJenkinsJob);
        }
    }

    /**
     * ???????????????
     * ?????????????????????????????????????????????????????????????????????devops_jenkins_job+devops_jenkins_job_history???
     *
     * @param devopsJenkinsJobs
     */
    public void syncWorkflowMultiBranchJob(DevopsJenkinsJob... devopsJenkinsJobs) {
        //??????????????????
        for (DevopsJenkinsJob devopsJenkinsJob : devopsJenkinsJobs) {
            try {
                if (!StringUtils.equals(JenkinsConstants.JOB_TYPE_MULTIBRANCH, devopsJenkinsJob.getType())) {
                    continue;
                }
                //?????????????????????
                if (devopsJenkinsJob.getParentId() == null) {
                    syncBranchAndMR(devopsJenkinsJob, false, OperationType.BUILD);
                    devopsJenkinsJob.setBuildStatus(BuildResult.SUCCESS.name());
                } else {
                    //???????????????
                    DevopsJenkinsJobHistoryExample example = new DevopsJenkinsJobHistoryExample();
                    example.createCriteria()
                            .andJobIdEqualTo(devopsJenkinsJob.getId());
                    //.andOrderNumEqualTo(devopsJenkinsJob.getBuildSize());
                    List<DevopsJenkinsJobHistory> histories = devopsJenkinsJobHistoryMapper.selectByExample(example);
                    if (CollectionUtils.isNotEmpty(histories)) {
                        //???????????????????????????????????????
                        Map<Integer, DevopsJenkinsJobHistory> allRemoteHistory = getAllRemoteHistory(devopsJenkinsJob, OperationType.BUILD);
                        LogUtil.info(devopsJenkinsJob.getName() + "???????????????,??????jenkins?????????????????????" + allRemoteHistory);
                        for (DevopsJenkinsJobHistory buildHistory : histories) {
                            //1.????????????????????????????????? 2.???????????????????????????????????? ???????????????
                            if (buildHistory.getOrderNum().equals(devopsJenkinsJob.getBuildSize()) || !StringUtils.equalsAny(buildHistory.getBuildStatus(), BuildResult.SUCCESS.name(), BuildResult.FAILURE.name())) {
                                if (allRemoteHistory.containsKey(buildHistory.getOrderNum())) {
                                    DevopsJenkinsJobHistory remoteHistory = allRemoteHistory.get(buildHistory.getOrderNum());
                                    buildHistory.setIsBuilding(remoteHistory.getIsBuilding());
                                    buildHistory.setBuildStatus(remoteHistory.getBuildStatus());
                                    devopsJenkinsJob.setBuildStatus(remoteHistory.getBuildStatus());
                                    buildHistory.setSyncTime(System.currentTimeMillis());
                                    buildHistory.setName(remoteHistory.getName());
                                    buildHistory.setBuildTime(remoteHistory.getBuildTime());
                                    buildHistory.setDurationTime(remoteHistory.getDurationTime());
                                    buildHistory.setActions(remoteHistory.getActions());
                                    devopsJenkinsJobHistoryMapper.updateByPrimaryKeySelective(buildHistory);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                devopsJenkinsJob.setBuildStatus(BuildResult.FAILURE.name());
                LogUtil.warn("syncWorkflowMultiBranchJob  error", e);
            } finally {
                devopsJenkinsJobMapper.updateByPrimaryKeySelective(devopsJenkinsJob);
            }
        }
    }

    public void syncHistoryTriggerFromJenkinsV2() {
        List<DevopsJenkinsJob> devopsJenkinsJobs = getJenkinsTaskExectors();
        devopsJenkinsJobs.forEach(e -> {
            try {
                syncAllBranchHistory(e, OperationType.BUILD);
                //?????????????????????
                e.setBuildStatus(BuildResult.BUILDING.name());
                devopsJenkinsJobMapper.updateByPrimaryKey(e);
            } catch (Exception ex) {
            }
        });
    }

    public void syncHistoryTriggerFromJenkins(List<DevopsJenkinsJob> devopsJenkinsJobs) {
        devopsJenkinsJobs.forEach(e -> {
            try {
                if (checkTriggerFromJenkins(e)) {
                    syncAllBranchHistory(e, OperationType.BUILD);
                }
                //?????????????????????
                if (checkBranchHistroyStatus(e)) {
                    e.setBuildStatus(BuildResult.BUILDING.name());
                    devopsJenkinsJobMapper.updateByPrimaryKey(e);
                }
            } catch (Exception ex) {
            }
        });
    }

    /**
     * ????????????jenkins???????????? ????????????????????????
     * ?????????jenkins???????????????????????????id > ???????????????????????????
     *
     * @param devopsJenkinsJob
     * @return
     * @throws Exception
     */
    private boolean checkTriggerFromJenkins(DevopsJenkinsJob devopsJenkinsJob) {
        try {
            OptionalInt maxId = getMaxId(devopsJenkinsJob);
            if (maxId.isPresent() && maxId.getAsInt() > devopsJenkinsJob.getBuildSize()) {
                LogUtil.info(String.format("jenkins?????????????????????%s???,????????????id???%s???", devopsJenkinsJob.getName(), maxId.getAsInt()));
                return true;
            }
        } catch (IOException e) {
        }
        return false;
    }

    private OptionalInt getMaxId(DevopsJenkinsJob devopsJenkinsJob) throws IOException {
        if (StringUtils.equalsIgnoreCase(devopsJenkinsJob.getType(), JenkinsConstants.JOB_TYPE_MULTIBRANCH)) {
            HttpResponse httpResponse = devopsJenkinsService.getJenkinsClient().post_form_with_result(String.format(JOB_STATUS_URL, devopsJenkinsJob.getName(), "", System.currentTimeMillis()), null, true);
            JSONArray jsonArray = JSON.parseArray(EntityUtils.toString(httpResponse.getEntity()));
            OptionalInt maxId = jsonArray.stream().mapToInt(e -> ((JSONObject) e).getInteger("id")).max();
            return maxId;
        } else {
            String history = devopsJenkinsService.getJenkinsClient().get(String.format(JOB_BUILD_HISTORY, devopsJenkinsJob.getName()));
            JSONArray allBuilds = JSON.parseObject(history).getJSONArray("allBuilds");
            OptionalInt maxId = allBuilds.stream().mapToInt(e -> ((JSONObject) e).getInteger("number")).max();
            return maxId;
        }
    }

    private boolean checkBranchHistroyStatus(DevopsJenkinsJob devopsJenkinsJob) {
        DevopsJenkinsJobHistoryExample example = new DevopsJenkinsJobHistoryExample();
        example.createCriteria().andJobIdEqualTo(devopsJenkinsJob.getId());
        List<DevopsJenkinsJobHistory> histories = devopsJenkinsJobHistoryMapper.selectByExample(example);
        return histories.stream().filter(e -> StringUtils.equals(e.getBuildStatus(), BuildResult.UNKNOWN.name())).findAny().isPresent();
    }

    /**
     * ??????????????????????????????????????? devops_jenkins_job_history
     */
    public void syncAllBranchHistory(DevopsJenkinsJob devopsJenkinsJob, OperationType type) {
        ReentrantLock lock = syncFromJenkinsLock.computeIfAbsent(devopsJenkinsJob.getName(), k -> new ReentrantLock());
        if (lock.tryLock()) {
            try {
                DevopsJenkinsJobHistoryExample example = new DevopsJenkinsJobHistoryExample();
                example.createCriteria().andJobIdEqualTo(devopsJenkinsJob.getId());
                List<DevopsJenkinsJobHistory> histories = devopsJenkinsJobHistoryMapper.selectByExample(example);

                Map<Integer, DevopsJenkinsJobHistory> localHistory = histories.stream().collect(Collectors.toMap(DevopsJenkinsJobHistory::getOrderNum, e -> e, (k1, k2) -> k1));
                Map<Integer, DevopsJenkinsJobHistory> remoteHistory = getAllRemoteHistory(devopsJenkinsJob, type);
                LogUtil.info("??????jenkins???????????????" + remoteHistory.size());
                if (MapUtils.isNotEmpty(remoteHistory)) {
                    remoteHistory.forEach((k, v) -> {
                        try {
                            DevopsJenkinsJobHistory jobHistory = localHistory.remove(k);
                            if (jobHistory == null) {
                                if (type == OperationType.BUILD) {
                                    applicationPipelineSevice.bindJobHistory(v);
                                }
                                devopsJenkinsJobHistoryMapper.insert(v);
                            } else {
                                String id = jobHistory.getId();
                                BeanUtils.copyBean(jobHistory, v);
                                jobHistory.setId(id);
                                //if (jobHistory.getActions() != null && jobHistory.getActions().length() > LONGVAR_MAX_LEN) {
                                //    LogUtil.error(String.format("jobHistory Actions over max limit:%s, clean it", String.valueOf(jobHistory.getActions().length())));
                                //    jobHistory.setActions("");
                                //}
                                devopsJenkinsJobHistoryMapper.updateByPrimaryKeySelective(jobHistory);
                            }
                        } catch (Exception e) {
                            LogUtil.error("DevopsMultiBranchJobService???{}??? devopsJenkinsJobHistoryMapper error", devopsJenkinsJob.getName(), e);
                        }
                    });
                    //????????????????????????
                    if (type == OperationType.SYNC) {
                        localHistory.values().forEach(e -> devopsJenkinsJobHistoryMapper.deleteByPrimaryKey(e.getId()));
                    }
                    devopsJenkinsJob.setBuildSize(remoteHistory.keySet().stream().max(Comparator.naturalOrder()).get());
                }
                devopsJenkinsJob.setSyncStatus(JenkinsConstants.SyncStatus.END_SYNC.name());
                devopsJenkinsJobMapper.updateByPrimaryKey(devopsJenkinsJob);
            } catch (Exception e) {
                LogUtil.error("syncAllBranchHistory error", e);
            } finally {
                lock.unlock();
            }
        }

    }

    /**
     * ??????jenkins????????????
     * JOB_STATUS_URL ??????????????????10??????????????????
     *
     * @param devopsJenkinsJob
     * @param type
     * @return
     */
    private Map<Integer, DevopsJenkinsJobHistory> getLastRemoteHistory(DevopsJenkinsJob devopsJenkinsJob, OperationType type) {
        Map<Integer, DevopsJenkinsJobHistory> map = Maps.newHashMap();
        try {
            //??????20???
            JSONArray jobInfo = RetryWhenErrorUtil.execute(20, 1, () -> {
                        HttpResponse httpResponse = devopsJenkinsService.getJenkinsClient().post_form_with_result(String.format(JOB_STATUS_URL, devopsJenkinsJob.getName(), "", System.currentTimeMillis()), null, true);
                        JSONArray maxIdJobInfo = JSON.parseArray(EntityUtils.toString(httpResponse.getEntity()));
                        if (CollectionUtils.isEmpty(maxIdJobInfo)) {
                            throw new RuntimeException(devopsJenkinsJob.getName() + " getRemoteHistory result is empty,Retry !!!");
                        }
                        //???????????????job jenkins???????????????
                        if (type == OperationType.ADD || type == OperationType.UPDATE) {
                            if (maxIdJobInfo.size() == devopsJenkinsJob.getBuildSize()) {
                                throw new RuntimeException(devopsJenkinsJob.getName() + " OperationType.ADD or OperationType.UPDATE Retry !!!");
                            }
                        }
                        return maxIdJobInfo;
                    }
            );
            long time = System.currentTimeMillis();
            for (int i = 0; i < jobInfo.size(); i++) {
                JSONObject jsonObject = jobInfo.getJSONObject(i);
                DevopsJenkinsJobHistory buildHistory = new DevopsJenkinsJobHistory();
                buildHistory.setId(UUIDUtil.newUUID());
                buildHistory.setJobName(devopsJenkinsJob.getName());
                buildHistory.setJobId(devopsJenkinsJob.getId());
                buildHistory.setName(jsonObject.getString("name"));
                buildHistory.setOrderNum(jsonObject.getInteger("id"));
                buildHistory.setUrl(devopsJenkinsJob.getUrl() + "/" + jsonObject.getInteger("id"));
                buildHistory.setBuildTime(jsonObject.getLong("startTimeMillis"));
                buildHistory.setDurationTime(jsonObject.getLong("durationMillis"));
                String status = jsonObject.getString("status");
                if (StringUtils.equals("IN_PROGRESS", status)) {
                    buildHistory.setIsBuilding(true);
                } else {
                    buildHistory.setIsBuilding(false);
                }
                //buildHistory.setDescription("");
                buildHistory.setBuildStatus(StringUtils.equals("SUCCESS", status) ? BuildResult.SUCCESS.name() : StringUtils.equals("FAILED", status) ? BuildResult.FAILURE.name() : BuildResult.UNKNOWN.name());
                buildHistory.setSyncTime(time);
                //buildHistory.setTriggerUser(SessionUtils.getUser().getId());
                buildHistory.setSyncStatus(JenkinsConstants.SyncStatus.END_SYNC.name());
                buildHistory.setActions(jsonObject.toJSONString());
                map.put(buildHistory.getOrderNum(), buildHistory);
                devopsJenkinsJobHistoryService.saveSonarqubeMertics(buildHistory);
                devopsJenkinsJobHistoryService.saveTestReport(buildHistory);
            }
        } catch (Exception e) {
            LogUtil.warn("SyncWorkflowMultiBranchJob name[" + devopsJenkinsJob.getName() + "] get RemoteHistory   error", e);
        }
        return map;
    }

    private Map<Integer, DevopsJenkinsJobHistory> getAllRemoteHistory(DevopsJenkinsJob devopsJenkinsJob, OperationType type) {
        Map<Integer, DevopsJenkinsJobHistory> map = Maps.newHashMap();
        try {
            long time = System.currentTimeMillis();
            String history = devopsJenkinsService.getJenkinsClient().get(String.format(JOB_BUILD_HISTORY, devopsJenkinsJob.getName()));
            JSONArray allBuilds = JSON.parseObject(history).getJSONArray("allBuilds");
            //????????????????????????????????????????????????
            int size = (type == OperationType.BUILD && allBuilds.size() > 3) ? 3 : allBuilds.size();
            for (int i = 0; i < size; i++) {
                try {
                    JSONObject build = allBuilds.getJSONObject(i);
                    int number = build.getInteger("number");
                    String res = devopsJenkinsService.getJenkinsClient().get(build.getString("url"));
                    JSONObject jsonObject = JSON.parseObject(res);
                    DevopsJenkinsJobHistory buildHistory = new DevopsJenkinsJobHistory();
                    buildHistory.setId(UUIDUtil.newUUID());
                    buildHistory.setJobName(devopsJenkinsJob.getName());
                    buildHistory.setJobId(devopsJenkinsJob.getId());
                    buildHistory.setName(jsonObject.getString("displayName"));
                    buildHistory.setOrderNum(number);
                    buildHistory.setUrl(devopsJenkinsJob.getUrl() + "/" + number);
                    buildHistory.setBuildTime(jsonObject.getLong("timestamp"));
                    buildHistory.setDurationTime(jsonObject.getLong("duration"));
                    String status = jsonObject.getString("result");
                    if (StringUtils.equals("IN_PROGRESS", status)) {
                        buildHistory.setIsBuilding(true);
                    } else {
                        buildHistory.setIsBuilding(false);
                    }
                    //buildHistory.setDescription("");
                    try {
                        buildHistory.setBuildStatus(BuildResult.valueOf(status).name());
                    } catch (Exception e) {
                        buildHistory.setBuildStatus(BuildResult.BUILDING.name());
                    }
                    buildHistory.setSyncTime(time);
                    //buildHistory.setTriggerUser(SessionUtils.getUser().getId());
                    buildHistory.setSyncStatus(JenkinsConstants.SyncStatus.END_SYNC.name());
                    //fix:Data too long for column 'actions'
                    if (jsonObject.containsKey("changeSet")) {
                        jsonObject.remove("changeSet");
                    }
                    buildHistory.setActions(jsonObject.toJSONString());
                    map.put(buildHistory.getOrderNum(), buildHistory);
                    //devopsJenkinsJobHistoryService.saveSonarqubeMertics(buildHistory);
                    //devopsJenkinsJobHistoryService.saveTestReport(buildHistory);
                } catch (IOException e) {
                }
            }
        } catch (IOException e) {
            LogUtil.warn("SyncWorkflowMultiBranchJob name[" + devopsJenkinsJob.getName() + "] get getAllRemoteHistory error", e);
        }
        return map;
    }

    /**
     * ????????????
     *
     * @param devopsJenkinsJob
     */
    public void buildWithNotDelay(DevopsJenkinsJob devopsJenkinsJob) {
        String jobName = devopsJenkinsJob.getName();
        DevopsJenkinsJobHistory buildHistory = new DevopsJenkinsJobHistory();
        buildHistory.setJobId(devopsJenkinsJob.getId());
        if (SessionUtils.getUser() != null) {
            buildHistory.setTriggerUser(SessionUtils.getUser().getId());
        }
        buildHistory.setId(UUIDUtil.newUUID());
        buildHistory.setJobName(devopsJenkinsJob.getName());
        try {
            //????????????
            RetryWhenErrorUtil.execute(3, 1, () -> {
                devopsJenkinsService.getJenkinsClient().post(String.format(BUILD_URL, jobName), true);
                return 0;
            });
            HttpResponse httpResponse = devopsJenkinsService.getJenkinsClient().post_form_with_result(String.format(JOB_STATUS_URL, jobName, "", System.currentTimeMillis()), null, true);
            JSONArray jsonArray = JSON.parseArray(EntityUtils.toString(httpResponse.getEntity()));
            OptionalInt maxId = jsonArray.stream().mapToInt(e -> ((JSONObject) e).getInteger("id")).max();
            buildHistory.setBuildStatus(BuildResult.BUILDING.name());
            buildHistory.setOrderNum(maxId.getAsInt());
            buildHistory.setName("#" + buildHistory.getOrderNum());
            buildHistory.setSyncTime(System.currentTimeMillis());
            buildHistory.setIsBuilding(true);
            buildHistory.setUrl(devopsJenkinsJob.getUrl() + "/" + maxId.getAsInt());
        } catch (Exception e) {
            LogUtil.warn(String.format("??????[%s]?????????Error: %s", devopsJenkinsJob.getName(), e.getMessage()));
            buildHistory.setBuildStatus(BuildResult.FAILURE.name());
        } finally {
            applicationPipelineSevice.bindJobHistory(buildHistory);
            //??????job ??????
            afterBuild(devopsJenkinsJob, buildHistory);
        }
    }

    private void afterBuild(DevopsJenkinsJob devopsJenkinsJob, DevopsJenkinsJobHistory buildHistory) {
        LogUtil.warn(String.format("?????? %s ??????????????????", devopsJenkinsJob.getName()));
        DevopsJenkinsJobHistoryExample example = new DevopsJenkinsJobHistoryExample();
        example.createCriteria()
                .andJobIdEqualTo(devopsJenkinsJob.getId())
                .andOrderNumEqualTo(buildHistory.getOrderNum());
        List<DevopsJenkinsJobHistory> histories = devopsJenkinsJobHistoryMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(histories)) {
            LogUtil.warn(String.format("?????? %s ??????????????????????????????????????? ??????????????????????????????", devopsJenkinsJob.getName()));
            devopsJenkinsJobHistoryMapper.insert(buildHistory);
//                    ??????????????????
            DevopsJenkinsJob job = devopsJenkinsJobMapper.selectByPrimaryKey(devopsJenkinsJob.getId());
            DevopsJenkinsJob tmpJob = new DevopsJenkinsJob();
            tmpJob.setId(job.getId());
            tmpJob.setBuildSize(buildHistory.getOrderNum());
            devopsJenkinsJobMapper.updateByPrimaryKeySelective(tmpJob);
        } else {
            LogUtil.warn(String.format("?????? %s ?????????????????????????????????????????? ?????????????????????????????????????????????????????????????????????ID ???", devopsJenkinsJob.getName()));
            DevopsJenkinsJobHistory localHistory = histories.get(0);
            buildHistory.setId(localHistory.getId());
            devopsJenkinsJobHistoryMapper.updateByPrimaryKeySelective(buildHistory);
        }
        if (BuildResult.BUILDING.name().equalsIgnoreCase(buildHistory.getBuildStatus())) {
            LogUtil.warn(String.format("?????? %s ???????????????", devopsJenkinsJob.getName()));
            DevopsJenkinsJob job = new DevopsJenkinsJob();
            job.setId(devopsJenkinsJob.getId());
            job.setBuildStatus(BuildResult.BUILDING.name());
            devopsJenkinsJobMapper.updateByPrimaryKeySelective(job);
        } else if (BuildResult.FAILURE.name().equalsIgnoreCase(buildHistory.getBuildStatus())) {
            LogUtil.warn(String.format("?????? %s ????????????", devopsJenkinsJob.getName()));
            DevopsJenkinsJob job = new DevopsJenkinsJob();
            job.setId(devopsJenkinsJob.getId());
            job.setBuildStatus(BuildResult.FAILURE.name());
            devopsJenkinsJobMapper.updateByPrimaryKeySelective(job);
        }

    }

    /**
     * ???????????? + ????????????????????????
     *
     * @param devopsJenkinsJob
     * @param syncHistory
     */
    public void syncBranchAndMR(DevopsJenkinsJob devopsJenkinsJob, boolean syncHistory, OperationType type) {
        ReentrantLock lock = syncFromJenkinsLock.computeIfAbsent(devopsJenkinsJob.getId(), k -> new ReentrantLock());
        if (lock.tryLock()) {
            try {
                //????????????
                if (type == OperationType.ADD || type == OperationType.UPDATE) {
                    TimeUnit.SECONDS.sleep(3);
                }
                //branch
                Map<String, BuildResult> branchMap = null;
                try {
                    branchMap = RetryWhenErrorUtil.execute(3, 1, () -> {
                        String html = devopsJenkinsService.getJenkinsClient().getHtml("/job/" + devopsJenkinsJob.getName());
                        Map<String, BuildResult> buildResultMap = parseHtml(html);
                        if (MapUtils.isEmpty(buildResultMap)) {
                            throw new RuntimeException("Retry Get Branch");
                        }
                        return buildResultMap;
                    });
                    LogUtil.info(devopsJenkinsJob.getName() + "?????????????????????????????? branch??????" + branchMap);
                } catch (Exception e) {
                    LogUtil.warn(devopsJenkinsJob.getName() + "?????????????????????????????? branch????????????", e);
                }
                //mr
                Map<String, BuildResult> mrMap = null;
                try {
                    mrMap = RetryWhenErrorUtil.execute(3, 1, () -> {
                        String html = devopsJenkinsService.getJenkinsClient().getHtml("/job/" + devopsJenkinsJob.getName() + "/view/change-requests/");
                        Map<String, BuildResult> buildResultMap = parseHtml(html);
                        if (MapUtils.isEmpty(buildResultMap)) {
                            throw new RuntimeException("Retry Get MR");
                        }
                        return buildResultMap;
                    });
                    LogUtil.info(devopsJenkinsJob.getName() + "?????????????????????????????? MR??????" + mrMap);
                } catch (Exception e) {
                    LogUtil.warn(devopsJenkinsJob.getName() + "?????????????????????????????? MR????????????", e);
                }
                //????????????
                Example example = new Example(DevopsJenkinsJob.class);
                example.createCriteria().andEqualTo("parentId", devopsJenkinsJob.getId());
                List<DevopsJenkinsJob> jobList = devopsJenkinsJobMapper.selectByExample(example);
                Map<String, DevopsJenkinsJob> jobMap = jobList.stream().collect(Collectors.toMap(DevopsJenkinsJob::getName, e -> e));
                List<DevopsJenkinsJob> allJob = Lists.newArrayList();
                boolean flag = false;
                if (MapUtils.isNotEmpty(branchMap)) {
                    flag = true;
                    allJob.addAll(doSync(jobMap, branchMap, devopsJenkinsJob, (key) -> "job/" + key));
                }
                if (MapUtils.isNotEmpty(mrMap)) {
                    flag = true;
                    allJob.addAll(doSync(jobMap, mrMap, devopsJenkinsJob, (key) -> "view/change-requests/job/" + key));
                }
                //??????job
                jobMap.values().forEach(e -> devopsJenkinsJobMapper.deleteByPrimaryKey(e.getId()));
                //????????????????????????
                if (syncHistory && flag) {
                    allJob.forEach(e -> syncAllBranchHistory(e, type));
                }
                String jobxml = devopsJenkinsService.getJenkinsServer().getJobXml(devopsJenkinsJob.getName());
                devopsJenkinsJob.setJobXml(jobxml);
                devopsJenkinsJob.setSyncTime(System.currentTimeMillis());
                devopsJenkinsJob.setSyncStatus(JenkinsConstants.SyncStatus.END_SYNC.name());
            } catch (Exception e) {
                devopsJenkinsJob.setSyncStatus(JenkinsConstants.SyncStatus.ERROR_SYNC.name());
                LogUtil.error(devopsJenkinsJob.getName() + "????????????????????????????????????", e);
                F2CException.throwException(devopsJenkinsJob.getName() + "????????????????????????????????????" + e.getMessage());
            } finally {
                lock.unlock();
                devopsJenkinsJobMapper.updateByPrimaryKeySelective(devopsJenkinsJob);
            }
        }
    }

    private List<DevopsJenkinsJob> doSync(Map<String, DevopsJenkinsJob> jobMap, Map<String, BuildResult> map, DevopsJenkinsJob devopsJenkinsJob, Function<String, String> function) throws IOException {
        long time = System.currentTimeMillis();
        List<DevopsJenkinsJob> jobList = Lists.newArrayList();
        for (Map.Entry<String, BuildResult> entry : map.entrySet()) {
            String jobName = devopsJenkinsJob.getName() + "/" + function.apply(entry.getKey());
            DevopsJenkinsJob branchJob = new DevopsJenkinsJob();
            BeanUtils.copyBean(branchJob, devopsJenkinsJob);
            String jobxml = devopsJenkinsService.getJenkinsServer().getJobXml(jobName);
            DevopsJenkinsJob localJob = jobMap.remove(jobName);
            if (localJob != null) {
                localJob.setJobXml(jobxml);
                localJob.setSyncStatus(JenkinsConstants.SyncStatus.END_SYNC.name());
                localJob.setUpdateTime(time);
                localJob.setSyncTime(time);
                localJob.setBuildStatus(entry.getValue().name());
                jobList.add(localJob);
                devopsJenkinsJobMapper.updateByPrimaryKeySelective(branchJob);
            } else {
                branchJob.setId(UUIDUtil.newUUID());
                branchJob.setName(jobName);
                branchJob.setJobXml(jobxml);
                branchJob.setBuildable(true);
                branchJob.setUrl(devopsJenkinsJob.getUrl() + function.apply(entry.getKey()));
                branchJob.setSyncStatus(JenkinsConstants.SyncStatus.END_SYNC.name());
                branchJob.setSyncTime(time);
                branchJob.setCreateTime(time);
                branchJob.setUpdateTime(time);
                branchJob.setBuildStatus(entry.getValue().name());
                branchJob.setParentId(devopsJenkinsJob.getId());
                jobList.add(branchJob);
                devopsJenkinsJobMapper.insert(branchJob);
            }
        }
        return jobList;
    }

    /**
     * ??????html
     *
     * @param html
     * @return
     */
    private Map<String, BuildResult> parseHtml(String html) {
        Document document = Jsoup.parse(html);
        Map<String, BuildResult> result = Maps.newHashMap();
        Element projectstatus = document.getElementById("projectstatus");
        if (projectstatus == null) {
            return result;
        }
        Elements elementsByTag = projectstatus.select("tbody > tr");
        if (elementsByTag != null) {
            for (Element element : elementsByTag) {
                String id = element.attr("id");
                String status = element.attr("class");
                if (StringUtils.isNotBlank(id)) {
                    result.put(id.replace("job_", ""), statusMapping.getOrDefault(StringUtils.isNotBlank(status) ? status.trim() : status, BuildResult.BUILDING));
                }
            }
        }
        return result;
    }

    public String getChildOutputText(DevopsJenkinsJobHistory history) {
        try {
            return RetryWhenErrorUtil.execute(3, 1, () -> {
                String url = String.format(CHILD_JOB_CONSOLE_TEXT, history.getJobName(), history.getOrderNum());
                return devopsJenkinsService.getJenkinsClient().get(url);
            });
        } catch (Exception e) {
            LogUtil.error("getOutputText error", e);
        }
        return StringUtils.EMPTY;
    }

    public String getIndexOutputText(String id) {
        try {
            return RetryWhenErrorUtil.execute(3, 1, () -> {
                String url = String.format(INDEXING_JOB_CONSOLE_TEXT, id);
                return devopsJenkinsService.getJenkinsClient().get(url);
            });
        } catch (Exception e) {
            LogUtil.error("getOutputText error", e);
        }
        return StringUtils.EMPTY;
    }

    /**
     * ??????jenkins????????????????????????
     */
    public List<DevopsJenkinsJob> getJenkinsTaskExectors() {
        List<String> executorNodes = Lists.newArrayList();
        try {
            executorNodes.addAll(NODE_CACHE.get("nodes", () -> devopsJenkinsSystemConfigService.getExecutorNodes()));
        } catch (ExecutionException e) {
        }
        List<DevopsJenkinsJob> result = Lists.newArrayList();
        for (String executorNode : executorNodes) {
            try {
                String html = devopsJenkinsService.getJenkinsClient().getHtml(String.format(JOB_EXECUTORS_URL, executorNode));
                Document document = Jsoup.parse(html);
                Element executors = document.getElementById("executors");
                Elements trs = executors.select("tbody > tr");
                for (Element ele : trs) {
                    String text = ele.text();
                    if (StringUtils.isBlank(text) || StringUtils.containsAny(text, "??????", "Idle")) {
                        continue;
                    }
                    //5 mytest #40
                    String[] split = text.split(" ");
                    String jobName = split[1];
                    String buildNumber = split[2];
                    Example devopsJenkinsJobExample = new Example(DevopsJenkinsJob.class);
                    devopsJenkinsJobExample.createCriteria().andEqualTo("name", jobName);
                    List<DevopsJenkinsJob> jobList = devopsJenkinsJobMapper.selectByExample(devopsJenkinsJobExample);
                    if (CollectionUtils.isNotEmpty(jobList)) {
                        DevopsJenkinsJob jenkinsJob = jobList.get(0);
                        if (!StringUtils.equalsIgnoreCase(jenkinsJob.getBuildStatus(), BuildResult.BUILDING.name())) {
                            LogUtil.info(String.format("??????jenkins???????????????????????????%s???,????????????id???%s???", jobName, buildNumber));
                            result.add(jenkinsJob);
                        }
                    }
                }
            } catch (Exception e) {
                LogUtil.warn("getJenkinsTaskExectors err", e);
            }
        }
        return result;
    }
}
