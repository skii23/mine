package com.fit2cloud.devops.service.jenkins;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.commons.server.base.domain.*;
import com.fit2cloud.commons.server.base.mapper.OrganizationMapper;
import com.fit2cloud.commons.server.base.mapper.SystemParameterMapper;
import com.fit2cloud.commons.server.base.mapper.WorkspaceMapper;
import com.fit2cloud.commons.server.exception.F2CException;
import com.fit2cloud.commons.server.service.UserKeysService;
import com.fit2cloud.commons.server.utils.SessionUtils;
import com.fit2cloud.commons.utils.BeanUtils;
import com.fit2cloud.commons.utils.CommonThreadPool;
import com.fit2cloud.commons.utils.LogUtil;
import com.fit2cloud.commons.utils.UUIDUtil;
import com.fit2cloud.devops.base.domain.*;
import com.fit2cloud.devops.base.mapper.*;
import com.fit2cloud.devops.common.consts.EFileNodeType;
import com.fit2cloud.devops.common.consts.JenkinsConstants;
import com.fit2cloud.devops.common.consts.PathConst;
import com.fit2cloud.devops.common.util.CommonUtils;
import com.fit2cloud.devops.common.util.HtmlUtils;
import com.fit2cloud.devops.common.util.XmlUtils;
import com.fit2cloud.devops.dao.ext.ExtDevopsJenkinsJobHistoryMapper;
import com.fit2cloud.devops.dao.ext.ExtDevopsJenkinsJobMapper;
import com.fit2cloud.devops.dto.ApplicationDTO;
import com.fit2cloud.devops.dto.DevopsJenkinsJobDto;
import com.fit2cloud.devops.request.JobWorkspaceRequest;
import com.fit2cloud.devops.service.ApplicationPipelineSevice;
import com.fit2cloud.devops.service.jenkins.handler.*;
import com.fit2cloud.devops.service.jenkins.handler.obj2xml.WorkflowMultiBranchTransformer;
import com.fit2cloud.devops.service.jenkins.handler.xml2obj.WorkflowMultiBranchParser;
import com.fit2cloud.devops.service.jenkins.model.common.publisher.PublisherType;
import com.fit2cloud.devops.service.jenkins.model.common.reporters.MavenMailer;
import com.fit2cloud.devops.service.jenkins.model.event.DevopsJenkinsJobChangeEvent;
import com.fit2cloud.devops.service.jenkins.model.event.OperationType;
import com.fit2cloud.devops.service.jenkins.model.flow.WorkFlow;
import com.fit2cloud.devops.service.jenkins.model.freestyle.Project;
import com.fit2cloud.devops.service.jenkins.model.maven.MavenModuleSet;
import com.fit2cloud.devops.service.jenkins.model.multibranch.BranchSource;
import com.fit2cloud.devops.service.jenkins.model.multibranch.WorkflowMultiBranchProject;
import com.fit2cloud.devops.service.model.JenkinsJobSonarqubeParams;
import com.fit2cloud.devops.vo.FileNodeVO;
import com.google.common.collect.ImmutableMap;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.client.JenkinsHttpClient;
import com.offbytwo.jenkins.model.Plugin;
import com.offbytwo.jenkins.model.*;
import lombok.NonNull;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpResponseException;
import org.apache.http.message.BasicNameValuePair;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * DevopsJenkinsJobService
 *
 * @author makai
 */
@Service
public class DevopsJenkinsJobService {
    private static final String WORKSPACE_URL = "/job/${jobName}/ws";
    private static final String CLEAN_WORKSPACE_URL = "/job/${jobName}/doWipeOutWorkspace";
    private static final String DEFAULT_DIRECTORY_SELECTOR = "tbody>tr:has(td>img.icon-folder)>td:nth-child(2)>a:first-child";
    private static final String DEFAULT_FILE_SELECTOR = "tbody>tr:has(td>img.icon-text)>td:nth-child(2)>a";
    public static final String MULTIBRANCH_PUBLISHER = "f2CPublisher";
    public static final Pattern SECRET_TOKEN_PATTERN = Pattern.compile("<secretToken>(.*?)</secretToken>");

    @Resource
    private DevopsJenkinsJobMapper devopsJenkinsJobMapper;
    @Resource
    private ExtDevopsJenkinsJobMapper extDevopsJenkinsJobMapper;
    @Resource
    private DevopsJenkinsJobHistoryService devopsJenkinsJobHistoryService;
    @Resource
    private OrganizationMapper organizationMapper;
    @Resource
    private WorkspaceMapper workspaceMapper;
    @Resource
    private DevopsJenkinsService devopsJenkinsService;
    @Resource
    private DevopsJenkinsParamsMapper devopsJenkinsParamsMapper;
    @Resource
    private SystemParameterMapper systemParameterMapper;
    @Resource(name = "devopsJobPool")
    private CommonThreadPool commonThreadPool;
    @Resource
    private ApplicationMapper applicationMapper;
    @Resource
    private ApplicationVersionMapper applicationVersionMapper;
    @Resource
    private DevopsJenkinsJobHistoryMapper devopsJenkinsJobHistoryMapper;
    @Resource
    private UserKeysService userKeysService;
    @Resource
    private ApplicationContext applicationContext;
    @Resource
    private DevopsMultiBranchJobService devopsMultiBranchJobService;
    @Resource
    private ExtDevopsJenkinsJobHistoryMapper extDevopsJenkinsJobHistoryMapper;
    @Resource
    private ApplicationPipelineSevice applicationPipelineSevice;

    @PostConstruct
    public void init() {
        try {
            devopsJenkinsService.getJenkinsClient().get("credentials/store/system/domain/_/credential/ST/api/json");
        } catch (Exception e) {
            if (e instanceof HttpResponseException) {
                HttpResponseException httpResponseException = (HttpResponseException) e;
                if (httpResponseException.getStatusCode() == 404) {
                    List<NameValuePair> data = new ArrayList<>();
                    String ulr = "/credentials/store/system/domain/_/createCredentials";
                    String form = "{\"\": \"2\", \"credentials\": {\"scope\": \"GLOBAL\", \"apiToken\": \"444\", \"$redact\": \"apiToken\", \"id\": \"ST\", \"description\": \"??????????????????Secret token\", \"stapler-class\": \"com.dabsquared.gitlabjenkins.connection.GitLabApiTokenImpl\", \"$class\": \"com.dabsquared.gitlabjenkins.connection.GitLabApiTokenImpl\"}, \"Jenkins-Crumb\": \"5896668f73e7c9e4a44c15c859f907e3ee3fe2ffc1f04c7c8f7d16a5b128d33c\"}";
                    data.add(new BasicNameValuePair("json", form));
                    try {
                        devopsJenkinsService.getJenkinsClient().post_form_with_result(ulr, data, true);
                    } catch (IOException ex) {
                    }
                }
            }
        }
    }

    /**
     * Secret token ??????
     */
    public String encryptV2(String secretToken) {
        try {
            List<NameValuePair> data = new ArrayList<>();
            String updateSubmitUrl = "/credentials/store/system/domain/_/credential/ST/updateSubmit";
            String form = "{\"stapler-class\": \"com.dabsquared.gitlabjenkins.connection.GitLabApiTokenImpl\", \"scope\": \"GLOBAL\", \"apiToken\": \"22222\", \"$redact\": \"apiToken\", \"id\": \"ST\", \"description\": \"\", \"Jenkins-Crumb\": \"ffd44baa3d74d2617fe18819e75b4e98a172f14c59043170b9306bc18af442a0\"}";
            JSONObject json = JSON.parseObject(form);
            json.put("apiToken", secretToken);
            data.add(new BasicNameValuePair("json", json.toJSONString()));
            devopsJenkinsService.getJenkinsClient().post_form_with_result(updateSubmitUrl, data, true);
            String url = "/credentials/store/system/domain/_/credential/ST/update";
            String credentialHtml = devopsJenkinsService.getJenkinsClient().getHtml(url);
            org.jsoup.nodes.Document document = Jsoup.parse(credentialHtml);
            Elements e = document.select(".hidden-password > input[name=_.apiToken]");
            return e.attr("value");
        } catch (Exception e) {
        }
        return secretToken;
    }



    public List<DevopsJenkinsJobDto> listAllJenkinsJob(Map<String, Object> params) {
        CommonUtils.filterPermission(params);
        List<DevopsJenkinsJobDto> devopsJenkinsJobDtos = extDevopsJenkinsJobMapper.listDevopsJenkinsJob(params);
        for (DevopsJenkinsJobDto devopsJenkinsJobDto : devopsJenkinsJobDtos) {
            if (StringUtils.isBlank(devopsJenkinsJobDto.getParentId()) && StringUtils.equals(JenkinsConstants.JOB_TYPE_MULTIBRANCH, devopsJenkinsJobDto.getType())) {
                devopsJenkinsJobDto.setBuildSize(extDevopsJenkinsJobHistoryMapper.countHistoryByparentId(devopsJenkinsJobDto.getId()));
            }
        }
        return devopsJenkinsJobDtos;
    }

