package com.fit2cloud.devops.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.commons.server.model.SessionUser;
import com.fit2cloud.commons.server.model.UserRoleDTO;
import com.fit2cloud.commons.server.service.UserCommonService;
import com.fit2cloud.commons.server.utils.SessionUtils;
import com.fit2cloud.commons.utils.LogUtil;
import com.fit2cloud.commons.utils.ResultHolder;
import com.fit2cloud.commons.utils.UUIDUtil;
import com.fit2cloud.devops.base.domain.*;
import com.fit2cloud.devops.base.mapper.*;
import com.fit2cloud.devops.common.consts.StatusConstants;
import com.fit2cloud.devops.common.model.DeploymentLevel;
import com.fit2cloud.devops.common.model.PipelineContext;
import com.fit2cloud.devops.common.util.CommonUtils;
import com.fit2cloud.devops.dao.ext.ExtApplicationDeploymentMapper;
import com.fit2cloud.devops.dto.ApplicationDeploymentAndTestDTO;
import com.fit2cloud.devops.dto.ApplicationDeploymentDTO;
import com.fit2cloud.devops.dto.ClusterDTO;
import com.fit2cloud.devops.service.deployment.job.AsycDeployJob;
import com.fit2cloud.devops.service.jenkins.DevopsJenkinsService;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fit2cloud.devops.common.consts.StatusConstants.*;

@Service
public class ApplicationDeploymentService {


    @Resource
    private ApplicationVersionService applicationVersionService;
    private AsycDeployJob asycDeployJob;
    @Resource
    private ApplicationDeploymentMapper applicationDeploymentMapper;
    @Resource
    private ClusterService clusterService;
    @Resource
    private ExtApplicationDeploymentMapper extApplicationDeploymentMapper;
    @Resource
    private ApplicationDeploymentLogService applicationDeploymentLogService;
    @Resource
    private UserCommonService userCommonService;
    @Resource
    private ApplicationDeploySettingsMapper applicationDeploySettingsMapper;
    @Resource
    private DevopsCloudServerMapper devopsCloudServerMapper;
    @Resource
    private ApplicationMapper applicationMapper;
    @Resource
    private ApplicationVersionMapper applicationVersionMapper;
    @Resource
    private ApplicationSceneTestService applicationSceneTestService;
    @Resource
    private ApplicationDeploymentEventLogMapper applicationDeploymentEventLogMapper;
    @Resource
    private DevopsJenkinsJobMapper devopsJenkinsJobMapper;
    @Resource
    private DevopsJenkinsService devopsJenkinsService;
    @Resource
    private DevopsApiTestMapper devopsApiTestMapper;


    public AsycDeployJob getAsycDeployJob() {
        return asycDeployJob;
    }

    public void setAsycDeployJob(AsycDeployJob asycDeployJob) {
        this.asycDeployJob = asycDeployJob;
    }

    private void createDeploymentJob(ApplicationDeployment applicationDeployment) {
        asycDeployJob.depoly(applicationDeployment);
    }


