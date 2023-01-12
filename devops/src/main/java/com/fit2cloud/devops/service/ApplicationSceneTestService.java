package com.fit2cloud.devops.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.commons.utils.CommonThreadPool;
import com.fit2cloud.commons.utils.LogUtil;
import com.fit2cloud.commons.utils.UUIDUtil;
import com.fit2cloud.devops.base.domain.*;
import com.fit2cloud.devops.base.mapper.ApplicationDeploymentEventLogMapper;
import com.fit2cloud.devops.base.mapper.ApplicationDeploymentMapper;
import com.fit2cloud.devops.base.mapper.DevopsApiTestMapper;
import com.fit2cloud.devops.common.consts.CodeDeploymentSteps;
import com.fit2cloud.devops.common.consts.StatusConstants;
import com.fit2cloud.devops.common.model.PipelineContext;
import com.fit2cloud.devops.common.util.DevopsLogUtil;
import com.fit2cloud.devops.service.openapi.XOceanOpenApiService;
import com.fit2cloud.devops.service.openapi.model.LastReport;
import com.fit2cloud.devops.service.openapi.model.SceneRunRequest;
import com.fit2cloud.devops.service.openapi.model.SceneRunResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自动化测试API平台
 *
 * @author caiwzh
 * @date 2022/9/13
 */
@Service
public class ApplicationSceneTestService {

    private static final long DEFAULT_TIME_OUT = TimeUnit.MINUTES.toSeconds(30);

    private static final long DEFAULT_FREQ_SEC = TimeUnit.SECONDS.toSeconds(3);

    public static final String EVENT_NAME = "ApplicationTest";
    public static final String HAVE_MOVE_TO_TABLE = "move";
    public static final String DISABLE_API_TEST = "disable api test";

    @Resource
    private ApplicationDeploymentMapper applicationDeploymentMapper;

    @Resource
    private DevopsApiTestMapper devopsApiTestMapper;

    @Resource
    private ApplicationDeploymentEventLogMapper applicationDeploymentEventLogMapper;

    @Resource
    private XOceanOpenApiService xOceanOpenApiService;

    @Resource(name = "appTestPool")
    private CommonThreadPool appTestPool;

    @Resource
    private ApplicationPipelineSevice applicationPipelineSevice;

    /**
     * 异步测试
     *
     * @param pipelineContext
     */
    public void asyncDoApiTest(PipelineContext pipelineContext) {
        appTestPool.addTask(() -> doApiTest(pipelineContext));
    }

    public void pollingDeployComplete(PipelineContext pipelineContext) {
        appTestPool.addTask(() -> waitForDeployComplete(pipelineContext));
    }