    public void syncAllJenkinsJobs() {
        SystemParameter syncStatusParam = systemParameterMapper.selectByPrimaryKey(JenkinsConstants.JENKINS_SYNC_STATUS_PARAM);
//        ??????????????????
        if (StringUtils.equalsIgnoreCase(syncStatusParam.getParamValue(), JenkinsConstants.SyncStatus.IN_SYNC.name())) {
            return;
        }

        try {
//        ?????????????????????
            syncStatusParam.setParamValue(JenkinsConstants.SyncStatus.IN_SYNC.name());
            int i = systemParameterMapper.updateByPrimaryKey(syncStatusParam);
            if (i == 0) {
                return;
            }
            Example example = new Example(DevopsJenkinsJob.class);
            example.createCriteria().andIsNull("parentId");
            List<DevopsJenkinsJob> devopsJenkinsJobs = devopsJenkinsJobMapper.selectByExample(example);
            JenkinsServer jenkinsServer = devopsJenkinsService.getJenkinsServer();
//        ?????????????????????job
            Map<String, DevopsJenkinsJob> currentJobMap = devopsJenkinsJobs.stream().collect(Collectors.toMap(DevopsJenkinsJob::getName, o -> o));
            Map<String, Job> remoteServerJobs = jenkinsServer.getJobs();

            remoteServerJobs.forEach((name, job) -> {
                DevopsJenkinsJob currentJob = currentJobMap.get(name);
//                ?????? try ???????????? finally???????????????
                if (currentJob != null && JenkinsConstants.SyncStatus.IN_SYNC.name().equalsIgnoreCase(currentJob.getSyncStatus())) {
                    currentJobMap.remove(name);
                    return;
                }
                try {
                    JobWithDetails details = job.details();
                    String jobXml = jenkinsServer.getJobXml(name);

                    if (currentJob == null) {
//                      ??????????????????????????????????????????
//                      ????????????????????????????????????
                        DevopsJenkinsJob newJob = setupDevopsJenkinsJob(details);
                        newJob.setJobXml(jobXml);
                        newJob.setType(getJobType(newJob.getJobXml()));
                        devopsJenkinsJobMapper.insert(newJob);
                        //jenkins?????????????????????????????????job????????????
                        if(StringUtils.equals(JenkinsConstants.JOB_TYPE_MULTIBRANCH,newJob.getType())){
                            applicationContext.publishEvent(new DevopsJenkinsJobChangeEvent(newJob, OperationType.ADD));
                        }
                        return;
                    }

//                  ????????????????????????
                    currentJob.setSyncStatus(JenkinsConstants.SyncStatus.IN_SYNC.name());
                    devopsJenkinsJobMapper.updateByPrimaryKeySelective(currentJob);

                    currentJob.setBuildable(details.isBuildable());
                    currentJob.setSyncTime(System.currentTimeMillis());
                    currentJob.setDescription(details.getDescription());
                    currentJob.setJobXml(jobXml);
                    currentJob.setBuildSize(details.getAllBuilds().size());
                    currentJob.setUrl(details.getUrl());
                    currentJob.setType(getJobType(jobXml));
                    // ??????jobxml????????????????????????????????????
                    if (jobXml.contains("parameterDefinitions")) {
                        currentJob.setParameterizedBuild(true);
                    } else {
                        currentJob.setParameterizedBuild(false);
                    }

                    if (details.getLastBuild() != null) {
                        currentJob.setBuildStatus(details.getLastBuild().details().getResult().name());
                    }
                    currentJob.setSyncStatus(JenkinsConstants.SyncStatus.END_SYNC.name());
                    devopsJenkinsJobMapper.updateByPrimaryKeySelective(currentJob);
                } catch (Exception e) {
                    Optional.ofNullable(currentJob).ifPresent(tmpJob -> {
                        tmpJob.setSyncStatus(JenkinsConstants.SyncStatus.ERROR_SYNC.name());
                        devopsJenkinsJobMapper.updateByPrimaryKeySelective(tmpJob);
                    });
                    LogUtil.error(String.format("??????????????????[%s]??????,Error: %s", name, e.getMessage()));
                    e.printStackTrace();
                } finally {
//                    ?????????????????????????????? job ??????????????????
                    if (currentJob != null && JenkinsConstants.SyncStatus.IN_SYNC.name().equalsIgnoreCase(currentJob.getSyncStatus())) {
                        currentJob.setSyncStatus(JenkinsConstants.SyncStatus.ERROR_SYNC.name());
                        devopsJenkinsJobMapper.updateByPrimaryKeySelective(currentJob);
                    }
                    currentJobMap.remove(name);
                }
            });
//            ?????????????????????????????????????????????????????????????????????????????????????????????????????????job
            currentJobMap.forEach((name, job) -> deleteLocalJenkinsJob(job));
//            ??????????????????
            List<DevopsJenkinsJob> newJobs = devopsJenkinsJobMapper.selectByExample(example);
            commonThreadPool.addTask(() -> newJobs.forEach(job -> devopsJenkinsJobHistoryService.syncJobHistory(job)));
//             ????????????
            syncStatusParam.setParamValue(JenkinsConstants.SyncStatus.END_SYNC.name());
        } catch (Exception e) {
            syncStatusParam.setParamValue(JenkinsConstants.SyncStatus.ERROR_SYNC.name());
            LogUtil.error(String.format("???????????????????????????Error: %s", e.getMessage()));
        } finally {
//            ????????????
            if (JenkinsConstants.SyncStatus.IN_SYNC.name().equalsIgnoreCase(syncStatusParam.getParamValue())) {
                syncStatusParam.setParamValue(JenkinsConstants.SyncStatus.ERROR_SYNC.name());
            }
            systemParameterMapper.updateByPrimaryKeySelective(syncStatusParam);
        }
    }

    public DevopsJenkinsJob setupDevopsJenkinsJob(JobWithDetails jobWithDetails) {
        DevopsJenkinsJob devopsJenkinsJob = new DevopsJenkinsJob();
        try {
            devopsJenkinsJob.setUpdateTime(System.currentTimeMillis());
            devopsJenkinsJob.setId(UUIDUtil.newUUID());
            devopsJenkinsJob.setCreateTime(System.currentTimeMillis());
            devopsJenkinsJob.setSyncStatus(JenkinsConstants.SyncStatus.NO_SYNC.name());
            devopsJenkinsJob.setSource("sync");
            devopsJenkinsJob.setUrl(jobWithDetails.getUrl());
            devopsJenkinsJob.setDescription(jobWithDetails.getDescription());
            devopsJenkinsJob.setBuildable(jobWithDetails.isBuildable());
            devopsJenkinsJob.setSyncTime(System.currentTimeMillis());
            devopsJenkinsJob.setName(jobWithDetails.getName());
            devopsJenkinsJob.setBuildSize(jobWithDetails.getAllBuilds().size());
            if (jobWithDetails.getLastBuild() != null) {
                devopsJenkinsJob.setBuildStatus(jobWithDetails.getLastBuild().details().getResult().name());
            }
        } catch (Exception e) {
            LogUtil.error(String.format("?????????????????????[%s]??????,Error: %s", jobWithDetails.getName(), e.getMessage()));
            devopsJenkinsJob.setSyncStatus(JenkinsConstants.SyncStatus.ERROR_SYNC.name());
            e.printStackTrace();
        }
        return devopsJenkinsJob;
    }

    public void syncJobBuildStatus(List<DevopsJenkinsJob> devopsJenkinsJobs) {
        if (CollectionUtils.isNotEmpty(devopsJenkinsJobs)) {
            JenkinsServer jenkinsServer = devopsJenkinsService.getJenkinsServer();
            DevopsJenkinsJob tmpJob = new DevopsJenkinsJob();
            devopsJenkinsJobs.forEach(job -> {
                try {
                    if (StringUtils.equals(JenkinsConstants.JOB_TYPE_MULTIBRANCH, job.getType())) {
                        devopsMultiBranchJobService.syncWorkflowMultiBranchJob(job);
                        return;
                    } else {
                        tmpJob.setId(job.getId());
                        JobWithDetails jobWithDetails = jenkinsServer.getJob(job.getName());
                        if (jobWithDetails.getLastBuild() != null) {
                            //BUG: ?????????jobWithDetails.getLastBuild().details().getResult()????????????SUCCESS ???????????????????????????
                            //   BuildResult result = jobWithDetails.getLastBuild().details().getResult();
                            if (jobWithDetails.getLastBuild().details().isBuilding()) {
                                tmpJob.setBuildStatus(BuildResult.BUILDING.name());
                            } else {
                                tmpJob.setBuildStatus(jobWithDetails.getLastBuild().details().getResult().name());
                            }
                        } else {
//                        ????????? null ???????????????????????????????????? jenkins ?????????????????????????????????
//                        ????????????????????? job ?????????????????????
                            tmpJob.setBuildStatus(BuildResult.NOT_BUILT.name());
                        }
                    }
                } catch (Exception e) {
                    tmpJob.setBuildStatus(BuildResult.UNKNOWN.name());
                    LogUtil.error(String.format("??????????????????[%s]???????????????Error: %s", job.getName(), e.getMessage()));
                    e.printStackTrace();
                } finally {
//                    ????????????
                    devopsJenkinsJobMapper.updateByPrimaryKeySelective(tmpJob);
                }
            });
        }
    }

