package com.fit2cloud.devops.service.jenkins;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.commons.server.base.domain.SystemParameter;
import com.fit2cloud.commons.server.base.domain.SystemParameterExample;
import com.fit2cloud.commons.server.base.domain.Workspace;
import com.fit2cloud.commons.server.base.mapper.SystemParameterMapper;
import com.fit2cloud.commons.server.base.mapper.WorkspaceMapper;
import com.fit2cloud.commons.utils.CommonThreadPool;
import com.fit2cloud.commons.utils.LogUtil;
import com.fit2cloud.commons.utils.ResultHolder;
import com.fit2cloud.devops.base.domain.Application;
import com.fit2cloud.devops.base.domain.ApplicationRepositorySetting;
import com.fit2cloud.devops.base.domain.DevopsJenkinsJob;
import com.fit2cloud.devops.base.mapper.ApplicationMapper;
import com.fit2cloud.devops.base.mapper.ApplicationRepositorySettingMapper;
import com.fit2cloud.devops.base.mapper.DevopsJenkinsJobMapper;
import com.fit2cloud.devops.common.consts.JenkinsConstants;
import com.fit2cloud.devops.common.util.JenkinsConnectUtils;
import com.fit2cloud.devops.dto.ApplicationDeploymentAndTestDTO;
import com.fit2cloud.devops.dto.ApplicationVersionDTO;
import com.fit2cloud.devops.service.ApplicationDeploymentService;
import com.fit2cloud.devops.service.ApplicationSceneTestService;
import com.fit2cloud.devops.service.ApplicationVersionService;
import com.fit2cloud.devops.service.jenkins.model.event.DevopsJenkinsJobChangeEvent;
import com.fit2cloud.devops.service.jenkins.model.event.OperationType;
import com.fit2cloud.devops.service.jenkins.model.multibranch.JenkinsfileApplicationInfo;
import com.fit2cloud.quartz.service.QuartzManageService;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.client.JenkinsHttpClient;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * jenkins
 *
 * @author shaochuan
 */
@Service
public class DevopsJenkinsService {

    @Resource
    private QuartzManageService quartzManageService;

    @Resource
    private CommonThreadPool commonThreadPool;

    @Resource
    private SystemParameterMapper systemParameterMapper;

    @Resource
    private DevopsJenkinsJobService devopsJenkinsJobService;

    @Resource
    private DevopsJenkinsJobMapper devopsJenkinsJobMapper;

    @Resource
    private ApplicationRepositorySettingMapper applicationRepositorySettingMapper;

    @Resource
    private ApplicationMapper applicationMapper;

    @Resource
    private WorkspaceMapper workspaceMapper;

    @Resource
    private ApplicationVersionService applicationVersionService;

    @Resource
    private ApplicationDeploymentService applicationDeploymentService;

    @Resource
    private ApplicationSceneTestService applicationSceneTestService;

    @Resource
    private ApplicationContext applicationContext;

    /**
     * ????????????
     * @param jobName
     * @return
     */
    public ResultHolder applicationInfo(String jobName) {
        LogUtil.info("Jenkinsfile????????????,?????????" + jobName);
        try {
            jobName = URLDecoder.decode(jobName, "UTF-8");
            if(jobName.contains("/")){
                jobName = jobName.split("/")[0];
            }
        } catch (Exception e) {
        }
        JenkinsfileApplicationInfo jenkinsfileApplicationInfo = new JenkinsfileApplicationInfo();
        Example devopsJenkinsJobExample = new Example(DevopsJenkinsJob.class);
        devopsJenkinsJobExample.createCriteria().andEqualTo("name", jobName);
        List<DevopsJenkinsJob> devopsJenkinsJobs = devopsJenkinsJobMapper.selectByExample(devopsJenkinsJobExample);
        if (CollectionUtils.isEmpty(devopsJenkinsJobs)) {
            return ResultHolder.error(String.format("????????????{%s},?????????", jobName));
        }
        //????????????
        JSONObject publish = getPublish(devopsJenkinsJobs.get(0));
        if (publish == null) {
            return ResultHolder.error(String.format("??????{%s}??????", jobName));
        }
        //F2C_PUBLISHER
        String applicationId = publish.getString("applicationId");
        jenkinsfileApplicationInfo.setId(applicationId);
        Application application = applicationMapper.selectByPrimaryKey(applicationId);
        if (application == null) {
            return ResultHolder.error(String.format("????????????{%s},???????????????id[%s]?????????", jobName, applicationId));
        }
        jenkinsfileApplicationInfo.setName(application.getName());
        String workspaceId = application.getWorkspaceId();
        Workspace workspace = workspaceMapper.selectByPrimaryKey(workspaceId);
        jenkinsfileApplicationInfo.setWorkspaceId(workspaceId);
        jenkinsfileApplicationInfo.setDescription(application.getDescription());
        jenkinsfileApplicationInfo.setOrganizationId(application.getOrganizationId());
        if (workspace != null) {
            jenkinsfileApplicationInfo.setWorkspaceName(workspace.getName());
        }
        String repositorySettingId = publish.getString("repositorySettingId");
        ApplicationRepositorySetting applicationRepositorySetting = applicationRepositorySettingMapper.selectByPrimaryKey(repositorySettingId);
        if (applicationRepositorySetting != null) {
            jenkinsfileApplicationInfo.setRepositoryId(applicationRepositorySetting.getRepositoryId());
        }
        return ResultHolder.success(jenkinsfileApplicationInfo);
    }