    public ResultHolder saveApplicationDeployment(ApplicationDeployment applicationDeployment) {
        ResultHolder resultHolder = new ResultHolder();
        if (applicationDeployment.getId() != null) {
            applicationDeploymentMapper.updateByPrimaryKeySelective(applicationDeployment);
        } else {
            if (!checkDeploySettings()) {
                resultHolder.setSuccess(false);
                resultHolder.setMessage("当前时间无法部署应用！如需部署请联系管理员或组织管理员解决！");
                return resultHolder;
            }
            applicationDeployment.setId(UUIDUtil.newUUID());
            applicationDeployment.setUserId(SessionUtils.getUser().getId());
            applicationDeployment.setWorkspaceId(SessionUtils.getWorkspaceId());
            applicationDeployment.setCreatedTime(System.currentTimeMillis());
            applicationDeployment.setStatus(StatusConstants.PENDING);

            if (applicationDeployment.getDeploymentLevel() == null) {
                applicationDeployment.setDeploymentLevel(DeploymentLevel.ALL.getValue());
            }

            if (applicationDeployment.getBackupQuantity() == null) {
                applicationDeployment.setBackupQuantity(5);
            }
            applicationDeploymentMapper.insert(applicationDeployment);
            applicationVersionService.updateLastDeployTime(applicationDeployment.getApplicationVersionId(), applicationDeployment.getCreatedTime());
            resultHolder.setData(applicationDeployment);
            createDeploymentJob(applicationDeployment);
            //1.版本列表 -> 部署应用功能 2.jenkins fit2cloud插件调用
            if(applicationDeployment instanceof ApplicationDeploymentAndTestDTO){
                ApplicationDeploymentAndTestDTO test = (ApplicationDeploymentAndTestDTO) applicationDeployment;
                JSONObject publish = null;
                if (StringUtils.isNotBlank(test.getJobName())) {
                    LogUtil.info("jenkins fit2cloud插件调用自动化测试api,任务名称："+test.getJobName());
                    Example devopsJenkinsJobExample = new Example(DevopsJenkinsJob.class);
                    devopsJenkinsJobExample.createCriteria().andEqualTo("name", test.getJobName());
                    List<DevopsJenkinsJob> devopsJenkinsJobs = devopsJenkinsJobMapper.selectByExample(devopsJenkinsJobExample);
                    if (org.apache.commons.collections.CollectionUtils.isEmpty(devopsJenkinsJobs)) {
                        LogUtil.info(String.format("任务名称{%s},不存在", test.getJobName()));
                        return ResultHolder.error(String.format("任务名称{%s},不存在", test.getJobName()));
                    }
                    publish = devopsJenkinsService.getPublish(devopsJenkinsJobs.get(0));
                } else {
                    publish = JSON.parseObject(JSON.toJSONString(test));
                }
                if (publish != null) {
                    ApplicationVersion applicationVersion = applicationVersionMapper.selectByPrimaryKey(applicationDeployment.getApplicationVersionId());
                    Application application = applicationMapper.selectByPrimaryKey(applicationVersion.getApplicationId());
                    //执行代码测试...
                    boolean autoApiTest = publish.getBooleanValue("autoApiTest");
                    PipelineContext pipelineContext = PipelineContext.builder().applicationDeploymentId(applicationDeployment.getId())
                            .application(application).applicationVersion(applicationVersion)
                            .publish(publish).jobName(test.getJobName()).buildNumber(test.getBuildNumber()).build();
                    if (autoApiTest) {
                        //覆盖默认绑定的参数
                        if (StringUtils.isNotBlank(publish.getString("testProdId"))) {
                            application.setTestProdId(publish.getString("testProdId"));
                        }
                        if (StringUtils.isNotBlank(publish.getString("testPlanId"))) {
                            application.setTestPlanId(publish.getString("testPlanId"));
                        }
                        if (StringUtils.isNotBlank(publish.getString("testEvn"))) {
                            application.setTestEvn(publish.getString("testEvn"));
                        }
                        applicationSceneTestService.asyncDoApiTest(pipelineContext);
                    } else {
                        applicationSceneTestService.pollingDeployComplete(pipelineContext);
                    }
                }
            }
        }
        return resultHolder;
    }


    public ApplicationDeployment getApplicationDeployment(String applicationDeploymentId) {
        return applicationDeploymentMapper.selectByPrimaryKey(applicationDeploymentId);
    }


    /**
     * 这里的sql语句不使用左连接devops_cloud_server这张表，
     * 因为服务器多选之后使用find_in_set函数效率会非常低，
     * 在对比了使用代码进行匹配之后发现代码的效率在部署记录25000条，虚拟机记录1300条的情况下，
     * 查询时间约是左联查询的1/3，故选择用代码过滤。
     *
     * @param params 过滤参数
     * @return 部署记录列表
     */
    public List<ApplicationDeploymentDTO> selectApplicationDeployment(Map<String, Object> params) {
        CommonUtils.filterPermission(params);
        List<ApplicationDeploymentDTO> deployments = extApplicationDeploymentMapper.selectApplicationDeployments(params);

        List<DevopsCloudServer> cloudServers = devopsCloudServerMapper.selectByExample(null);
        Map<String, String> serverMap = cloudServers.stream().collect(Collectors.toMap(DevopsCloudServer::getId, DevopsCloudServer::getInstanceName));
        final StringBuilder sb = new StringBuilder();

        Stream<ApplicationDeploymentDTO> filterList = deployments.stream().filter(deployment ->
                !StringUtils.equalsIgnoreCase(deployment.getCloudServerId(), "all")
                        && StringUtils.isNotBlank(deployment.getCloudServerId()));
        filterList.forEach(deployment -> {
            if (deployment.getCloudServerId().contains(",")) {
                String[] servers = deployment.getCloudServerId().split(",");
                for (String server : servers) {
                    String serverName = serverMap.get(server);
                    sb.append(serverName)
                            .append(",");
                }
            } else {
                deployment.setCloudServerName(serverMap.get(deployment.getCloudServerId()));
            }

            if (sb.length() != 0) {
                deployment.setCloudServerName(sb.substring(0, sb.length() - 1));
                sb.setLength(0);
            }
        });
        Map<String, ApplicationDeploymentDTO> collect = deployments.stream().collect(Collectors.toMap(ApplicationDeploymentDTO::getId, e -> e, (k1, k2) -> k1));
        if (MapUtils.isNotEmpty(collect)) {
            ApplicationDeploymentEventLogExample example = new ApplicationDeploymentEventLogExample();
            example.createCriteria().andDeploymentLogIdIn(Lists.newArrayList(collect.keySet()));
            List<ApplicationDeploymentEventLog> applicationDeploymentEventLogs = applicationDeploymentEventLogMapper.selectByExample(example);
            Map<String, ApplicationDeploymentEventLog> logIdMapping = applicationDeploymentEventLogs.stream().collect(Collectors.toMap(ApplicationDeploymentEventLog::getDeploymentLogId, e -> e, (k1, k2) -> k1));
            Example exampleApiTest = new Example(DevopsApiTest.class);
            exampleApiTest.createCriteria().orIn("deployId", Lists.newArrayList(collect.keySet()));
            List<DevopsApiTest> tests = devopsApiTestMapper.selectByExample(exampleApiTest);
            Map<String, DevopsApiTest> testsMaps = tests.stream().collect(Collectors.toMap(DevopsApiTest::getDeployId, e -> e, (k1, k2) -> k1));
            deployments.stream().forEach(e -> {
                ApplicationDeploymentEventLog applicationDeploymentEventLog = logIdMapping.get(e.getId());
                if (applicationDeploymentEventLog != null) {
                    e.setTestLogId(applicationDeploymentEventLog.getId());
                    e.setTestStatus(applicationDeploymentEventLog.getStatus());
                }
                DevopsApiTest test = null;
                if (!testsMaps.containsKey(e.getId())) {
                    test = applicationSceneTestService.getApiTestByDeploy(e);
                } else {
                    test = testsMaps.get(e.getId());
                }
                if (test != null ) {
                    e.setApiTestId(test.getId());
                    e.setApiTestUrl(test.getReportUrl());
                    e.setApiTestPassRate(0);
                    try {
                        if (StringUtils.equalsIgnoreCase(test.getResult(), "success")) {
                            Long rate = (test.getTotalCount() > 0L)?(test.getPassCount()*100/test.getTotalCount()):0L;
                            e.setApiTestPassRate(rate.intValue());//百分制
                        }
                    } catch (Exception exp) {
                        LogUtil.error(String.format("计算通过率失败，error:%s", exp.getMessage()));
                    }
                    e.setTestReportUrl(test.getReportUrl());
                } else {
                    e.setTestReportUrl(null);
                }
            });
        }
        return deployments;
    }

