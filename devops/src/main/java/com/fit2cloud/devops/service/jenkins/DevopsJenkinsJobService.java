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
                    String form = "{\"\": \"2\", \"credentials\": {\"scope\": \"GLOBAL\", \"apiToken\": \"444\", \"$redact\": \"apiToken\", \"id\": \"ST\", \"description\": \"辅助云管加密Secret token\", \"stapler-class\": \"com.dabsquared.gitlabjenkins.connection.GitLabApiTokenImpl\", \"$class\": \"com.dabsquared.gitlabjenkins.connection.GitLabApiTokenImpl\"}, \"Jenkins-Crumb\": \"5896668f73e7c9e4a44c15c859f907e3ee3fe2ffc1f04c7c8f7d16a5b128d33c\"}";
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
     * Secret token 加密
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
//        检查同步状态
        if (StringUtils.equalsIgnoreCase(syncStatusParam.getParamValue(), JenkinsConstants.SyncStatus.IN_SYNC.name())) {
            return;
        }

        try {
//        设置为正在同步
            syncStatusParam.setParamValue(JenkinsConstants.SyncStatus.IN_SYNC.name());
            int i = systemParameterMapper.updateByPrimaryKey(syncStatusParam);
            if (i == 0) {
                return;
            }
            Example example = new Example(DevopsJenkinsJob.class);
            example.createCriteria().andIsNull("parentId");
            List<DevopsJenkinsJob> devopsJenkinsJobs = devopsJenkinsJobMapper.selectByExample(example);
            JenkinsServer jenkinsServer = devopsJenkinsService.getJenkinsServer();
//        数据库中现存的job
            Map<String, DevopsJenkinsJob> currentJobMap = devopsJenkinsJobs.stream().collect(Collectors.toMap(DevopsJenkinsJob::getName, o -> o));
            Map<String, Job> remoteServerJobs = jenkinsServer.getJobs();

            remoteServerJobs.forEach((name, job) -> {
                DevopsJenkinsJob currentJob = currentJobMap.get(name);
//                进入 try 才会执行 finally，所以这边
                if (currentJob != null && JenkinsConstants.SyncStatus.IN_SYNC.name().equalsIgnoreCase(currentJob.getSyncStatus())) {
                    currentJobMap.remove(name);
                    return;
                }
                try {
                    JobWithDetails details = job.details();
                    String jobXml = jenkinsServer.getJobXml(name);

                    if (currentJob == null) {
//                      服务器有，本地没有，就是新增
//                      理论上这边不会抛异常出来
                        DevopsJenkinsJob newJob = setupDevopsJenkinsJob(details);
                        newJob.setJobXml(jobXml);
                        newJob.setType(getJobType(newJob.getJobXml()));
                        devopsJenkinsJobMapper.insert(newJob);
                        //jenkins多分支流水线创建后发布job变更事件
                        if(StringUtils.equals(JenkinsConstants.JOB_TYPE_MULTIBRANCH,newJob.getType())){
                            applicationContext.publishEvent(new DevopsJenkinsJobChangeEvent(newJob, OperationType.ADD));
                        }
                        return;
                    }

//                  两边都有，就更新
                    currentJob.setSyncStatus(JenkinsConstants.SyncStatus.IN_SYNC.name());
                    devopsJenkinsJobMapper.updateByPrimaryKeySelective(currentJob);

                    currentJob.setBuildable(details.isBuildable());
                    currentJob.setSyncTime(System.currentTimeMillis());
                    currentJob.setDescription(details.getDescription());
                    currentJob.setJobXml(jobXml);
                    currentJob.setBuildSize(details.getAllBuilds().size());
                    currentJob.setUrl(details.getUrl());
                    currentJob.setType(getJobType(jobXml));
                    // 要从jobxml中解析参数化标签是否存在
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
                    LogUtil.error(String.format("同步构建任务[%s]失败,Error: %s", name, e.getMessage()));
                    e.printStackTrace();
                } finally {
//                    到这边应该是只有更新 job 出错才会执行
                    if (currentJob != null && JenkinsConstants.SyncStatus.IN_SYNC.name().equalsIgnoreCase(currentJob.getSyncStatus())) {
                        currentJob.setSyncStatus(JenkinsConstants.SyncStatus.ERROR_SYNC.name());
                        devopsJenkinsJobMapper.updateByPrimaryKeySelective(currentJob);
                    }
                    currentJobMap.remove(name);
                }
            });
//            剩下的就是删除，这边用阻塞删除，避免下面的异步历史记录同步选出不存在的job
            currentJobMap.forEach((name, job) -> deleteLocalJenkinsJob(job));
//            同步构建历史
            List<DevopsJenkinsJob> newJobs = devopsJenkinsJobMapper.selectByExample(example);
            commonThreadPool.addTask(() -> newJobs.forEach(job -> devopsJenkinsJobHistoryService.syncJobHistory(job)));
//             同步结束
            syncStatusParam.setParamValue(JenkinsConstants.SyncStatus.END_SYNC.name());
        } catch (Exception e) {
            syncStatusParam.setParamValue(JenkinsConstants.SyncStatus.ERROR_SYNC.name());
            LogUtil.error(String.format("同步构建任务失败，Error: %s", e.getMessage()));
        } finally {
//            同步结束
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
            LogUtil.error(String.format("获取新构建任务[%s]失败,Error: %s", jobWithDetails.getName(), e.getMessage()));
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
                            //BUG: 构建时jobWithDetails.getLastBuild().details().getResult()返回的是SUCCESS 但状态依然为构建中
                            //   BuildResult result = jobWithDetails.getLastBuild().details().getResult();
                            if (jobWithDetails.getLastBuild().details().isBuilding()) {
                                tmpJob.setBuildStatus(BuildResult.BUILDING.name());
                            } else {
                                tmpJob.setBuildStatus(jobWithDetails.getLastBuild().details().getResult().name());
                            }
                        } else {
//                        这边为 null 的情况可能是点击构建之后 jenkins 那边并没有去执行构建，
//                        但是程序还是将 job 标注为正在构建
                            tmpJob.setBuildStatus(BuildResult.NOT_BUILT.name());
                        }
                    }
                } catch (Exception e) {
                    tmpJob.setBuildStatus(BuildResult.UNKNOWN.name());
                    LogUtil.error(String.format("同步构建任务[%s]状态失败，Error: %s", job.getName(), e.getMessage()));
                    e.printStackTrace();
                } finally {
//                    同步完成
                    devopsJenkinsJobMapper.updateByPrimaryKeySelective(tmpJob);
                }
            });
        }
    }

    /**
     * 批量同步构建任务
     */
    public void syncJobs(List<DevopsJenkinsJob> devopsJenkinsJobs) {
        if (CollectionUtils.isEmpty(devopsJenkinsJobs)) {
            F2CException.throwException("请选择需要同步的构建任务！");
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
                //多分支任务同步
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

                    // 要从jobxml中解析参数化标签是否存在
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
                    //jenkins多分支流水线创建后自动构建
                    if(StringUtils.equals(JenkinsConstants.JOB_TYPE_MULTIBRANCH,devopsJenkinsJob.getType())){
                        devopsJenkinsJob.setBuildStatus(BuildResult.SUCCESS.name());
                        devopsJenkinsJob.setBuildable(true);
                    }
                    devopsJenkinsJob.setSyncStatus(JenkinsConstants.SyncStatus.END_SYNC.name());
                    //devopsJenkinsJob.setExtParam("");
                } catch (Exception e) {
                    devopsJenkinsJob.setSyncStatus(JenkinsConstants.SyncStatus.ERROR_SYNC.name());
                    LogUtil.error(String.format("同步构建任务[%s]失败,Error: %s", job.getName(), e.getMessage()));
                    e.printStackTrace();
                } finally {
                    devopsJenkinsJobMapper.updateByPrimaryKeySelective(devopsJenkinsJob);
                    devopsJenkinsJobHistoryService.syncJobHistory(devopsJenkinsJob);
                }
            });
        });
    }


    /**
     * 批量构建DevopsJenkinsJob
     *
     * @param devopsJenkinsJobs 构建任务
     */
    public void buildJobs(List<DevopsJenkinsJob> devopsJenkinsJobs) {
        for (DevopsJenkinsJob jenkinsJob : devopsJenkinsJobs) {
            if (BooleanUtils.toBoolean(jenkinsJob.getBuildable())) {
                commonThreadPool.addTask(() -> {
                    try {
                        buildJob(jenkinsJob);
                    } catch (Exception e) {
                        LogUtil.error(e.getMessage());
                        F2CException.throwException("构建失败" + e.getMessage());
                    }
                });
            }
        }
    }

    /**
     * 参数化构建DevopsJenkinsJob
     *
     * @param devopsJenkinsJob 参数化构建任务
     */
    public void buildWithParametersJob(DevopsJenkinsJob devopsJenkinsJob) {
        if (devopsJenkinsJob == null || StringUtils.isBlank(devopsJenkinsJob.getName())) {
            F2CException.throwException("构建任务不能为空！");
            return;
        }

        // build params 值是字符串格式的hashmap 前端注意转换
        if (devopsJenkinsJob.getParams() == null || devopsJenkinsJob.getParams().isEmpty()) {
            F2CException.throwException("构建参数不能为空！");
            return;
        }

//        LogUtil.info(String.format("参数化构建任务[%s],构建参数[%s]", devopsJenkinsJob.getName(), devopsJenkinsJob.getParams()));

        // 测试验证，待删除
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

            // 通过api http方式调用时，uri需要特殊编码 未正确转换下预期在服务端收到时出现特殊空格符 暂时只针对#手动替换处理
            HashMap<String, String> maps = devopsJenkinsJob.getParams();
            for (String key : maps.keySet()) {
                String value = maps.get(key);
                maps.put(key, URLEncoder.encode(value,"UTF-8"));
            }
            // 调用参数化构建动作
            QueueReference ref = job.build(maps, true);
            commonBuildStep(devopsJenkinsJob, ref);
        } catch (Exception e) {
            // 这里不做普通构建动作的失败重做特殊处理
            devopsJenkinsJob.setBuildStatus(BuildResult.UNKNOWN.name());
            devopsJenkinsJobMapper.updateByPrimaryKeySelective(devopsJenkinsJob);
            LogUtil.error(String.format("触发参数化构建任务[%s]失败,构建参数[%s],Error: %s", devopsJenkinsJob.getName(), devopsJenkinsJob.getParams(), e.getMessage()));
            e.printStackTrace();
        }

        return;
    }


    /**
     * 构建DevopsJenkinsJob
     *
     * @param devopsJenkinsJob 构建任务
     */
    public void buildJob(DevopsJenkinsJob devopsJenkinsJob) {
        if (devopsJenkinsJob == null || StringUtils.isBlank(devopsJenkinsJob.getName())) {
            F2CException.throwException("构建任务不能为空！");
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
//            首先使用无参数构建
            QueueReference ref = job.build(true);
            commonBuildStep(devopsJenkinsJob, ref);
        } catch (Exception e) {
            try {
                assert job != null;
//                失败后使用默认参数构建
                QueueReference ref = job.build(new HashMap<>(), true);
                commonBuildStep(devopsJenkinsJob, ref);
            } catch (Exception ex) {
                devopsJenkinsJob.setBuildStatus(BuildResult.UNKNOWN.name());
                devopsJenkinsJobMapper.updateByPrimaryKeySelective(devopsJenkinsJob);
                LogUtil.error(String.format("触发构建任务[%s]失败,Error: %s", devopsJenkinsJob.getName(), e.getMessage()));
                ex.printStackTrace();
            }
        }
    }

    /**
     * 阻塞构建思路整理：
     * <p>
     * 1. 触发构建之后 jenkins 会返回一个队列引用 QueueReference ，这个类里面的保存了这个队列对象 QueueItem 的 url，
     * 通过这个 url 可以知道这个队列对象的状态。
     * 2. 根据 jenkins 官方文档，一个队列对象主要有三种状态。第一种是等待中(pending)，这种状态是因为当前构建器已经被占满，
     * 或者是一个构建任务触发了两次，并且没有打开并发构建选项；第二种状态是构建中(building)，这个通过队列对象的 Executable 这个属性来判断，
     * 只要队列对象有这个属性一般就代表正在构建中；第三种状态是已取消(cancelled)，这个是构建任务在等待(pending)的时候人为主动取消，通过队列对象的 cancelled 属性来判断
     * 3. 正在构建的任务手动取消的话状态会变为已阻止(aborted)，这个在应该要在构建结果那边展示，因为已经产生了构建历史记录，而等待中取消是不会产生构建历史记录的。
     * 4. 所有的队列对象不管是什么状态都是有时效性的，时间一过队列对象的 url 就会变成 404，这个算是潜在的坑点。
     * 综上，总体思路就是在还未 404 的情况下去轮询队列对象状态，也就是在 pending 的时候一直去轮询，此时的 executable 应该是 null ，并且 cancelled 为 false，
     * 只要队列对象一旦处于构建状态就插入一条构建状态为 building 的构建历史记录(这边需要注意不要和历史记录同步冲突插入多条相同的记录)，然后设定的同步构建状态的 cronjob 就会
     * 扫描到这条记录继续同步构建结果，这边不考虑任务被手动终止的情况是因为这类任务的结果会在构建历史记录中体现出来(构建结果为 aborted)，也就是同步构建结果就可以看到了。
     *
     * @param devopsJenkinsJob 目标构建任务
     * @param queueReference   构建成功后的队列引用
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

//            超过20次就去同步完整历史记录
            if (count.get() < 1) {
                commonThreadPool.addTask(() -> devopsJenkinsJobHistoryService.syncJobHistory(devopsJenkinsJob));
                return;
            }

            if (!queueItem.isCancelled()) {
                Build build = jenkinsServer.getBuild(queueItem);
                BuildWithDetails buildDetail = build.details();
                //BUG: 构建时buildDetail.getResult()返回的是SUCCESS 但状态依然为构建中
                //buildHistory.setBuildStatus(buildDetail.getResult() == null ? BuildResult.BUILDING.name() : buildDetail.getResult().name());
                buildHistory.setBuildStatus(buildDetail.isBuilding() ? BuildResult.BUILDING.name() : buildDetail.getResult().name());
                if (buildDetail.isBuilding()) {
                    LogUtil.warn(String.format("任务 %s 正在构建中", devopsJenkinsJob.getName()));
                } else {
                    LogUtil.warn(String.format("任务 %s 不是正在构建状态 任务明细结果 %s", devopsJenkinsJob.getName(), buildDetail.getResult().name()));
                }
                buildHistory.setOrderNum(build.getNumber());
                buildHistory.setSyncTime(System.currentTimeMillis());
                buildHistory.setIsBuilding(buildDetail.isBuilding());
                buildHistory.setName(buildDetail.getDisplayName());
                buildHistory.setBuildTime(buildDetail.getTimestamp());
                buildHistory.setUrl(build.getUrl());
                buildHistory.setDescription(buildDetail.getDescription());

                //Add: 转换为json字符串格式 持久化
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
            LogUtil.error("线程被终止", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            LogUtil.debug(String.format("阻塞构建[%s]出错，Error: %s", devopsJenkinsJob.getName(), e.getMessage()));
            e.printStackTrace();
//            阻塞构建出错的话就全量同步构建历史
            commonThreadPool.addTask(() -> devopsJenkinsJobHistoryService.syncJobHistory(devopsJenkinsJob));
        } finally {
            if (hasTriggerBuild) {
                //绑定pipeline
                applicationPipelineSevice.bindJobHistory(buildHistory);
                LogUtil.warn(String.format("任务 %s 已经触发构建", devopsJenkinsJob.getName()));
                DevopsJenkinsJobHistoryExample example = new DevopsJenkinsJobHistoryExample();
                example.createCriteria()
                        .andJobIdEqualTo(devopsJenkinsJob.getId())
                        .andOrderNumEqualTo(buildHistory.getOrderNum());
                List<DevopsJenkinsJobHistory> histories = devopsJenkinsJobHistoryMapper.selectByExample(example);
                if (CollectionUtils.isEmpty(histories)) {
                    LogUtil.warn(String.format("任务 %s 当前匹配的构建历史记录为空 需要插入当前构建记录", devopsJenkinsJob.getName()));
                    devopsJenkinsJobHistoryMapper.insert(buildHistory);
//                    更新一下数量
                    DevopsJenkinsJob job = devopsJenkinsJobMapper.selectByPrimaryKey(devopsJenkinsJob.getId());
                    DevopsJenkinsJob tmpJob = new DevopsJenkinsJob();
                    tmpJob.setId(job.getId());
                    tmpJob.setBuildSize(job.getBuildSize() + 1);
                    devopsJenkinsJobMapper.updateByPrimaryKeySelective(tmpJob);
                } else {
                    LogUtil.warn(String.format("任务 %s 当前匹配的构建历史记录不为空 把当前构建记录覆盖到查出的历史构建集合中最新的ID ???", devopsJenkinsJob.getName()));
                    DevopsJenkinsJobHistory localHistory = histories.get(0);
                    buildHistory.setId(localHistory.getId());
                    devopsJenkinsJobHistoryMapper.updateByPrimaryKeySelective(buildHistory);
                }
                if (BuildResult.BUILDING.name().equalsIgnoreCase(buildHistory.getBuildStatus())) {
                    LogUtil.warn(String.format("任务 %s 还在构建中", devopsJenkinsJob.getName()));
                    DevopsJenkinsJob job = new DevopsJenkinsJob();
                    job.setId(devopsJenkinsJob.getId());
                    job.setBuildStatus(BuildResult.BUILDING.name());
                    devopsJenkinsJobMapper.updateByPrimaryKeySelective(job);
                } else if (BuildResult.FAILURE.name().equalsIgnoreCase(buildHistory.getBuildStatus())) {
                    LogUtil.warn(String.format("任务 %s 构建失败", devopsJenkinsJob.getName()));
                    DevopsJenkinsJob job = new DevopsJenkinsJob();
                    job.setId(devopsJenkinsJob.getId());
                    job.setBuildStatus(BuildResult.FAILURE.name());
                    devopsJenkinsJobMapper.updateByPrimaryKeySelective(job);
                }

            }
        }

    }

    /**
     * 获取所有组织
     *
     * @return 所有组织列表
     */
    public List<Organization> getAllOrganization() {
        if (!StringUtils.equalsIgnoreCase(SessionUtils.getUser().getParentRoleId(), "ADMIN")) {
            return Collections.emptyList();
        }
        return organizationMapper.selectByExample(null);
    }

    /**
     * 根据组织ID获得所有工作空间
     *
     * @param organizationId 指定的
     * @return 对应的工作空间列表
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
     * 授权可见空间
     *
     * @param jenkinsJobs 需要授权的 job
     */
    public void jobGrant(List<DevopsJenkinsJob> jenkinsJobs) {
        for (DevopsJenkinsJob job : jenkinsJobs) {
            devopsJenkinsJobMapper.updateByPrimaryKeySelective(job);
        }
    }

    /**
     * 只删除devops端的jenkins
     *
     * @param jenkinsJob 要删除的 job
     */
    public void deleteDevJenkinsJob(DevopsJenkinsJob jenkinsJob) {
        if (devopsJenkinsJobMapper.deleteByPrimaryKey(jenkinsJob.getId()) != 0) {
            devopsJenkinsJobHistoryService.deleteJobHistory(jenkinsJob.getId());
        }
    }

    /**
     * 批量删除JenkinsJob，并在jenkins端也删除
     *
     * @param jenkinsJobs 需要删除的job
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
                    //删除jenkins服务上的任务 这里一直返回 500 服务器内部错误 只有没有初始构建的项目删除成功
                    //如果有历史记录，需要先禁用，再删除，否则会内部异常 设置禁用后同样报错
//                    if(job.details().getBuilds().isEmpty() == false){
//                        jenkinsServer.disableJob(jenkinsJob.getName(), true);
//                    }

                    jenkinsServer.deleteJob(jenkinsJob.getName(), true);
                    //jenkinsServer.
                }
                //删除本地任务历史记录
                devopsJenkinsJobHistoryService.deleteJobHistory(jenkinsJob.getId());
                //删除子任务
                Example example = new Example(DevopsJenkinsJob.class);
                example.createCriteria().andEqualTo("parentId",jenkinsJob.getId());
                devopsJenkinsJobMapper.deleteByExample(example);
            } catch (IOException e) {
                e.printStackTrace();
                LogUtil.error(String.format("删除构建任务失败 返回代码 %s 再次尝试删除", e.getMessage()));

                try {
                    jenkinsServer.deleteJob(jenkinsJob.getName(), true);
                } catch (IOException ex) {
                    LogUtil.error(String.format("再次删除构建任务失败 返回代码 %s 需要排查", ex.getMessage()));
                    F2CException.throwException("删除构建任务" + jenkinsJob.getName() + "失败");
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
        //用户构建后配置时输入优化，云管平台地址做成系统参数，判断用户无ak sk时需自动生成并存储 获取当前编辑用户的aksk列表 判断是否和原始存储的ak不同，均不相同，自动替换首个.
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
        //副本
        DevopsJenkinsJob devopsJenkinsJobRel = new DevopsJenkinsJob();
        BeanUtils.copyBean(devopsJenkinsJobRel,devopsJenkinsJob);
        String originJobXml = devopsJenkinsJob.getJobXml();
        String jobName = jsonObject.getString("name");
        LogUtil.info(String.format("jobName = [%s], json_string = %s", jobName, jsonObject.toJSONString()));

        if (!StringUtils.equals(jobName, devopsJenkinsJob.getName())) {
            // 判断任务需要重命名 TODO：要判断是否存在重名场景 重名时，应返回提示。
            try {
                jenkinsServer.renameJob(devopsJenkinsJob.getName(), jobName, true);
            } catch (IOException e) {
                LogUtil.error(String.format("任务更名失败 %s %s %s", e.getMessage(), e.getLocalizedMessage(), e.getCause()));
                F2CException.throwException("任务更名失败,请避免使用重复的任务名称");
                return;
            }
        }
        devopsJenkinsJob.setName(jobName);
        devopsJenkinsJob.setUpdateTime(System.currentTimeMillis());
        devopsJenkinsJob.setDescription(jsonObject.getString("description"));


        //Add: gitlab支持 秘钥加密后持久化
        handlerTriggers(jsonObject);

        //F2C_PUBLISHER 变更存储时自动判断是否和原始存储的ak不同，均不相同，替换为当前操作用户的首个userKey.
        JSONArray publishers = jsonObject.containsKey("publishers") ? jsonObject.getJSONArray("publishers") : new JSONArray();
//      LogUtil.info(String.format("publishers %s %d" ,publishers,publishers.size()));

        //  注意，编辑模式时，获取的配置内容，原先开发处理上因为兼容性问题没有正确的转换标签，导致无法匹配到。编辑逻辑不成功，已修正。
        for (int i = 0; i < publishers.size(); i++) {
            JSONObject publisher = publishers.getJSONObject(i);


            // 兼容云管代码部署插件1.0的格式 目前是初步展示为原始的xml格式
            if (publisher.containsKey("F2CCodeDeployPublisher")) {
                // 获取系统参数配置的云管平台地址
                DevopsJenkinsParamsExample devopsJenkinsParamsExample = new DevopsJenkinsParamsExample();
                devopsJenkinsParamsExample.createCriteria().andParamKeyEqualTo(JenkinsConstants.F2C_ENDPOINT_PARAM);
                String f2cEndpoint = devopsJenkinsParamsMapper.selectByExample(devopsJenkinsParamsExample).get(0).getParamValue();

                // 获取当前用户的userkey
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
                    LogUtil.warn("当前用户并非是原初用户，需要覆盖ak sk ");
                    //因为原先项目不是当前用户的创建使用的，需要覆盖当前新用户的ak sk
                    publisher.put("f2cAccessKey", userKey.getAccessKey());
                    publisher.put("f2cSecretKey", userKey.getSecretKey());
                }
                // 发现云管地址不一致直接替换
                if (publisher.getString("f2cEndpoint") == null || !publisher.getString("f2cEndpoint").equalsIgnoreCase(f2cEndpoint)) {
                    publisher.put("f2cEndpoint", f2cEndpoint);
                }


            }
            // 云管代码部署插件2.0 com.fit2cloud.codedeploy2.F2CCodeDeploySouthPublisher 映射为
            else if (StringUtils.equalsIgnoreCase(publisher.getString("type"),"F2C_PUBLISHER")) {
                // 获取系统参数配置的云管平台地址
                DevopsJenkinsParamsExample devopsJenkinsParamsExample = new DevopsJenkinsParamsExample();
                devopsJenkinsParamsExample.createCriteria().andParamKeyEqualTo(JenkinsConstants.F2C_ENDPOINT_PARAM);
                String f2cEndpoint = devopsJenkinsParamsMapper.selectByExample(devopsJenkinsParamsExample).get(0).getParamValue();

                // 获取当前用户的userkey
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
                    LogUtil.warn("当前用户并非是原初用户，需要覆盖ak sk ");
                    //因为原先项目不是当前用户的创建使用的，需要覆盖当前新用户的ak sk
                    publisher.put("f2cAccessKey", userKey.getAccessKey());
                    publisher.put("f2cSecretKey", userKey.getSecretKey());
                }
                // 发现云管地址不一致直接替换
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
                //jobName 是服务更新的key 要确保前面已经更名成功否则key找不到会导致异常
//              LogUtil.warn(String.format("同步构建提交的 jobxml = %s", jobXml));
                jenkinsServer.updateJob(jobName, jobXml, true);
                devopsJenkinsJob.setJobXml(jobXml);
                devopsJenkinsJob.setBuildable(!jsonObject.getBooleanValue("disabled"));
                devopsJenkinsJob.setParameterizedBuild(jsonObject.getBoolean("parameterizedBuild"));
                //多分支流水线更新任务会触发构建 需要同步
                if (StringUtils.equals(JenkinsConstants.JOB_TYPE_MULTIBRANCH, devopsJenkinsJob.getType())) {
                    devopsJenkinsJob.setSyncStatus(JenkinsConstants.SyncStatus.IN_SYNC.name());
                }
                devopsJenkinsJob.setAppId(this.getApplicationId(devopsJenkinsJob));
                devopsJenkinsJobMapper.updateByPrimaryKeySelective(devopsJenkinsJob);
                //TODO 必要信息更新时，发布job变更事件
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
        //同名不处理
        if (CollectionUtils.isNotEmpty(devopsJenkinsJobs)) {
            return;
        }

        //Add: gitlab支持 秘钥加密后持久化
        handlerTriggers(jsonObject);

        //F2C_PUBLISHER 自动填参处理
        JSONArray publishers = jsonObject.containsKey("publishers") ? jsonObject.getJSONArray("publishers") : new JSONArray();
        for (int i = 0; i < publishers.size(); i++) {
            JSONObject publisher = publishers.getJSONObject(i);
//          LogUtil.warn(String.format("type is %s ", publisher.getString("type")));
//          新建任务时，可简单判别type=F2C_PUBLISHER 但是有别于编辑模式时的数据结构
            if (StringUtils.equalsIgnoreCase(publisher.getString("type"),PublisherType.F2C_PUBLISHER.getJavaType())) {

                // 获取系统参数配置的云管平台地址
                DevopsJenkinsParamsExample devopsJenkinsParamsExample = new DevopsJenkinsParamsExample();
                devopsJenkinsParamsExample.createCriteria().andParamKeyEqualTo(JenkinsConstants.F2C_ENDPOINT_PARAM);
                String f2cEndpoint = devopsJenkinsParamsMapper.selectByExample(devopsJenkinsParamsExample).get(0).getParamValue();

                // 获取当前用户的userkey
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

        // 获取了包含工作空间等用户会话中的信息
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
                    // 按项目类生成对应的XML文档
                    document = saxBuilder.build(new StringReader(projectBaseXml));
                    // 按自由风格的项目转换json对象 输出到目标XML文档
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
            //jenkins多分支流水线创建后自动构建
            if(StringUtils.equals(JenkinsConstants.JOB_TYPE_MULTIBRANCH,devopsJenkinsJob.getType())){
                devopsJenkinsJob.setBuildStatus(BuildResult.BUILDING.name());
                //devopsJenkinsJob.setBuildable(false);
            }
            devopsJenkinsJob.setAppId(this.getApplicationId(devopsJenkinsJob));
            devopsJenkinsJob.setParameterizedBuild(jsonObject.getBoolean("parameterizedBuild"));
            devopsJenkinsJobMapper.insert(devopsJenkinsJob);
            //发布job变更事件
            applicationContext.publishEvent(new DevopsJenkinsJobChangeEvent(devopsJenkinsJob, OperationType.ADD));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建或者更新构建任务时，把绑定的应用写入构建任务的appId字段里。
     * 如果你没有绑定，则写"none"到appId字段。
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
                // 兼容云管代码部署插件1.0的格式 目前是初步展示为原始的xml格式
                if (StringUtils.equalsIgnoreCase(trigger.getString("type"),"GITLAB_PUSH_TRIGGER") && StringUtils.isNotBlank(trigger.getString("secretToken"))) {
                    // 原始的秘钥token 经过算法加密
                    String originalSecretToken = trigger.getString("secretToken");
                    if(originalSecretToken.length() <= 32){
                        String encryptedValue = encryptV2(originalSecretToken);
                        LogUtil.info(String.format("原始内容 %s 加密后的内容 %s", originalSecretToken, encryptedValue));
                        trigger.put("secretToken", encryptedValue);
                        trigger.put("originalSecretToken", originalSecretToken);
                    }
                }
            }
            // 覆盖
            jsonObject.put("triggers", triggers);
        }
    }

    private void handlerTriggers(Document document,DevopsJenkinsJob devopsJenkinsJob) {
        Element triggers = document.getRootElement().getChild("triggers");
        for (Element trigger : triggers.getChildren()) {
            if (trigger.toString().contains("GitLabPushTrigger")) {
                for (Element element : trigger.getChildren()) {
                    if (element.toString().contains("secretToken")) {
                        // 把加密的token解密为明文
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
            F2CException.throwException("获取JENKINS插件信息出错！");
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
                //Add: gitlab支持
                handlerTriggers(document,devopsJenkinsJob);
                new MavenJobParser(document, mavenModuleSet).convert();
                //适配前端数据结构 devopsJenkinsJob
                return handleMavenProject(mavenModuleSet,document);
            }
            case JenkinsConstants.JOB_TYPE_FREESTYLE: {
                Project project = new Project();
                project.setName(devopsJenkinsJob.getName());
                project.setId(devopsJenkinsJob.getId());
                try {
                    Document document = saxBuilder.build(new StringReader(devopsJenkinsJob.getJobXml()));

                    //Add: gitlab支持
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
                //适配前端数据结构 devopsJenkinsJob
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
                F2CException.throwException("不支持的任务类型！");
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
                    LogUtil.error(String.format("JenkinsJob[%s:%s] 属于多分支job,但找不到所属的父Job,孤儿流水线，删除.", job.getId(), job.getName()));
                    deleteLocalJenkinsJob(job); //删除无流水线
                    break;
                }
                DevopsJenkinsJob parentJob = devopsJenkinsJobMapper.selectByPrimaryKey(job.getParentId());
                if (parentJob == null ) {
                    LogUtil.error(String.format("JenkinsJob[%s:%s] 属于多分支job,但所属的父Job[%s]不存在，强清除.", job.getId(), job.getName(), job.getParentId()));
                    deleteLocalJenkinsJob(job); //删除无流水线
                    break;
                }
                appId = parentJob.getAppId(); // 子任务都属于父任务相同的应用
                break;
            }
            case JenkinsConstants.JOB_TYPE_FLOW: {
                LogUtil.warn(String.format("单分流水线暂不支持应用绑定"));
                break;
            }
            default: {
                LogUtil.error(String.format("JenkinsJob[%s:%s] 属于未知类型[%s].", job.getId(), job.getName(), job.getType()));
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
            LogUtil.warn(String.format("查询条件下[appId=%s, or appId=null, and workspaceId=%s, organizationId=%s],无法获取到jobs", 
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
                LogUtil.warn(String.format("请注意: JenkinsJobs[%s:%s] 不属于任何一个应用,设置为none.", job.getId(), job.getName()));
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
            LogUtil.warn(String.format("JenkinsJob[%s:%s] 无法解析xml: %s.", job.getId(), job.getName(), jobXml));
            return null;
        }
        
        XPathFactory xFactory = XPathFactory.instance();
        List<Element> sonarEles = null;
        try { 
            XPathExpression<Element> expr = xFactory.compile("//" + sonarqubeXmlNode, Filters.element());
            sonarEles = expr.evaluate(document);
        } catch (Exception e) { 
            LogUtil.error(String.format("JenkinsJob[%s:%s] 无法匹配到xml节点=[%s].", job.getId(), job.getName(), sonarqubeXmlNode));
            return null;
        }
        
        if (sonarEles.isEmpty()) {
            LogUtil.error(String.format("JenkinsJob[%s:%s]xml节点[%s]元素为空.", job.getId(), job.getName(), sonarqubeXmlNode));
            return null;
        }
        JenkinsJobSonarqubeParams params = new JenkinsJobSonarqubeParams();
        for (Element elm : sonarEles) {
            Element child = elm.getChild("installationName");
            if (child != null) {
                params.setServerName(child.getValue());
            } else {
                LogUtil.error(String.format("JenkinsJob[%s:%s]xml节点[%s][installationName]元素为空.", job.getId(), job.getName(), sonarqubeXmlNode));
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
            LogUtil.error("获取工作空间目录出错", e.getMessage());
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