    /**
     * 同步阻塞api测试
     * 必须等待部署完成 才能执行自动化测试
     * @param pipelineContext
     */
    public void doApiTest(PipelineContext pipelineContext) {
        String applicationDeploymentId = pipelineContext.getApplicationDeploymentId();
        Application application = pipelineContext.getApplication();
        ApplicationVersion applicationVersion = pipelineContext.getApplicationVersion();
        JSONObject publish = pipelineContext.getPublish();
        DevopsApiTest apiTest = new DevopsApiTest();
        apiTest.setId(UUIDUtil.newUUID());
        apiTest.setDeployId(applicationDeploymentId);
        apiTest.setStartTime(System.currentTimeMillis());
        apiTest.setProductId(application.getTestProdId());
        apiTest.setPlanId(application.getTestPlanId());
        apiTest.setEnv(application.getTestEvn());
        apiTest.setOperator(application.getName());
        try {
            if (waitForDeployComplete(pipelineContext)) {
                SceneRunRequest runRequest = new SceneRunRequest();
                runRequest.setProductId(application.getTestProdId());
                runRequest.setPlanId(application.getTestPlanId());
                runRequest.setEnv(application.getTestEvn());
                runRequest.setOnlyUnPassed(publish.getBooleanValue("onlyUnPassed"));
                runRequest.setNeedReport(publish.getBooleanValue("needReport"));
                runRequest.setOperator(application.getName());
                runRequest.setBiz(publish.getString("biz"));
                runRequest.setUuid(apiTest.getId());
                JSONObject runInfo = xOceanOpenApiService.sceneRun(runRequest);
                String runId = runInfo.getString("runId");
                apiTest.setRunId(runId);
                JSONObject param = runInfo.getJSONObject("test_plan_env");
                long start = apiTest.getStartTime();
                long testPollingTimeoutSec = publish.getLongValue("testPollingTimeoutSec") == 0L ? DEFAULT_TIME_OUT : publish.getLongValue("testPollingTimeoutSec");
                long testPollingFreqSec = publish.getLongValue("testPollingFreqSec") == 0L ? DEFAULT_FREQ_SEC : publish.getLongValue("testPollingFreqSec");
                //最长30s 查询一次
                testPollingFreqSec = testPollingFreqSec > 30 ? 30 : testPollingFreqSec;
                StringBuffer buffer = new StringBuffer();
                buffer.append(DevopsLogUtil.info(EVENT_NAME, "开始自动化测试 runId: " + runId + ",SceneRunRequest: " + JSON.toJSONString(runRequest,true)));
                ApplicationDeploymentEventLog applicationDeploymentEventLog = insertEventLog(false, applicationDeploymentId, buffer.toString());
                ApplicationDeploymentEventLog newLog = new ApplicationDeploymentEventLog();
                newLog.setId(applicationDeploymentEventLog.getId());
                apiTest.setId(applicationDeploymentEventLog.getId());
                ApplicationDeployment applicationDeployment = new ApplicationDeployment();
                applicationDeployment.setId(applicationDeploymentId);
                applicationDeployment.setTestReportUrl(HAVE_MOVE_TO_TABLE);
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        SceneRunResult sceneRunResult = xOceanOpenApiService.getSceneRunResult(runId);
                        if (StringUtils.equals(sceneRunResult.getStatus(), "FINISHED")) {
                            //stop 接口
                            xOceanOpenApiService.stop(param);
                            if (runRequest.getNeedReport()) {
                                LastReport lastReport = xOceanOpenApiService.getLastReport(runRequest.getProductId(), runRequest.getPlanId());
                                buffer.append(DevopsLogUtil.info(applicationDeploymentEventLog.getEventName(), "生成测试报告：" + lastReport.getName() + ",url: " + lastReport.getReportUrl()));
                                //更新测试报告url
                                apiTest.setReportUrl(lastReport.getReportUrl());
                            }
                            apiTest.setPassCount(Long.valueOf(sceneRunResult.getPassedCount()));
                            apiTest.setTotalCount(Long.valueOf(sceneRunResult.getTotalCount()));
                            apiTest.setFailCount(Long.valueOf(sceneRunResult.getFailedCount()));
                            apiTest.setUntestedCount(Long.valueOf(sceneRunResult.getUntestedCount()));
                            apiTest.setResult("success");
                            String msg = String.format("应用【%s】,版本【%s】自动化测试完成,测试结果：【%s】", application.getName(), applicationVersion.getName(), JSON.toJSONString(sceneRunResult, true));
                            buffer.append(DevopsLogUtil.info(applicationDeploymentEventLog.getEventName(), msg));
                            LogUtil.info(msg);
                            newLog.setStatus(StatusConstants.SUCCESS);
                            break;
                        } else {
                            String msg = String.format("应用【%s】,版本【%s】自动化测试中...,执行成功率【%s】%%", application.getName(), applicationVersion.getName(), sceneRunResult.getPassRate() == null ? 0 : sceneRunResult.getPassRate());
                            buffer.append(DevopsLogUtil.info(applicationDeploymentEventLog.getEventName(), msg));
                            LogUtil.info(msg);
                            //判断是否超时
                            if (System.currentTimeMillis() - start > testPollingTimeoutSec * 1000) {
                                newLog.setStatus(StatusConstants.TIMEOUT);
                                msg = String.format("应用【%s】,版本【%s】自动化测试超时", application.getName(), applicationVersion.getName());
                                buffer.append(DevopsLogUtil.info(applicationDeploymentEventLog.getEventName(), msg));
                                LogUtil.info(msg);
                                xOceanOpenApiService.stop(param);
                                apiTest.setResult("timeout");
                                break;
                            }
                        }
                        TimeUnit.SECONDS.sleep(testPollingFreqSec);
                    } catch (Exception e) {
                        buffer.append(DevopsLogUtil.info(applicationDeploymentEventLog.getEventName(), e.getMessage()));
                        newLog.setStatus(StatusConstants.ERROR);
                        LogUtil.warn("doApiTest 异常", e);
                        apiTest.setResult("abort");
                    } finally {
                        //更新输出内容
                        newLog.setStdout(buffer.toString());
                        applicationDeploymentEventLogMapper.updateByPrimaryKeySelective(newLog);
                    }
                }
                buffer.append(DevopsLogUtil.info(applicationDeploymentEventLog.getEventName(), "自动化测试完毕"));
                newLog.setStdout(buffer.toString());
                apiTest.setEndTime(System.currentTimeMillis());
                newLog.setEndTime(apiTest.getEndTime());
                applicationDeploymentEventLogMapper.updateByPrimaryKeySelective(newLog);
                devopsApiTestMapper.insert(apiTest);
                applicationDeploymentMapper.updateByPrimaryKeySelective(applicationDeployment);
            } else {
                String msg = String.format("应用【%s】,版本【%s】未能部署成功，自动化测试启动失败", application.getName(), applicationVersion.getName());
                insertEventLog(true, applicationDeploymentId, DevopsLogUtil.info(EVENT_NAME, msg));
                LogUtil.info(msg);
            }