    public JSONObject getPublish(DevopsJenkinsJob jenkinsJob) {
        //????????????
        if (StringUtils.isNotBlank(jenkinsJob.getExtParam())) {
            JSONObject ext = JSONObject.parseObject(jenkinsJob.getExtParam());
            //?????????????????? ????????????f2CPublisher
            if (StringUtils.equals(jenkinsJob.getType(),JenkinsConstants.JOB_TYPE_MULTIBRANCH)) {
                if(ext.containsKey(DevopsJenkinsJobService.MULTIBRANCH_PUBLISHER)){
                    return ext.getJSONObject(DevopsJenkinsJobService.MULTIBRANCH_PUBLISHER);
                }
                return ext;
            }
            //???????????????
            if(ext.containsKey("publishers")){
                JSONArray publishers = ext.getJSONArray("publishers");
                for (int i = 0; i < publishers.size(); i++) {
                    JSONObject publishData = publishers.getJSONObject(i);
                    if(StringUtils.equals(publishData.getString("type"),"F2C_PUBLISHER")){
                        return publishData;
                    }
                }
            }
        }
        return null;
    }

    /**
     * ???????????????
     *
     * @param data
     * @return
     */
    public ResultHolder postBuildApps(JSONObject data) {
        LogUtil.info("Jenkinsfile ????????????:" + data);
        String fullName = data.getString("jobName");
        String jobName = data.getString("jobName");
        try {
            jobName = URLDecoder.decode(jobName, "UTF-8");
            if(jobName.contains("/")){
                jobName = jobName.split("/")[0];
            }
        } catch (Exception e) {
        }
        if (StringUtils.isBlank(jobName)) {
            return ResultHolder.error("jobName ????????????");
        }

        if (!data.getBoolean("success")) {
            LogUtil.info(String.format("????????????{%s},????????????", fullName));
            return ResultHolder.error(String.format("????????????{%s},????????????", fullName));
        }
        Example devopsJenkinsJobExample = new Example(DevopsJenkinsJob.class);
        devopsJenkinsJobExample.createCriteria().andEqualTo("name", jobName);
        List<DevopsJenkinsJob> devopsJenkinsJobs = devopsJenkinsJobMapper.selectByExample(devopsJenkinsJobExample);
        if (CollectionUtils.isEmpty(devopsJenkinsJobs)) {
            return ResultHolder.error(String.format("????????????{%s},?????????", jobName));
        }
        DevopsJenkinsJob devopsJenkinsJob = devopsJenkinsJobs.get(0);
        JSONObject publish = getPublish(devopsJenkinsJob);
        String repositorySettingId = publish.getString("repositorySettingId");
        ApplicationRepositorySetting applicationRepositorySetting = applicationRepositorySettingMapper.selectByPrimaryKey(repositorySettingId);

        if (StringUtils.isAnyBlank(data.getString("filePath"), data.getString("buildVersion"))) {
            return ResultHolder.error(String.format("????????????{%s},filePath???buildVersion ??????????????????", jobName));
        }
        //?????????????????????...
        ApplicationVersionDTO applicationVersion = new ApplicationVersionDTO();
        applicationVersion.setApplicationId(applicationRepositorySetting.getApplicationId());
        applicationVersion.setName(data.getString("buildVersion"));
        applicationVersion.setEnvironmentValueId(applicationRepositorySetting.getEnvId());
        applicationVersion.setApplicationRepositoryId(applicationRepositorySetting.getRepositoryId());
        applicationVersion.setLocation(data.getString("filePath"));
        try {
            applicationVersionService.saveApplicationVersion(applicationVersion);
        } catch (Exception e) {
            return ResultHolder.error("??????????????????????????????" + e.getMessage());
        }
        //?????????????????????
        checkNewJob(fullName, devopsJenkinsJob);
        boolean autoDeploy = publish.getBooleanValue("autoDeploy");
        //????????????????????????...
        if (autoDeploy) {
            ApplicationDeploymentAndTestDTO applicationDeployment = new ApplicationDeploymentAndTestDTO();
            applicationDeployment.setJobName(jobName);
            applicationDeployment.setClusterId(publish.getString("clusterId"));
            applicationDeployment.setClusterRoleId(publish.getString("clusterRoleId"));
            applicationDeployment.setCloudServerId(publish.getString("cloudServerId"));
            applicationDeployment.setApplicationVersionId(applicationVersion.getId());
            applicationDeployment.setPolicy(publish.getString("deployPolicy"));
            applicationDeployment.setDeploymentLevel(publish.getString("deploymentLevel"));
            applicationDeployment.setBackupQuantity(publish.getInteger("backupQuantity"));
            applicationDeployment.setDescription("Jenkins ??????");
            ResultHolder resultHolder = applicationDeploymentService.saveApplicationDeployment(applicationDeployment);
            if (!resultHolder.isSuccess()) {
                return resultHolder;
            }
        }
        ResultHolder resultHolder = new ResultHolder();
        resultHolder.setSuccess(true);
        resultHolder.setMessage("OK");
        return resultHolder;
    }