    /**
     * ????????????????????????
     */
    public void syncJobs(List<DevopsJenkinsJob> devopsJenkinsJobs) {
        if (CollectionUtils.isEmpty(devopsJenkinsJobs)) {
            F2CException.throwException("???????????????????????????????????????");
            return;
        }
        commonThreadPool.addTask(() -> {
            JenkinsServer jenkinsServer = devopsJenkinsService.getJenkinsServer();
            devopsJenkinsJobs.forEach(job -> {
                DevopsJenkinsJob devopsJenkinsJob = devopsJenkinsJobMapper.selectByPrimaryKey(job.getId());
                if (devopsJenkinsJob == null || JenkinsConstants.SyncStatus.IN_SYNC.name().equalsIgnoreCase(devopsJenkinsJob.getSyncStatus())) {
                    return;
                }
                devopsJenkinsJob.setSyncStatus(JenkinsConstants.SyncStatus.IN_SYNC.name());
                int i = devopsJenkinsJobMapper.updateByPrimaryKeySelective(devopsJenkinsJob);
                if (i == 0) {
                    return;
                }
                //?????????????????????
                if(StringUtils.equals(JenkinsConstants.JOB_TYPE_MULTIBRANCH,devopsJenkinsJob.getType())){
                    applicationContext.publishEvent(new DevopsJenkinsJobChangeEvent(job,OperationType.SYNC));
                    return;
                }

                try {
                    JobWithDetails jobWithDetails = jenkinsServer.getJob(devopsJenkinsJob.getName());
                    String jobXml = jenkinsServer.getJobXml(devopsJenkinsJob.getName());
                    devopsJenkinsJob.setJobXml(jobXml);
                    devopsJenkinsJob.setBuildSize(jobWithDetails.getAllBuilds().size());
                    devopsJenkinsJob.setDescription(jobWithDetails.getDescription());
                    devopsJenkinsJob.setSyncTime(System.currentTimeMillis());
                    devopsJenkinsJob.setBuildable(jobWithDetails.isBuildable());
                    devopsJenkinsJob.setType(getJobType(jobXml));

                    // ??????jobxml????????????????????????????????????
                    if (jobXml.contains("parameterDefinitions")) {
                        devopsJenkinsJob.setParameterizedBuild(true);
                    } else {
                        devopsJenkinsJob.setParameterizedBuild(false);
                    }

                    devopsJenkinsJob.setUrl(jobWithDetails.getUrl());
                    if (jobWithDetails.getLastBuild() != null) {
                        BuildResult result = jobWithDetails.getLastBuild().details().getResult();
                        devopsJenkinsJob.setBuildStatus(BuildResult.BUILDING.name());
                        Optional.ofNullable(result).ifPresent(res -> devopsJenkinsJob.setBuildStatus(res.name()));
                    }
                    //jenkins???????????????????????????????????????
                    if(StringUtils.equals(JenkinsConstants.JOB_TYPE_MULTIBRANCH,devopsJenkinsJob.getType())){
                        devopsJenkinsJob.setBuildStatus(BuildResult.SUCCESS.name());
                        devopsJenkinsJob.setBuildable(true);
                    }
                    devopsJenkinsJob.setSyncStatus(JenkinsConstants.SyncStatus.END_SYNC.name());
                    //devopsJenkinsJob.setExtParam("");
                } catch (Exception e) {
                    devopsJenkinsJob.setSyncStatus(JenkinsConstants.SyncStatus.ERROR_SYNC.name());
                    LogUtil.error(String.format("??????????????????[%s]??????,Error: %s", job.getName(), e.getMessage()));
                    e.printStackTrace();
                } finally {
                    devopsJenkinsJobMapper.updateByPrimaryKeySelective(devopsJenkinsJob);
                    devopsJenkinsJobHistoryService.syncJobHistory(devopsJenkinsJob);
                }
            });
        });
    }


    /**
     * ????????????DevopsJenkinsJob
     *
     * @param devopsJenkinsJobs ????????????
     */
    public void buildJobs(List<DevopsJenkinsJob> devopsJenkinsJobs) {
        for (DevopsJenkinsJob jenkinsJob : devopsJenkinsJobs) {
            if (BooleanUtils.toBoolean(jenkinsJob.getBuildable())) {
                commonThreadPool.addTask(() -> {
                    try {
                        buildJob(jenkinsJob);
                    } catch (Exception e) {
                        LogUtil.error(e.getMessage());
                        F2CException.throwException("????????????" + e.getMessage());
                    }
                });
            }
        }
    }

    /**
     * ???????????????DevopsJenkinsJob
     *
     * @param devopsJenkinsJob ?????????????????????
     */
    public void buildWithParametersJob(DevopsJenkinsJob devopsJenkinsJob) {
        if (devopsJenkinsJob == null || StringUtils.isBlank(devopsJenkinsJob.getName())) {
            F2CException.throwException("???????????????????????????");
            return;
        }

        // build params ????????????????????????hashmap ??????????????????
        if (devopsJenkinsJob.getParams() == null || devopsJenkinsJob.getParams().isEmpty()) {
            F2CException.throwException("???????????????????????????");
            return;
        }

//        LogUtil.info(String.format("?????????????????????[%s],????????????[%s]", devopsJenkinsJob.getName(), devopsJenkinsJob.getParams()));

        // ????????????????????????
//        HashMap<String, String> map = new HashMap<String, String>();
////        map.put("RUN_NAME","new-lgc#5");
////        map.put("RUN_NAME","http://10.25.103.5:21088/jenkins/job/new-lgc/7/");
////        map.put("RUN_NAME","jenkins/job/new-lgc/7/");
////        map.put("RUN_NAME","new-lgc/7/");
//        devopsJenkinsJob.setParams(map);

        JobWithDetails job = null;
        try {
            JenkinsServer jenkinsServer = devopsJenkinsService.getJenkinsServer();
            job = jenkinsServer.getJob(devopsJenkinsJob.getName());

            // ??????api http??????????????????uri?????????????????? ?????????????????????????????????????????????????????????????????? ???????????????#??????????????????
            HashMap<String, String> maps = devopsJenkinsJob.getParams();
            for (String key : maps.keySet()) {
                String value = maps.get(key);
                maps.put(key, URLEncoder.encode(value,"UTF-8"));
            }
            // ???????????????????????????
            QueueReference ref = job.build(maps, true);
            commonBuildStep(devopsJenkinsJob, ref);
        } catch (Exception e) {
            // ?????????????????????????????????????????????????????????
            devopsJenkinsJob.setBuildStatus(BuildResult.UNKNOWN.name());
            devopsJenkinsJobMapper.updateByPrimaryKeySelective(devopsJenkinsJob);
            LogUtil.error(String.format("???????????????????????????[%s]??????,????????????[%s],Error: %s", devopsJenkinsJob.getName(), devopsJenkinsJob.getParams(), e.getMessage()));
            e.printStackTrace();
        }

        return;
    }


    /**
     * ??????DevopsJenkinsJob
     *
     * @param devopsJenkinsJob ????????????
     */
    public void buildJob(DevopsJenkinsJob devopsJenkinsJob) {
        if (devopsJenkinsJob == null || StringUtils.isBlank(devopsJenkinsJob.getName())) {
            F2CException.throwException("???????????????????????????");
            return;
        }
        if(StringUtils.equals(JenkinsConstants.JOB_TYPE_MULTIBRANCH,devopsJenkinsJob.getType())){
            applicationContext.publishEvent(new DevopsJenkinsJobChangeEvent(devopsJenkinsJob,OperationType.BUILD));
            return;
        }
        JobWithDetails job = null;
        try {
            JenkinsServer jenkinsServer = devopsJenkinsService.getJenkinsServer();
            job = jenkinsServer.getJob(devopsJenkinsJob.getName());
//            ???????????????????????????
            QueueReference ref = job.build(true);
            commonBuildStep(devopsJenkinsJob, ref);
        } catch (Exception e) {
            try {
                assert job != null;
//                ?????????????????????????????????
                QueueReference ref = job.build(new HashMap<>(), true);
                commonBuildStep(devopsJenkinsJob, ref);
            } catch (Exception ex) {
                devopsJenkinsJob.setBuildStatus(BuildResult.UNKNOWN.name());
                devopsJenkinsJobMapper.updateByPrimaryKeySelective(devopsJenkinsJob);
                LogUtil.error(String.format("??????????????????[%s]??????,Error: %s", devopsJenkinsJob.getName(), e.getMessage()));
                ex.printStackTrace();
            }
        }
    }