            //插入测试结果
        } catch (Exception e) {
            LogUtil.error("启动自动化测试失败", e);
            insertEventLog(true, applicationDeploymentId, DevopsLogUtil.info(EVENT_NAME, e.getMessage()));
        }finally {
            pipelineContext.setDevopsApiTest(apiTest);
            applicationPipelineSevice.onApiTestComplete(pipelineContext);
        }
    }

    public DevopsApiTest getApiTestByDeploy(ApplicationDeployment deploy) {
        if (deploy == null) {
            LogUtil.error("deploy is null");
            return null;
        }
        if (StringUtils.equalsIgnoreCase(deploy.getTestReportUrl(), DISABLE_API_TEST)) {
            return null;
        }
        if (StringUtils.equalsIgnoreCase(deploy.getTestReportUrl(), HAVE_MOVE_TO_TABLE)) {
            Example exampleApiTest = new Example(DevopsApiTest.class);
            exampleApiTest.createCriteria().andEqualTo("deployId", deploy.getId());
            List<DevopsApiTest> tests = devopsApiTestMapper.selectByExample(exampleApiTest);
            if (tests == null || tests.isEmpty()) {
                LogUtil.error("can't find api test data with:%s", deploy.getId());
                deploy.setTestReportUrl(DISABLE_API_TEST);
                applicationDeploymentMapper.updateByPrimaryKeySelective(deploy);
                return null;
            }
            return tests.get(0);
        }
        return saveApiTestWithDeploy(deploy);
    }

    public String getApiTesReporterUrltByDeploy(ApplicationDeployment deploy) {
        DevopsApiTest test = getApiTestByDeploy(deploy);
        if (test == null) {
            return null;
        }
        return test.getReportUrl();
    }

    public DevopsApiTest saveApiTestWithDeploy(ApplicationDeployment deploy) {
        if (deploy == null) {
            LogUtil.error("deploy is null");
            return null;
        }
        if (StringUtils.equalsIgnoreCase(deploy.getTestReportUrl(), DISABLE_API_TEST)) {
            return null;
        }
        if (StringUtils.equalsIgnoreCase(deploy.getTestReportUrl(), HAVE_MOVE_TO_TABLE)) {
            return null;
        }
        deploy.setTestReportUrl(DISABLE_API_TEST);
        ApplicationDeploymentEventLogExample example = new ApplicationDeploymentEventLogExample();
        example.createCriteria().andDeploymentLogIdEqualTo(deploy.getId()).andEventNameEqualTo(EVENT_NAME);
        List<ApplicationDeploymentEventLog> evenLogs = applicationDeploymentEventLogMapper.selectByExampleWithBLOBs(example);
        if (evenLogs == null || evenLogs.isEmpty()) {
            LogUtil.error("can't find event log with:", deploy.getId());
            applicationDeploymentMapper.updateByPrimaryKeySelective(deploy);
            return null;
        }
        DevopsApiTest testData = null;
        for (ApplicationDeploymentEventLog log: evenLogs) {
            if (!StringUtils.equals(log.getEventName(), EVENT_NAME)) {
                continue;
            }
            LogUtil.warn(String.format("开始将部署[%s]日志信息导出为api测试数据", deploy.getId()));
            testData = devopsApiTestMapper.selectByPrimaryKey(log.getId());
            if (testData == null) {
                testData = new DevopsApiTest();
                testData.setId(log.getId());
                testData.setStartTime(log.getStartTime());
                testData.setEndTime(log.getEndTime());
                testData.setResult(log.getStatus());
                testData.setDeployId(deploy.getId());
                Map<String, String> params = getKeyValueFormLogText(log.getStdout());
                testData.setProductId(params.getOrDefault("productId", ""));
                testData.setPlanId(params.getOrDefault("planId", ""));
                testData.setEnv(params.getOrDefault("env", ""));
                testData.setOperator(params.getOrDefault("operator", ""));
                testData.setReportUrl(params.getOrDefault("reportUrl", ""));
                testData.setTotalCount(Long.valueOf(params.getOrDefault("totalCount", "0")));
                testData.setFailCount(Long.valueOf(params.getOrDefault("failedCount", "0")));
                testData.setPassCount(Long.valueOf(params.getOrDefault("passedCount", "0")));
                testData.setUntestedCount(Long.valueOf(params.getOrDefault("untestedCount", "0")));
                devopsApiTestMapper.insert(testData);
                deploy.setTestReportUrl(HAVE_MOVE_TO_TABLE);
            }
            break;
        }
        applicationDeploymentMapper.updateByPrimaryKeySelective(deploy);
        return testData;
    }

    private Map<String, String> getKeyValueFormLogText(String text) {
        Map<String, String> out = new HashMap<>();
        if (StringUtils.isBlank(text)) {
            return out;
        }
        Pattern p = Pattern.compile("\"(.+)\":\"(.+)\"");
        Matcher match = p.matcher(text);
        while(match.find()) {
            try {
                out.put(match.group(1), match.group(2));
            } catch (Exception e) {
                LogUtil.error("find match fail:", match.group());
            }
        }
        return out;
    }
    /**
     * 等待部署完成
     *
     * @param pipelineContext
     * @return
     */
    public boolean waitForDeployComplete(PipelineContext pipelineContext) {
        String applicationDeploymentId = pipelineContext.getApplicationDeploymentId();
        Application application = pipelineContext.getApplication();
        ApplicationVersion applicationVersion = pipelineContext.getApplicationVersion();
        JSONObject publish = pipelineContext.getPublish();

        AtomicBoolean deployFlag = new AtomicBoolean(false);
        long start = System.currentTimeMillis();
        //部署超时时间
        long pollingTimeoutSec = publish.getLongValue("pollingTimeoutSec") == 0L ? DEFAULT_TIME_OUT : publish.getLongValue("pollingTimeoutSec");
        long pollingFreqSec = publish.getLongValue("pollingFreqSec") == 0L ? DEFAULT_FREQ_SEC : publish.getLongValue("pollingFreqSec");
        //必须等待部署完成 才能执行自动化测试
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ApplicationDeployment deployment = applicationDeploymentMapper.selectByPrimaryKey(applicationDeploymentId);
                //判断状态
                if (deployment.getStatus().equalsIgnoreCase("success") || deployment.getStatus().equalsIgnoreCase("fail")) {
                    LogUtil.info(String.format("应用【%s】,版本【%s】部署完成！", application.getName(), applicationVersion.getName()));
                    if (deployment.getStatus().equalsIgnoreCase("success")) {
                        deployFlag.set(true);
                        LogUtil.info(String.format("应用【%s】,版本【%s】部署结果: 成功", application.getName(), applicationVersion.getName()));
                    } else {
                        LogUtil.info(String.format("应用【%s】,版本【%s】部署结果: 失败", application.getName(), applicationVersion.getName()));
                    }
                    break;
                } else {
                    LogUtil.info(String.format("应用【%s】,版本【%s】部署任务运行中...", application.getName(), applicationVersion.getName()));
                }
                //判断是否超时
                if (System.currentTimeMillis() - start > pollingTimeoutSec * 1000) {
                    LogUtil.info(String.format("应用【%s】,版本【%s】部署任务超时", application.getName(), applicationVersion.getName()));
                    break;
                }
                TimeUnit.SECONDS.sleep(pollingFreqSec);
            } catch (Exception e) {
                LogUtil.warn("waitForDeployComplete 异常", e);
            }
        }
        applicationPipelineSevice.onDeployComplete(pipelineContext);
        return deployFlag.get();
    }

    /**
     * devops_application_deployment_event_log插入部署id的ApplicationTest事件
     *
     * @param fail
     * @param applicationDeploymentId
     * @param msg
     * @return
     */
    private ApplicationDeploymentEventLog insertEventLog(boolean fail, String applicationDeploymentId, String msg) {
        //ApplicationDeploymentLogExample example = new ApplicationDeploymentLogExample();
        //example.createCriteria().andDeploymentIdEqualTo(applicationDeploymentId);
        //List<ApplicationDeploymentLog> applicationDeploymentLogs = applicationDeploymentLogMapper.selectByExample(example);
        //ApplicationDeploymentLog applicationDeploymentLog = applicationDeploymentLogs.get(0);
        ApplicationDeployment applicationDeployment = applicationDeploymentMapper.selectByPrimaryKey(applicationDeploymentId);
        ApplicationDeploymentEventLog applicationDeploymentEventLog = new ApplicationDeploymentEventLog();
        applicationDeploymentEventLog.setId(UUIDUtil.newUUID());
        applicationDeploymentEventLog.setDeploymentLogId(applicationDeployment.getId());
        applicationDeploymentEventLog.setEventName(EVENT_NAME);
        applicationDeploymentEventLog.setStartTime(System.currentTimeMillis());
        applicationDeploymentEventLog.setEndTime(System.currentTimeMillis());
        applicationDeploymentEventLog.setStatus(fail ? StatusConstants.FAIL : StatusConstants.RUNNING);
        applicationDeploymentEventLog.setApplicationVersionId(applicationDeployment.getApplicationVersionId());
        applicationDeploymentEventLog.setCloudServerId(applicationDeployment.getCloudServerId());
        applicationDeploymentEventLog.setsOrder(CodeDeploymentSteps.supportEventTypes.size() + 1);
        applicationDeploymentEventLog.setStdout(msg);
        applicationDeploymentEventLogMapper.insert(applicationDeploymentEventLog);
        return applicationDeploymentEventLog;
    }
}