    private void checkNewJob(String fullName, DevopsJenkinsJob devopsJenkinsJob) {
        Example devopsJenkinsJobExample = new Example(DevopsJenkinsJob.class);
        devopsJenkinsJobExample.createCriteria().andEqualTo("name", fullName);
        List<DevopsJenkinsJob> devopsJenkinsJobs = devopsJenkinsJobMapper.selectByExample(devopsJenkinsJobExample);
        if (CollectionUtils.isEmpty(devopsJenkinsJobs)) {
            applicationContext.publishEvent(new DevopsJenkinsJobChangeEvent(devopsJenkinsJob, OperationType.SYNC));
        }
    }

    public List<SystemParameter> getJenkinsSystemParams() {
        SystemParameterExample systemParameterExample = new SystemParameterExample();
        systemParameterExample.setOrderByClause("sort");
        systemParameterExample.createCriteria().andParamKeyLike("jenkins.%");
        return systemParameterMapper.selectByExample(systemParameterExample);
    }

    public JenkinsServer getJenkinsServer() {
        String address = systemParameterMapper.selectByPrimaryKey(JenkinsConstants.JENKINS_ADDRESS_PARAM).getParamValue();
        String username = systemParameterMapper.selectByPrimaryKey(JenkinsConstants.JENKINS_USERNAME_PARAM).getParamValue();
        String password = systemParameterMapper.selectByPrimaryKey(JenkinsConstants.JENKINS_PASSWORD_PARAM).getParamValue();
        return JenkinsConnectUtils.getJenkinsServer(address, username, password);
    }