    /**
     * ???????????????????????????
     * <p>
     * 1. ?????????????????? jenkins ??????????????????????????? QueueReference ???????????????????????????????????????????????? QueueItem ??? url???
     * ???????????? url ??????????????????????????????????????????
     * 2. ?????? jenkins ??????????????????????????????????????????????????????????????????????????????(pending)?????????????????????????????????????????????????????????
     * ???????????????????????????????????????????????????????????????????????????????????????????????????????????????(building)?????????????????????????????? Executable ????????????????????????
     * ?????????????????????????????????????????????????????????????????????????????????????????????(cancelled)?????????????????????????????????(pending)??????????????????????????????????????????????????? cancelled ???????????????
     * 3. ???????????????????????????????????????????????????????????????(aborted)??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     * 4. ????????????????????????????????????????????????????????????????????????????????????????????? url ???????????? 404?????????????????????????????????
     * ???????????????????????????????????? 404 ?????????????????????????????????????????????????????? pending ???????????????????????????????????? executable ????????? null ????????? cancelled ??? false???
     * ???????????????????????????????????????????????????????????????????????? building ?????????????????????(??????????????????????????????????????????????????????????????????????????????)??????????????????????????????????????? cronjob ??????
     * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????(??????????????? aborted)???????????????????????????????????????????????????
     *
     * @param devopsJenkinsJob ??????????????????
     * @param queueReference   ??????????????????????????????
     */
    private void commonBuildStep(DevopsJenkinsJob devopsJenkinsJob, QueueReference queueReference) {
        boolean hasTriggerBuild = false;
        DevopsJenkinsJobHistory buildHistory = new DevopsJenkinsJobHistory();
        try {
            JenkinsServer jenkinsServer = devopsJenkinsService.getJenkinsServer();
            buildHistory.setJobId(devopsJenkinsJob.getId());
            if (SessionUtils.getUser() != null) {
                buildHistory.setTriggerUser(SessionUtils.getUser().getId());
            }
            buildHistory.setId(UUIDUtil.newUUID());
            buildHistory.setJobName(devopsJenkinsJob.getName());

            QueueItem queueItem;
            AtomicInteger count = new AtomicInteger(20);
            do {
                queueItem = jenkinsServer.getQueueItem(queueReference);
                if (queueItem.isCancelled() || queueItem.getExecutable() != null) {
                    break;
                }
                Thread.sleep(TimeUnit.SECONDS.toMillis(1));
            } while (count.decrementAndGet() > 0);

//            ??????20?????????????????????????????????
            if (count.get() < 1) {
                commonThreadPool.addTask(() -> devopsJenkinsJobHistoryService.syncJobHistory(devopsJenkinsJob));
                return;
            }

            if (!queueItem.isCancelled()) {
                Build build = jenkinsServer.getBuild(queueItem);
                BuildWithDetails buildDetail = build.details();
                //BUG: ?????????buildDetail.getResult()????????????SUCCESS ???????????????????????????
                //buildHistory.setBuildStatus(buildDetail.getResult() == null ? BuildResult.BUILDING.name() : buildDetail.getResult().name());
                buildHistory.setBuildStatus(buildDetail.isBuilding() ? BuildResult.BUILDING.name() : buildDetail.getResult().name());
                if (buildDetail.isBuilding()) {
                    LogUtil.warn(String.format("?????? %s ???????????????", devopsJenkinsJob.getName()));
                } else {
                    LogUtil.warn(String.format("?????? %s ???????????????????????? ?????????????????? %s", devopsJenkinsJob.getName(), buildDetail.getResult().name()));
                }
                buildHistory.setOrderNum(build.getNumber());
                buildHistory.setSyncTime(System.currentTimeMillis());
                buildHistory.setIsBuilding(buildDetail.isBuilding());
                buildHistory.setName(buildDetail.getDisplayName());
                buildHistory.setBuildTime(buildDetail.getTimestamp());
                buildHistory.setUrl(build.getUrl());
                buildHistory.setDescription(buildDetail.getDescription());

                //Add: ?????????json??????????????? ?????????
                List<Map<String, Object>> acts = (List<Map<String, Object>>) buildDetail.getActions();
                Map<String, Object> newMap = new HashMap<>();
                for (Map<String, Object> act : acts) {
                    if (!act.isEmpty()) {
                        newMap.putAll(act);
                    }
                }
                JSONObject actions = new JSONObject(newMap);
                buildHistory.setActions(actions.toJSONString());

                hasTriggerBuild = true;
            }

        } catch (InterruptedException e) {
            LogUtil.error("???????????????", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            LogUtil.debug(String.format("????????????[%s]?????????Error: %s", devopsJenkinsJob.getName(), e.getMessage()));
            e.printStackTrace();
//            ???????????????????????????????????????????????????
            commonThreadPool.addTask(() -> devopsJenkinsJobHistoryService.syncJobHistory(devopsJenkinsJob));
        } finally {
            if (hasTriggerBuild) {
                //??????pipeline
                applicationPipelineSevice.bindJobHistory(buildHistory);
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
                    tmpJob.setBuildSize(job.getBuildSize() + 1);
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
        }

    }

    /**
     * ??????????????????
     *
     * @return ??????????????????
     */
    public List<Organization> getAllOrganization() {
        if (!StringUtils.equalsIgnoreCase(SessionUtils.getUser().getParentRoleId(), "ADMIN")) {
            return Collections.emptyList();
        }
        return organizationMapper.selectByExample(null);
    }

    /**
     * ????????????ID????????????????????????
     *
     * @param organizationId ?????????
     * @return ???????????????????????????
     */
    public List<Workspace> getWorkspace(String organizationId) {
        if (StringUtils.isBlank(organizationId)) {
            return Collections.emptyList();
        }
        WorkspaceExample workspaceExample = new WorkspaceExample();
        workspaceExample.createCriteria().andOrganizationIdEqualTo(organizationId);
        return workspaceMapper.selectByExample(workspaceExample);
    }

    public List<Workspace> getWorkspace(String[] organizationIds) {
        List<Workspace> workspaceList = new ArrayList<>();
        for (String organizationId : organizationIds) {
            List<Workspace> workspaces = getWorkspace(organizationId);
            if (!workspaces.isEmpty()) {
                workspaceList.addAll(workspaces);
            }
        }
        return workspaceList;
    }

    /**
     * ??????????????????
     *
     * @param jenkinsJobs ??????????????? job
     */
    public void jobGrant(List<DevopsJenkinsJob> jenkinsJobs) {
        for (DevopsJenkinsJob job : jenkinsJobs) {
            devopsJenkinsJobMapper.updateByPrimaryKeySelective(job);
        }
    }

    /**
     * ?????????devops??????jenkins
     *
     * @param jenkinsJob ???????????? job
     */
    public void deleteDevJenkinsJob(DevopsJenkinsJob jenkinsJob) {
        if (devopsJenkinsJobMapper.deleteByPrimaryKey(jenkinsJob.getId()) != 0) {
            devopsJenkinsJobHistoryService.deleteJobHistory(jenkinsJob.getId());
        }
    }

    /**
     * ????????????JenkinsJob?????????jenkins????????????
     *
     * @param jenkinsJobs ???????????????job
     */
    public void deleteJenkinsJob(List<DevopsJenkinsJob> jenkinsJobs) {
        for (DevopsJenkinsJob jenkinsJob : jenkinsJobs) {
            deleteJenkinsJob(jenkinsJob);
        }
    }

    @Transactional(rollbackFor = F2CException.class)
    public void deleteJenkinsJob(DevopsJenkinsJob jenkinsJob) {
        JenkinsServer jenkinsServer = devopsJenkinsService.getJenkinsServer();
        if (devopsJenkinsJobMapper.deleteByPrimaryKey(jenkinsJob.getId()) != 0) {
            try {

                Job job = jenkinsServer.getJob(jenkinsJob.getName());

                if (job != null) {
                    //??????jenkins?????????????????? ?????????????????? 500 ????????????????????? ?????????????????????????????????????????????
                    //??????????????????????????????????????????????????????????????????????????? ???????????????????????????
//                    if(job.details().getBuilds().isEmpty() == false){
//                        jenkinsServer.disableJob(jenkinsJob.getName(), true);
//                    }

                    jenkinsServer.deleteJob(jenkinsJob.getName(), true);
                    //jenkinsServer.
                }
                //??????????????????????????????
                devopsJenkinsJobHistoryService.deleteJobHistory(jenkinsJob.getId());
                //???????????????
                Example example = new Example(DevopsJenkinsJob.class);
                example.createCriteria().andEqualTo("parentId",jenkinsJob.getId());
                devopsJenkinsJobMapper.deleteByExample(example);
            } catch (IOException e) {
                e.printStackTrace();
                LogUtil.error(String.format("???????????????????????? ???????????? %s ??????????????????", e.getMessage()));

                try {
                    jenkinsServer.deleteJob(jenkinsJob.getName(), true);
                } catch (IOException ex) {
                    LogUtil.error(String.format("?????????????????????????????? ???????????? %s ????????????", ex.getMessage()));
                    F2CException.throwException("??????????????????" + jenkinsJob.getName() + "??????");
                }
            }
        }
    }

    public void deleteLocalJenkinsJob(DevopsJenkinsJob jenkinsJob) {
        if (devopsJenkinsJobMapper.deleteByPrimaryKey(jenkinsJob.getId()) != 0) {
            devopsJenkinsJobHistoryService.deleteJobHistory(jenkinsJob.getId());
        }
    }

    public void saveJenkinsJob(JSONObject jsonObject) {
        //?????????????????????????????????????????????????????????????????????????????????????????????ak sk??????????????????????????? ???????????????????????????aksk?????? ??????????????????????????????ak??????????????????????????????????????????.
        String jobId = jsonObject.getString("id");
        if (StringUtils.isBlank(jobId)) {
            createJob(jsonObject);
        } else {
            updateJob(jsonObject, jobId);
        }
    }

    public void updateJob(JSONObject jsonObject, String jobId) {
        JenkinsServer jenkinsServer = devopsJenkinsService.getJenkinsServer();
        DevopsJenkinsJob devopsJenkinsJob = devopsJenkinsJobMapper.selectByPrimaryKey(jobId);
        //??????
        DevopsJenkinsJob devopsJenkinsJobRel = new DevopsJenkinsJob();
        BeanUtils.copyBean(devopsJenkinsJobRel,devopsJenkinsJob);
        String originJobXml = devopsJenkinsJob.getJobXml();
        String jobName = jsonObject.getString("name");
        LogUtil.info(String.format("jobName = [%s], json_string = %s", jobName, jsonObject.toJSONString()));

        if (!StringUtils.equals(jobName, devopsJenkinsJob.getName())) {
            // ??????????????????????????? TODO???????????????????????????????????? ??????????????????????????????
            try {
                jenkinsServer.renameJob(devopsJenkinsJob.getName(), jobName, true);
            } catch (IOException e) {
                LogUtil.error(String.format("?????????????????? %s %s %s", e.getMessage(), e.getLocalizedMessage(), e.getCause()));
                F2CException.throwException("??????????????????,????????????????????????????????????");
                return;
            }
        }
        devopsJenkinsJob.setName(jobName);
        devopsJenkinsJob.setUpdateTime(System.currentTimeMillis());
        devopsJenkinsJob.setDescription(jsonObject.getString("description"));


        //Add: gitlab?????? ????????????????????????
        handlerTriggers(jsonObject);

        //F2C_PUBLISHER ???????????????????????????????????????????????????ak????????????????????????????????????????????????????????????userKey.
        JSONArray publishers = jsonObject.containsKey("publishers") ? jsonObject.getJSONArray("publishers") : new JSONArray();
//      LogUtil.info(String.format("publishers %s %d" ,publishers,publishers.size()));

        //  ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        for (int i = 0; i < publishers.size(); i++) {
            JSONObject publisher = publishers.getJSONObject(i);


            // ??????????????????????????????1.0????????? ?????????????????????????????????xml??????
            if (publisher.containsKey("F2CCodeDeployPublisher")) {
                // ?????????????????????????????????????????????
                DevopsJenkinsParamsExample devopsJenkinsParamsExample = new DevopsJenkinsParamsExample();
                devopsJenkinsParamsExample.createCriteria().andParamKeyEqualTo(JenkinsConstants.F2C_ENDPOINT_PARAM);
                String f2cEndpoint = devopsJenkinsParamsMapper.selectByExample(devopsJenkinsParamsExample).get(0).getParamValue();

                // ?????????????????????userkey
                String userId = SessionUtils.getUser().getId();
                List<UserKey> userKeys = userKeysService.getUserKeysInfo(userId);
                UserKey userKey = null;
                if (userKeys.isEmpty()) {
                    userKey = userKeysService.generateUserKey(userId);
                } else {
                    userKey = userKeys.get(0);
                }
                publisher.getString("xmlNodeData").replaceAll("\\r\\n", "").trim();
                boolean isCurrentUserhandled = userKeys.stream().anyMatch(u -> u.getAccessKey().equals(publisher.getString("f2cAccessKey")));

                if (!isCurrentUserhandled) {
                    LogUtil.warn("????????????????????????????????????????????????ak sk ");
                    //???????????????????????????????????????????????????????????????????????????????????????ak sk
                    publisher.put("f2cAccessKey", userKey.getAccessKey());
                    publisher.put("f2cSecretKey", userKey.getSecretKey());
                }
                // ???????????????????????????????????????
                if (publisher.getString("f2cEndpoint") == null || !publisher.getString("f2cEndpoint").equalsIgnoreCase(f2cEndpoint)) {
                    publisher.put("f2cEndpoint", f2cEndpoint);
                }


            }
            // ????????????????????????2.0 com.fit2cloud.codedeploy2.F2CCodeDeploySouthPublisher ?????????
            else if (StringUtils.equalsIgnoreCase(publisher.getString("type"),"F2C_PUBLISHER")) {
                // ?????????????????????????????????????????????
                DevopsJenkinsParamsExample devopsJenkinsParamsExample = new DevopsJenkinsParamsExample();
                devopsJenkinsParamsExample.createCriteria().andParamKeyEqualTo(JenkinsConstants.F2C_ENDPOINT_PARAM);
                String f2cEndpoint = devopsJenkinsParamsMapper.selectByExample(devopsJenkinsParamsExample).get(0).getParamValue();

                // ?????????????????????userkey
                String userId = SessionUtils.getUser().getId();
                List<UserKey> userKeys = userKeysService.getUserKeysInfo(userId);
                UserKey userKey = null;
                if (userKeys.isEmpty()) {
                    userKey = userKeysService.generateUserKey(userId);
                } else {
                    userKey = userKeys.get(0);
                }

                boolean isCurrentUserhandled = userKeys.stream().anyMatch(u -> u.getAccessKey().equals(publisher.getString("f2cAccessKey")));

                if (!isCurrentUserhandled) {
                    LogUtil.warn("????????????????????????????????????????????????ak sk ");
                    //???????????????????????????????????????????????????????????????????????????????????????ak sk
                    publisher.put("f2cAccessKey", userKey.getAccessKey());
                    publisher.put("f2cSecretKey", userKey.getSecretKey());
                }
                // ???????????????????????????????????????
                if (publisher.getString("f2cEndpoint") == null || !publisher.getString("f2cEndpoint").equalsIgnoreCase(f2cEndpoint)) {
                    publisher.put("f2cEndpoint", f2cEndpoint);
                }

            }


        }
        jsonObject.put("publishers", publishers);

        if (StringUtils.isNotBlank(originJobXml)) {
            SAXBuilder saxBuilder = XmlUtils.getSaxBuilder();
            String jobXml;
            try {
                Document document = saxBuilder.build(new StringReader(originJobXml));
                switch (devopsJenkinsJob.getType()) {
                    case JenkinsConstants.JOB_TYPE_FREESTYLE: {
                        new FreestyleJobTransformer(jsonObject, document).convert();
                        break;
                    }
                    case JenkinsConstants.JOB_TYPE_MAVEN: {
                        new MavenJobTransformer(jsonObject, document).convert();
                        break;
                    }
                    case JenkinsConstants.JOB_TYPE_FLOW: {
                        new WorkFlowJobTransformer(jsonObject, document).convert();
                        break;
                    }
                    case JenkinsConstants.JOB_TYPE_MULTIBRANCH: {
                        WorkflowMultiBranchTransformer workflowMultiBranchTransformer = new WorkflowMultiBranchTransformer();
                        workflowMultiBranchTransformer.doConvert(jsonObject, document);
                        break;
                    }
                    default: {
                        return;
                    }
                }
                devopsJenkinsJob.setExtParam(jsonObject.toJSONString());
                jobXml = XmlUtils.outputXml(document.getRootElement());
                //jobName ??????????????????key ???????????????????????????????????????key????????????????????????
//              LogUtil.warn(String.format("????????????????????? jobxml = %s", jobXml));
                jenkinsServer.updateJob(jobName, jobXml, true);
                devopsJenkinsJob.setJobXml(jobXml);
                devopsJenkinsJob.setBuildable(!jsonObject.getBooleanValue("disabled"));
                devopsJenkinsJob.setParameterizedBuild(jsonObject.getBoolean("parameterizedBuild"));
                //????????????????????????????????????????????? ????????????
                if (StringUtils.equals(JenkinsConstants.JOB_TYPE_MULTIBRANCH, devopsJenkinsJob.getType())) {
                    devopsJenkinsJob.setSyncStatus(JenkinsConstants.SyncStatus.IN_SYNC.name());
                }
                devopsJenkinsJob.setAppId(this.getApplicationId(devopsJenkinsJob));
                devopsJenkinsJobMapper.updateByPrimaryKeySelective(devopsJenkinsJob);
                //TODO ??????????????????????????????job????????????
                applicationContext.publishEvent(new DevopsJenkinsJobChangeEvent(devopsJenkinsJob, OperationType.UPDATE));
            } catch (JDOMException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createJob(JSONObject jsonObject) {

        String jobName = jsonObject.getString("name");
        LogUtil.info(String.format("jobName = [%s], json_string = %s", jobName, jsonObject.toJSONString()));
        Example devopsJenkinsJobExample = new Example(DevopsJenkinsJob.class);
        devopsJenkinsJobExample.createCriteria().andEqualTo("name", jobName);
        List<DevopsJenkinsJob> devopsJenkinsJobs = devopsJenkinsJobMapper.selectByExample(devopsJenkinsJobExample);
        //???????????????
        if (CollectionUtils.isNotEmpty(devopsJenkinsJobs)) {
            return;
        }

        //Add: gitlab?????? ????????????????????????
        handlerTriggers(jsonObject);

        //F2C_PUBLISHER ??????????????????
        JSONArray publishers = jsonObject.containsKey("publishers") ? jsonObject.getJSONArray("publishers") : new JSONArray();
        for (int i = 0; i < publishers.size(); i++) {
            JSONObject publisher = publishers.getJSONObject(i);
//          LogUtil.warn(String.format("type is %s ", publisher.getString("type")));
//          ?????????????????????????????????type=F2C_PUBLISHER ?????????????????????????????????????????????
            if (StringUtils.equalsIgnoreCase(publisher.getString("type"),PublisherType.F2C_PUBLISHER.getJavaType())) {

                // ?????????????????????????????????????????????
                DevopsJenkinsParamsExample devopsJenkinsParamsExample = new DevopsJenkinsParamsExample();
                devopsJenkinsParamsExample.createCriteria().andParamKeyEqualTo(JenkinsConstants.F2C_ENDPOINT_PARAM);
                String f2cEndpoint = devopsJenkinsParamsMapper.selectByExample(devopsJenkinsParamsExample).get(0).getParamValue();

                // ?????????????????????userkey
                String userId = SessionUtils.getUser().getId();
                List<UserKey> userKeys = userKeysService.getUserKeysInfo(userId);
                UserKey userKey = null;
                if (userKeys.isEmpty()) {
                    userKey = userKeysService.generateUserKey(userId);
                } else {
                    userKey = userKeys.get(0);
                }

                publisher.put("f2cEndpoint", f2cEndpoint);
                publisher.put("f2cAccessKey", userKey.getAccessKey());
                publisher.put("f2cSecretKey", userKey.getSecretKey());
            }
        }
        jsonObject.put("publishers", publishers);

        // ??????????????????????????????????????????????????????
        DevopsJenkinsJob devopsJenkinsJob = getBaseDevopsJenkinsJob(jsonObject);
        JenkinsServer jenkinsServer = devopsJenkinsService.getJenkinsServer();
        devopsJenkinsJob.setName(jobName);
        String jobUrl = systemParameterMapper.selectByPrimaryKey(JenkinsConstants.JENKINS_ADDRESS_PARAM).getParamValue();
        if (jobUrl.endsWith(PathConst.URL_SEP)) {
            jobUrl = jobUrl + "job/" + jobName + PathConst.URL_SEP;
        } else {
            jobUrl = jobUrl + "/job/" + jobName + PathConst.URL_SEP;
        }
        devopsJenkinsJob.setUrl(jobUrl);
        SAXBuilder saxBuilder = XmlUtils.getSaxBuilder();
        String jobXml;
        Document document = null;
        String jobType = jsonObject.getString("type");
        switch (jobType) {
            case JenkinsConstants.JOB_TYPE_FREESTYLE: {
                Project project = new Project();
                String projectBaseXml = XmlUtils.toXml(project);
                try {
                    // ???????????????????????????XML??????
                    document = saxBuilder.build(new StringReader(projectBaseXml));
                    // ??????????????????????????????json?????? ???????????????XML??????
                    new FreestyleJobTransformer(jsonObject, document).convert();
                } catch (JDOMException | IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            case JenkinsConstants.JOB_TYPE_MAVEN: {
                MavenModuleSet mavenModuleSet = new MavenModuleSet();
                String baseMavenJobXml = XmlUtils.toXml(mavenModuleSet);
                try {
                    document = saxBuilder.build(new StringReader(baseMavenJobXml));
                    new MavenJobTransformer(jsonObject, document).convert();
                } catch (JDOMException | IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            case JenkinsConstants.JOB_TYPE_FLOW: {
                WorkFlow workFlow = new WorkFlow();
                String baseWorkFlowXml = XmlUtils.toXml(workFlow);
                try {
                    document = saxBuilder.build(new StringReader(baseWorkFlowXml));
                    new WorkFlowJobTransformer(jsonObject, document).convert();
                } catch (JDOMException | IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            case JenkinsConstants.JOB_TYPE_MULTIBRANCH: {
                WorkflowMultiBranchProject workflowMultiBranchProject = new WorkflowMultiBranchProject();
                String baseWorkFlowXml = XmlUtils.toXml(workflowMultiBranchProject);
                try {
                    document = saxBuilder.build(new StringReader(baseWorkFlowXml));
                    WorkflowMultiBranchTransformer workflowMultiBranchTransformer = new WorkflowMultiBranchTransformer();
                    workflowMultiBranchTransformer.doConvert(jsonObject, document);
                } catch (JDOMException | IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            default: {
                return;
            }
        }
        try {
            devopsJenkinsJob.setExtParam(jsonObject.toJSONString());
            assert document != null;
            jobXml = XmlUtils.outputXml(document.getRootElement());
            jenkinsServer.createJob(jobName, jobXml, true);
            devopsJenkinsJob.setJobXml(jobXml);
            devopsJenkinsJob.setType(jobType);
            devopsJenkinsJob.setBuildable(!jsonObject.getBooleanValue("disabled"));
            //jenkins???????????????????????????????????????
            if(StringUtils.equals(JenkinsConstants.JOB_TYPE_MULTIBRANCH,devopsJenkinsJob.getType())){
                devopsJenkinsJob.setBuildStatus(BuildResult.BUILDING.name());
                //devopsJenkinsJob.setBuildable(false);
            }
            devopsJenkinsJob.setAppId(this.getApplicationId(devopsJenkinsJob));
            devopsJenkinsJob.setParameterizedBuild(jsonObject.getBoolean("parameterizedBuild"));
            devopsJenkinsJobMapper.insert(devopsJenkinsJob);
            //??????job????????????
            applicationContext.publishEvent(new DevopsJenkinsJobChangeEvent(devopsJenkinsJob, OperationType.ADD));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????appId????????????
     * ??????????????????????????????"none"???appId?????????
     * @param devopsJenkinsJob
     * @return
     */
    private String getApplicationId(DevopsJenkinsJob devopsJenkinsJob) {
        JSONObject publish = this.devopsJenkinsService.getPublish(devopsJenkinsJob);
        if (publish != null) {
            String applicationId = publish.getString("applicationId");
            if (StringUtils.isNotBlank(applicationId)) {
                return applicationId;
            }
        }
        return "none";
    }

    private void handlerTriggers(JSONObject jsonObject) {
        JSONArray triggers = jsonObject.getJSONArray("triggers");
        if(CollectionUtils.isNotEmpty(triggers)){
            for (int i = 0; i < triggers.size(); i++) {
                JSONObject trigger = triggers.getJSONObject(i);
                // ??????????????????????????????1.0????????? ?????????????????????????????????xml??????
                if (StringUtils.equalsIgnoreCase(trigger.getString("type"),"GITLAB_PUSH_TRIGGER") && StringUtils.isNotBlank(trigger.getString("secretToken"))) {
                    // ???????????????token ??????????????????
                    String originalSecretToken = trigger.getString("secretToken");
                    if(originalSecretToken.length() <= 32){
                        String encryptedValue = encryptV2(originalSecretToken);
                        LogUtil.info(String.format("???????????? %s ?????????????????? %s", originalSecretToken, encryptedValue));
                        trigger.put("secretToken", encryptedValue);
                        trigger.put("originalSecretToken", originalSecretToken);
                    }
                }
            }
            // ??????
            jsonObject.put("triggers", triggers);
        }
    }

    private void handlerTriggers(Document document,DevopsJenkinsJob devopsJenkinsJob) {
        Element triggers = document.getRootElement().getChild("triggers");
        for (Element trigger : triggers.getChildren()) {
            if (trigger.toString().contains("GitLabPushTrigger")) {
                for (Element element : trigger.getChildren()) {
                    if (element.toString().contains("secretToken")) {
                        // ????????????token???????????????
                        String extParam = devopsJenkinsJob.getExtParam();
                        if (StringUtils.isNotBlank(extParam)) {
                            JSONObject extJson = JSON.parseObject(extParam);
                            JSONArray triggersArray = extJson.getJSONArray("triggers");
                            for (int i = 0; i < triggersArray.size(); i++) {
                                JSONObject triggerInfo = triggersArray.getJSONObject(i);
                                if(StringUtils.equalsIgnoreCase(triggerInfo.getString("type"),"GITLAB_PUSH_TRIGGER")){
                                    if(triggerInfo.containsKey("originalSecretToken")){
                                        element.setText(triggerInfo.getString("originalSecretToken"));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private DevopsJenkinsJob getBaseDevopsJenkinsJob(JSONObject jsonObject) {
        DevopsJenkinsJob devopsJenkinsJob = new DevopsJenkinsJob();
        String organizationId = SessionUtils.getOrganizationId();
        String workspaceId = SessionUtils.getWorkspaceId();
        devopsJenkinsJob.setBuildSize(0);
        devopsJenkinsJob.setId(UUIDUtil.newUUID());
        devopsJenkinsJob.setBuildable(jsonObject.getBoolean("buildable"));
        devopsJenkinsJob.setSyncStatus(JenkinsConstants.SyncStatus.END_SYNC.name());
        devopsJenkinsJob.setBuildStatus(BuildResult.NOT_BUILT.name());
        devopsJenkinsJob.setDescription(jsonObject.getString("description"));
        devopsJenkinsJob.setSyncTime(System.currentTimeMillis());
        devopsJenkinsJob.setCreateTime(System.currentTimeMillis());
        devopsJenkinsJob.setUpdateTime(System.currentTimeMillis());
        devopsJenkinsJob.setSource("local");
        devopsJenkinsJob.setOrganization(organizationId);
        devopsJenkinsJob.setWorkspace(workspaceId);
        return devopsJenkinsJob;
    }

    public void setPluginVersion(JenkinsServer jenkinsServer, Map<String, Object> objectToMap) {
        try {
            List<Plugin> plugins = jenkinsServer.getPluginManager().getPlugins();
            for (Plugin plugin : plugins) {
                if (StringUtils.equalsIgnoreCase("subversion", plugin.getShortName())) {
                    objectToMap.put("svnPlugin", "subversion@" + plugin.getVersion());
                }

                if (StringUtils.equalsIgnoreCase("git", plugin.getShortName())) {
                    objectToMap.put("gitPlugin", "git@" + plugin.getVersion());
                }
            }
        } catch (IOException e) {
            F2CException.throwException("??????JENKINS?????????????????????");
        }
    }

    private Object handleMavenProject(MavenModuleSet mavenModuleSet,Document document){
        JSONObject data = (JSONObject) JSON.toJSON(mavenModuleSet);
        Element reporters = document.getRootElement().getChild("reporters");
        if(reporters != null){
            Element child = reporters.getChild("hudson.maven.reporters.MavenMailer");
            if(child != null){
                MavenMailer mavenMailer = XmlUtils.fromXml(XmlUtils.outputXml(child), MavenMailer.class);
                JSONArray publishers = data.getJSONArray("publishers");
                if(publishers == null){
                    publishers = new JSONArray();
                    data.put("publishers",publishers);
                }
                publishers.add(JSON.toJSON(mavenMailer));
            }
        }
        return data;
    }

    public Object getJobById(String jobId) throws Exception {
        DevopsJenkinsJob devopsJenkinsJob = devopsJenkinsJobMapper.selectByPrimaryKey(jobId);
        if (!StringUtils.isNotBlank(devopsJenkinsJob.getJobXml())) {
            return null;
        }
        if (StringUtils.isNotBlank(devopsJenkinsJob.getExtParam())) {
            JSONObject data = JSON.parseObject(devopsJenkinsJob.getExtParam());
            data.put("id", jobId);
            return data;
        }
        SAXBuilder saxBuilder = XmlUtils.getSaxBuilder();
        switch (devopsJenkinsJob.getType()) {
            case JenkinsConstants.JOB_TYPE_MAVEN: {
                MavenModuleSet mavenModuleSet = new MavenModuleSet();
                mavenModuleSet.setName(devopsJenkinsJob.getName());
                mavenModuleSet.setId(devopsJenkinsJob.getId());
                Document document = saxBuilder.build(new StringReader(devopsJenkinsJob.getJobXml()));
                //Add: gitlab??????
                handlerTriggers(document,devopsJenkinsJob);
                new MavenJobParser(document, mavenModuleSet).convert();
                //???????????????????????? devopsJenkinsJob
                return handleMavenProject(mavenModuleSet,document);
            }
            case JenkinsConstants.JOB_TYPE_FREESTYLE: {
                Project project = new Project();
                project.setName(devopsJenkinsJob.getName());
                project.setId(devopsJenkinsJob.getId());
                try {
                    Document document = saxBuilder.build(new StringReader(devopsJenkinsJob.getJobXml()));

                    //Add: gitlab??????
                    handlerTriggers(document,devopsJenkinsJob);

                    new FreestyleJobParser(document, project).convert();
                } catch (JDOMException | IOException e) {
                    e.printStackTrace();
                }
                return project;
            }
            case JenkinsConstants.JOB_TYPE_FLOW: {
                WorkFlow project = new WorkFlow();
                project.setName(devopsJenkinsJob.getName());
                project.setId(devopsJenkinsJob.getId());
                Document document = saxBuilder.build(new StringReader(devopsJenkinsJob.getJobXml()));
                new WorkFlowJobParser(document, project).convert();
                return project;
            }
            case JenkinsConstants.JOB_TYPE_MULTIBRANCH: {
                WorkflowMultiBranchProject project = new WorkflowMultiBranchProject();
                project.setName(devopsJenkinsJob.getName());
                project.setId(devopsJenkinsJob.getId());
                Document document = saxBuilder.build(new StringReader(devopsJenkinsJob.getJobXml()));
                WorkflowMultiBranchParser workflowMultiBranchParser = new WorkflowMultiBranchParser();
                workflowMultiBranchParser.doConvert(document, project);
                //???????????????????????? devopsJenkinsJob
                JSONObject data = (JSONObject) JSON.toJSON(project);
                List<BranchSource> branchSource = project.getSources().getData().getBranchSource();
                if (CollectionUtils.isNotEmpty(branchSource)) {
                    data.put("scm", ImmutableMap.of("type", branchSource.get(0).getSource().getType()));
                    data.put("sources", branchSource.stream().map(BranchSource::getSource).collect(Collectors.toList()));
                }
                if (StringUtils.isNotBlank(devopsJenkinsJob.getExtParam())) {
                    data.put(MULTIBRANCH_PUBLISHER, devopsJenkinsService.getPublish(devopsJenkinsJob));
                }
                if (project.getFactory() != null) {
                    data.put("scriptPath", project.getFactory().getScriptPath());
                }
                return data;
            }
            default: {
                F2CException.throwException("???????????????????????????");
                return null;
            }
        }
    }

    private String getJobType(String jobXml) {
        if (jobXml.contains("<maven2-moduleset")) {
            return JenkinsConstants.JobType.MAVEN.name();
        } else if (jobXml.contains("<project>")) {
            return JenkinsConstants.JobType.FREE_STYLE.name();
        } else if (jobXml.contains("flow-definition")) {
            return JenkinsConstants.JOB_TYPE_FLOW;
        } else if (jobXml.contains("WorkflowMultiBranchProject") && !jobXml.contains("OrganizationFolder")) {
            return JenkinsConstants.JOB_TYPE_MULTIBRANCH;
        } else {
            return JenkinsConstants.JobType.UNKNOWN.name();
        }
    }

    private ApplicationDTO getApplicationDTOById(String appId) {
        if (StringUtils.isBlank(appId)) {
            return null;
        }
        Application application = applicationMapper.selectByPrimaryKey(appId);
        ApplicationDTO applicationDTO = null;
        if (application != null) {
            applicationDTO = new ApplicationDTO();
            ApplicationVersionExample applicationVersionExample = new ApplicationVersionExample();
            applicationVersionExample.createCriteria().andApplicationIdEqualTo(appId);
            long count = applicationVersionMapper.countByExample(applicationVersionExample);
            BeanUtils.copyBean(applicationDTO, application);
            applicationDTO.setVersionCount((int) count);
        }
        return applicationDTO;
    }

    public ApplicationDTO getApplicationByJob(@NonNull DevopsJenkinsJob job) {
        String appId = job.getAppId();
        ApplicationDTO app = getApplicationDTOById(appId);
        if (app != null) {
            return app;
        }
        switch (job.getType()) {
            case JenkinsConstants.JOB_TYPE_FREESTYLE: 
            case JenkinsConstants.JOB_TYPE_MAVEN: {
                appId = StringUtils.substringBetween(job.getJobXml(), "<applicationId>", "</applicationId>");
                break;
            }
            case JenkinsConstants.JOB_TYPE_MULTIBRANCH: {
                if (StringUtils.isNotBlank(job.getExtParam())) {
                    appId = devopsJenkinsService.getPublish(job).getString("applicationId");
                    break;
                } 
                if (StringUtils.isBlank(job.getParentId())) {
                    LogUtil.error(String.format("JenkinsJob[%s:%s] ???????????????job,????????????????????????Job,????????????????????????.", job.getId(), job.getName()));
                    deleteLocalJenkinsJob(job); //??????????????????
                    break;
                }
                DevopsJenkinsJob parentJob = devopsJenkinsJobMapper.selectByPrimaryKey(job.getParentId());
                if (parentJob == null ) {
                    LogUtil.error(String.format("JenkinsJob[%s:%s] ???????????????job,???????????????Job[%s]?????????????????????.", job.getId(), job.getName(), job.getParentId()));
                    deleteLocalJenkinsJob(job); //??????????????????
                    break;
                }
                appId = parentJob.getAppId(); // ??????????????????????????????????????????
                break;
            }
            case JenkinsConstants.JOB_TYPE_FLOW: {
                LogUtil.warn(String.format("???????????????????????????????????????"));
                break;
            }
            default: {
                LogUtil.error(String.format("JenkinsJob[%s:%s] ??????????????????[%s].", job.getId(), job.getName(), job.getType()));
                break;
            }
        }
        app = getApplicationDTOById(appId);
        if (app != null) {
            job.setAppId(app.getId());
            devopsJenkinsJobMapper.updateByPrimaryKey(job);
        }
        return app;
    }

    public List<DevopsJenkinsJob> getDevopsJenkinsJobByAppId(String appId) {
        Map<String, Object> conditions = new HashMap<String,Object>();
        CommonUtils.filterPermission(conditions);
        Example example = new Example(DevopsJenkinsJob.class);
        example.createCriteria().orEqualTo("appId", appId).orIsNull("appId");
        if (conditions.containsKey("workspaceId")) {
            example.and().andEqualTo("workspace", conditions.get("workspaceId"));
        }
        if (conditions.containsKey("organizationId")) {
            example.and().andEqualTo("organization", conditions.get("organizationId"));
        }
        List<DevopsJenkinsJob> jobs = devopsJenkinsJobMapper.selectByExample(example);
        if (jobs == null || jobs.isEmpty()) {
            LogUtil.warn(String.format("???????????????[appId=%s, or appId=null, and workspaceId=%s, organizationId=%s],???????????????jobs", 
                        appId, conditions.getOrDefault("workspaceId", "none"), conditions.getOrDefault("organizationId", "none")));
            return jobs;
        }
        Iterator<DevopsJenkinsJob> iterator = jobs.iterator();
        while (iterator.hasNext()) {
            DevopsJenkinsJob job = iterator.next();
            if (!StringUtils.isBlank(job.getAppId())) {
                continue;
            }
            ApplicationDTO app = getApplicationByJob(job);
            if (app != null) {
                if (StringUtils.equals(app.getId(), appId)) {
                    continue;
                }
            } else {
                LogUtil.warn(String.format("?????????: JenkinsJobs[%s:%s] ???????????????????????????,?????????none.", job.getId(), job.getName()));
                job.setAppId("none");
                devopsJenkinsJobMapper.updateByPrimaryKey(job);
            }
            iterator.remove();
        }
        return jobs;
    }

    public JenkinsJobSonarqubeParams getSonarParamFromJobXml(@NonNull DevopsJenkinsJob job) {
        final String sonarqubeXmlNode = "hudson.plugins.sonar.SonarRunnerBuilder";
        String jobXml = job.getJobXml();
        if (StringUtils.isBlank(jobXml)) {
            return null;
        }
        if (!jobXml.contains(sonarqubeXmlNode)) {
            return null;
        }

        Document document = null;
        try { 
            document = XmlUtils.getSaxBuilder().build(new StringReader(jobXml));
        } catch (JDOMException|IOException e){ 
            LogUtil.warn(String.format("JenkinsJob[%s:%s] ????????????xml: %s.", job.getId(), job.getName(), jobXml));
            return null;
        }
        
        XPathFactory xFactory = XPathFactory.instance();
        List<Element> sonarEles = null;
        try { 
            XPathExpression<Element> expr = xFactory.compile("//" + sonarqubeXmlNode, Filters.element());
            sonarEles = expr.evaluate(document);
        } catch (Exception e) { 
            LogUtil.error(String.format("JenkinsJob[%s:%s] ???????????????xml??????=[%s].", job.getId(), job.getName(), sonarqubeXmlNode));
            return null;
        }
        
        if (sonarEles.isEmpty()) {
            LogUtil.error(String.format("JenkinsJob[%s:%s]xml??????[%s]????????????.", job.getId(), job.getName(), sonarqubeXmlNode));
            return null;
        }
        JenkinsJobSonarqubeParams params = new JenkinsJobSonarqubeParams();
        for (Element elm : sonarEles) {
            Element child = elm.getChild("installationName");
            if (child != null) {
                params.setServerName(child.getValue());
            } else {
                LogUtil.error(String.format("JenkinsJob[%s:%s]xml??????[%s][installationName]????????????.", job.getId(), job.getName(), sonarqubeXmlNode));
                return null;
            }
            child = elm.getChild("properties");
            if (child != null) {
                params.setProperties(child.getValue());
            }
            break;
        }
        params.setProjectKey(job.getName());
        return params;
    }

    public List<FileNodeVO> getJobWorkspace(JobWorkspaceRequest jobWorkspaceRequest) {
        List<FileNodeVO> resultList = new ArrayList<>();
        JenkinsHttpClient client = devopsJenkinsService.getJenkinsClient();
        String jobName = jobWorkspaceRequest.getJobName();
        String rootPath = WORKSPACE_URL.replace("${jobName}", jobName);
        String workspacePath = jobWorkspaceRequest.getFileNode().getPath();
        SystemParameter dirSelectorParam = systemParameterMapper.selectByPrimaryKey(JenkinsConstants.JENKINS_DIRECTORY_SELECTOR);
        SystemParameter fileSelectorParam = systemParameterMapper.selectByPrimaryKey(JenkinsConstants.JENKINS_FILE_SELECTOR);
        String dirSelector = DEFAULT_DIRECTORY_SELECTOR;
        String fileSelector = DEFAULT_FILE_SELECTOR;
        if (CommonUtils.validSysParam(dirSelectorParam)) {
            dirSelector = dirSelectorParam.getParamValue();
        }
        if (CommonUtils.validSysParam(fileSelectorParam)) {
            fileSelector = fileSelectorParam.getParamValue();
        }

        if (StringUtils.isBlank(workspacePath)) {
            workspacePath = PathConst.URL_SEP;
        }
        if (!StringUtils.endsWith(workspacePath, PathConst.URL_SEP)) {
            workspacePath += PathConst.URL_SEP;
        }
        if (!StringUtils.startsWith(workspacePath, PathConst.URL_SEP)) {
            workspacePath = PathConst.URL_SEP + workspacePath;
        }

        try {
            final String tmpWorkspacePath = workspacePath;
            String html = client.getHtml(rootPath + workspacePath);
            List<FileNodeVO> dirNodes = HtmlUtils.selectElementFromBody(html, dirSelector, element -> new FileNodeVO(element.html(), EFileNodeType.DIRECTORY.getType(), tmpWorkspacePath + element.html() + PathConst.URL_SEP));
            List<FileNodeVO> fileNodes = HtmlUtils.selectElementFromBody(html, fileSelector, element -> new FileNodeVO(element.html(), EFileNodeType.FILE.getType(), tmpWorkspacePath + element.html()));
            resultList.addAll(dirNodes);
            resultList.addAll(fileNodes);
        } catch (IOException e) {
            LogUtil.error("??????????????????????????????", e.getMessage());
            e.printStackTrace();
        }
        return resultList;
    }

    public void getWorkspaceFile(JobWorkspaceRequest request, HttpServletResponse response) {
        String address = systemParameterMapper.selectByPrimaryKey(JenkinsConstants.JENKINS_ADDRESS_PARAM).getParamValue();
        if (!StringUtils.endsWith(address, PathConst.URL_SEP)) {
            address += PathConst.URL_SEP;
        }
        JenkinsHttpClient client = devopsJenkinsService.getJenkinsClient();
        String jobName = request.getJobName();
        String rootPath = WORKSPACE_URL.replace("${jobName}", jobName).substring(1);
        String filePath = request.getFileNode().getPath();
        try {
            InputStream is = client.getFile(URI.create(address + rootPath + filePath));
            BufferedInputStream bis = new BufferedInputStream(is);
            ServletOutputStream os = response.getOutputStream();
            BufferedOutputStream bos = new BufferedOutputStream(os);
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader("Content-Disposition", "attachment;filename=" +
                    new String(request.getFileNode().getName().getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));

            int size;
            byte[] body = new byte[8192];
            while ((size = bis.read(body, 0, 8192)) != -1) {
                bos.write(body, 0, size);
            }
            bos.flush();
            bos.close();
            bis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void cleanWorkspace(String jobName) throws IOException {
        try {
            JenkinsHttpClient jenkinsClient = devopsJenkinsService.getJenkinsClient();
            jenkinsClient.post(CLEAN_WORKSPACE_URL.replace("${jobName}", jobName), true);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }
}