    public List<ClusterDTO> getClustersByVersion(String versionId) {
        ApplicationVersion applicationVersion = applicationVersionService.getApplicationVersion(versionId);
        return clusterService.getClustersByAppversion(applicationVersion);
    }

    public void cleanApplicationDeployment() {
        ApplicationDeploymentExample applicationDeploymentExample = new ApplicationDeploymentExample();
        applicationDeploymentExample.createCriteria().andStatusNotIn(Arrays.asList(FAIL, SUCCESS, ERROR, TIMEOUT));
        List<ApplicationDeployment> applicationDeployments = applicationDeploymentMapper.selectByExample(applicationDeploymentExample);
        applicationDeployments.forEach(applicationDeployment -> {
            applicationDeployment.setEndTime(System.currentTimeMillis());
            applicationDeployment.setStatus(FAIL);
            applicationDeploymentLogService.cleanDeployLog(applicationDeployment.getId());
            applicationDeploymentMapper.updateByPrimaryKeySelective(applicationDeployment);
        });
    }

    public boolean checkDeploySettings() {
        SessionUser user = SessionUtils.getUser();
        String organizationId = user.getOrganizationId();
        List<UserRoleDTO> userRoleList = userCommonService.getUserRoleList(user.getId());
//        先判断是不是组织管理员
        for (UserRoleDTO role : userRoleList) {
            if (StringUtils.equalsIgnoreCase(organizationId, role.getId()) && role.getSwitchable()) {
                return true;
            }
        }
//        判断例外日期
        ApplicationDeploySettingsExample tmpObj1 = new ApplicationDeploySettingsExample();
        tmpObj1.createCriteria().andNameEqualTo(ApplicationDeploySettingsService.EXPECTED_DAYS);
        List<ApplicationDeploySettings> applicationDeploySettings = applicationDeploySettingsMapper.selectByExample(tmpObj1);
        LocalDate nowDate = new LocalDate(System.currentTimeMillis());
        for (ApplicationDeploySettings setting : applicationDeploySettings) {
            if (nowDate.equals(new LocalDate(setting.getDate()))) {
                return true;
            }
        }
//        判断时间
        DayOfWeek dayOfWeek = java.time.LocalDate.now().getDayOfWeek();
        ApplicationDeploySettingsExample tmpObj2 = new ApplicationDeploySettingsExample();
        tmpObj2.createCriteria().andNameEqualTo("weekday." + dayOfWeek.name());
        List<ApplicationDeploySettings> settingsList = applicationDeploySettingsMapper.selectByExample(tmpObj2);
        if (CollectionUtils.isNotEmpty(settingsList)) {
            ApplicationDeploySettings setting = settingsList.get(0);
            LocalTime start = setting.getStart().toLocalTime();
            LocalTime end = setting.getEnd().toLocalTime();
            LocalTime now = new Time(System.currentTimeMillis()).toLocalTime();
            return !now.isAfter(start) || !now.isBefore(end);
        }
        return true;
    }
}