    /**
     * ???????????????????????????????????????
     */
    public void reAddJenkinsAutoSyncJ() {
        String enableCron = systemParameterMapper.selectByPrimaryKey(JenkinsConstants.JENKINS_CRON_FLAG_PARAM).getParamValue();
        if (StringUtils.equalsIgnoreCase(enableCron, "true")) {
            String cronSpec = systemParameterMapper.selectByPrimaryKey(JenkinsConstants.JENKINS_CRON_SPEC_PARAM).getParamValue();
            try {
                if (CronExpression.isValidExpression(cronSpec)) {
                    Trigger oldTrigger = quartzManageService.getTrigger(new TriggerKey(JenkinsConstants.JENKINS_SYNC_TRIGGER_KEY));
                    if (oldTrigger != null) {
                        quartzManageService.deleteJob(oldTrigger.getJobKey());
                    }
                    Trigger trigger = TriggerBuilder.newTrigger().withIdentity(JenkinsConstants.JENKINS_SYNC_TRIGGER_KEY).withSchedule(CronScheduleBuilder.cronSchedule(cronSpec).withMisfireHandlingInstructionDoNothing()).build();
                    quartzManageService.addJob("devopsJenkinsJobService", "syncAllJenkinsJobs", trigger);
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.error("??????????????????????????????" + e.getMessage());
            }
        }
    }

    public ResultHolder validateSystemParams(List<SystemParameter> systemParameters) {
        HashMap<String, String> paramMap = new HashMap<>(8);
        systemParameters.forEach(param -> paramMap.put(param.getParamKey(), param.getParamValue()));
        String address = paramMap.get(JenkinsConstants.JENKINS_ADDRESS_PARAM);
        String username = paramMap.get(JenkinsConstants.JENKINS_USERNAME_PARAM);
        String password = paramMap.get(JenkinsConstants.JENKINS_PASSWORD_PARAM);
        String enableCronSync = paramMap.get(JenkinsConstants.JENKINS_CRON_FLAG_PARAM);
        String cronSyncSpec = paramMap.get(JenkinsConstants.JENKINS_CRON_SPEC_PARAM);
        ResultHolder resultHolder = new ResultHolder();
        if (StringUtils.equalsIgnoreCase(enableCronSync, "true")) {
            if (!CronExpression.isValidExpression(cronSyncSpec)) {
                resultHolder.setSuccess(false);
                resultHolder.setMessage("???????????????????????????!");
                return resultHolder;
            }
        }
        if (StringUtils.isAnyBlank(address, username, password)) {
            resultHolder.setSuccess(false);
            resultHolder.setMessage("?????????????????????????????????????????????????????????");
            return resultHolder;
        }
        if (!JenkinsConnectUtils.validateJenkins(address, username, password)) {
            resultHolder.setSuccess(false);
            resultHolder.setMessage("??????Jenkins???????????????????????????????????????????????????Jenkins????????????????????????");
        }
        return resultHolder;
    }

    public ResultHolder saveJenkinsSystemParams(List<SystemParameter> systemParameters) {
        ResultHolder resultHolder = validateSystemParams(systemParameters);
        Map<String, String> map = new HashMap<>(8);
        systemParameters.forEach(systemParameter -> map.put(systemParameter.getParamKey(), systemParameter.getParamValue()));
        boolean cronFlag = Boolean.parseBoolean(map.get(JenkinsConstants.JENKINS_CRON_FLAG_PARAM));
        String cronSpec = map.get(JenkinsConstants.JENKINS_CRON_SPEC_PARAM);
        if (resultHolder.isSuccess()) {
            systemParameters.forEach(systemParameter -> systemParameterMapper.updateByPrimaryKey(systemParameter));
            commonThreadPool.addTask(() -> devopsJenkinsJobService.syncAllJenkinsJobs());
            try {
                Trigger oldTrigger = quartzManageService.getTrigger(new TriggerKey(JenkinsConstants.JENKINS_SYNC_TRIGGER_KEY));
                if (oldTrigger != null) {
                    quartzManageService.deleteJob(oldTrigger.getJobKey());
                }
//                ???????????????????????????????????????
                if (cronFlag) {
                    Trigger trigger = TriggerBuilder.newTrigger().withIdentity(JenkinsConstants.JENKINS_SYNC_TRIGGER_KEY).withSchedule(CronScheduleBuilder.cronSchedule(cronSpec).withMisfireHandlingInstructionDoNothing()).build();
                    quartzManageService.addJob("devopsJenkinsJobService", "syncAllJenkinsJobs", trigger);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return resultHolder;
    }

    public JenkinsHttpClient getJenkinsClient() {
        String address = systemParameterMapper.selectByPrimaryKey(JenkinsConstants.JENKINS_ADDRESS_PARAM).getParamValue();
        String username = systemParameterMapper.selectByPrimaryKey(JenkinsConstants.JENKINS_USERNAME_PARAM).getParamValue();
        String password = systemParameterMapper.selectByPrimaryKey(JenkinsConstants.JENKINS_PASSWORD_PARAM).getParamValue();
        JenkinsHttpClient jenkinsHttpClient = null;
        try {
            jenkinsHttpClient = new JenkinsHttpClient(new URI(address), username, password);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return jenkinsHttpClient;
    }

    public String getSyncStatus() {
        return systemParameterMapper.selectByPrimaryKey(JenkinsConstants.JENKINS_SYNC_STATUS_PARAM).getParamValue();
    }
}
