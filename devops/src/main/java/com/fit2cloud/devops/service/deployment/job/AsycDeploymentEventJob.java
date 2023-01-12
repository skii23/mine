package com.fit2cloud.devops.service.deployment.job;

import com.fit2cloud.ansible.AnsibleService;
import com.fit2cloud.ansible.model.TaskResult;
import com.fit2cloud.ansible.model.consts.CloudServerCredentialType;
import com.fit2cloud.ansible.model.request.ScriptRunRequest;
import com.fit2cloud.commons.server.base.domain.CloudServer;
import com.fit2cloud.commons.server.base.domain.CloudServerCredential;
import com.fit2cloud.commons.server.exception.F2CException;
import com.fit2cloud.commons.utils.BeanUtils;
import com.fit2cloud.devops.base.domain.ApplicationDeploymentEventLog;
import com.fit2cloud.devops.base.domain.ApplicationVersion;
import com.fit2cloud.devops.base.domain.DevopsCloudServer;
import com.fit2cloud.devops.base.domain.Proxy;
import com.fit2cloud.devops.common.consts.StatusConstants;
import com.fit2cloud.devops.common.model.Appspec;
import com.fit2cloud.devops.common.model.Artifact;
import com.fit2cloud.devops.common.util.DevopsLogUtil;
import com.fit2cloud.devops.common.util.OsUtils;
import com.fit2cloud.devops.service.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AsycDeploymentEventJob {

    @Resource
    private ApplicationDeploymentEventLogService applicationDeploymentEventLogService;
    @Resource
    private InitDeployService initDeployService;
    @Resource
    private InstallService installService;
    @Resource
    private AnsibleService ansibleService;
    @Resource
    private ApplicationVersionService applicationVersionService;
    @Resource
    private DevopsCloudServerCommonService devopsCloudServerCommonService;
    @Resource
    private CredentialService credentialService;
    @Resource
    private BeforeExcuteScriptService beforeExcuteScriptService;
    @Resource
    private ProxyService proxyService;

    public static ConcurrentHashMap<String, Appspec> appspecs = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, String> downloadPath = new ConcurrentHashMap<>();

    public String initEvn(ApplicationDeploymentEventLog applicationDeploymentEventLog) {
        ApplicationVersion applicationVersion = applicationVersionService.getApplicationVersion(applicationDeploymentEventLog.getApplicationVersionId());
        DevopsCloudServer devopsCloudServer = devopsCloudServerCommonService.get(applicationDeploymentEventLog.getCloudServerId());
        CloudServer cloudServer = new CloudServer();
        BeanUtils.copyBean(cloudServer,devopsCloudServer);
        applicationDeploymentEventLog.setStartTime(System.currentTimeMillis());
        applicationDeploymentEventLog.setStatus(StatusConstants.RUNNING);
        applicationDeploymentEventLogService.saveApplicationDeploymentEventLog(applicationDeploymentEventLog);
        StringBuffer buffer = new StringBuffer();
        buffer.append(DevopsLogUtil.info(applicationDeploymentEventLog.getEventName(), "开始初始化环境"));
        applicationDeploymentEventLog.setStdout(buffer.toString());
        applicationDeploymentEventLogService.saveApplicationDeploymentEventLog(applicationDeploymentEventLog);
        try {
            CloudServerCredential cloudServerCredential = credentialService.findCloudServerCredential(cloudServer.getId());
            Artifact artifact = getArtifact(applicationVersion);

            initDeployService.checkPython(cloudServer, cloudServerCredential, applicationDeploymentEventLog, buffer);
            initDeployService.initEnv(artifact, cloudServer, cloudServerCredential, applicationDeploymentEventLog, buffer);
            initDeployService.loadAppspec(artifact, cloudServer, cloudServerCredential, applicationDeploymentEventLog, buffer);

        } catch (Exception e) {
            handleException(applicationDeploymentEventLog, buffer, e);
        }
        buffer.append(DevopsLogUtil.info(applicationDeploymentEventLog.getEventName(), "初始化环境完毕！"));
        String stdout = buffer.toString();
        applicationDeploymentEventLog.setStdout(stdout);
        applicationDeploymentEventLog.setStatus(StatusConstants.SUCCESS);
        applicationDeploymentEventLog.setEndTime(System.currentTimeMillis());
        applicationDeploymentEventLogService.saveApplicationDeploymentEventLog(applicationDeploymentEventLog);
        return buffer.toString();
    }

    public String install(ApplicationDeploymentEventLog applicationDeploymentEventLog) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(DevopsLogUtil.info(applicationDeploymentEventLog.getEventName(), "开始部署代码"));
        applicationDeploymentEventLog.setStdout(buffer.toString());
        DevopsCloudServer devopsCloudServer = devopsCloudServerCommonService.get(applicationDeploymentEventLog.getCloudServerId());
        CloudServer cloudServer = new CloudServer();
        BeanUtils.copyBean(cloudServer,devopsCloudServer);
        applicationDeploymentEventLog.setStatus(StatusConstants.RUNNING);
        applicationDeploymentEventLog.setStartTime(System.currentTimeMillis());
        applicationDeploymentEventLogService.saveApplicationDeploymentEventLog(applicationDeploymentEventLog);
        try {
            CloudServerCredential cloudServerCredential = credentialService.findCloudServerCredential(cloudServer.getId());
            installService.install(applicationDeploymentEventLog, cloudServer, cloudServerCredential, buffer);
        } catch (Exception e) {
            handleException(applicationDeploymentEventLog, buffer, e);
        }
        buffer.append(DevopsLogUtil.info(applicationDeploymentEventLog.getEventName(), "代码部署完毕"));
        String stdout = buffer.toString();
        applicationDeploymentEventLog.setStdout(stdout);
        applicationDeploymentEventLog.setStatus(StatusConstants.SUCCESS);
        applicationDeploymentEventLog.setEndTime(System.currentTimeMillis());
        applicationDeploymentEventLogService.saveApplicationDeploymentEventLog(applicationDeploymentEventLog);
        return buffer.toString();
    }

    public String executeScript(ApplicationDeploymentEventLog applicationDeploymentEventLog) {
        applicationDeploymentEventLog.setStartTime(System.currentTimeMillis());
        applicationDeploymentEventLog.setStatus(StatusConstants.RUNNING);
        applicationDeploymentEventLogService.saveApplicationDeploymentEventLog(applicationDeploymentEventLog);
        DevopsCloudServer devopsCloudServer = devopsCloudServerCommonService.get(applicationDeploymentEventLog.getCloudServerId());
        CloudServer cloudServer = new CloudServer();
        BeanUtils.copyBean(cloudServer,devopsCloudServer);
        String paramsHeader = beforeExcuteScriptService.generateExportScript(cloudServer.getId());
        Appspec appspec = appspecs.get(applicationDeploymentEventLog.getDeploymentLogId());
        String src = downloadPath.get(applicationDeploymentEventLog.getDeploymentLogId()) + "/";
        StringBuffer buffer = new StringBuffer();
        buffer.append(DevopsLogUtil.info(applicationDeploymentEventLog.getEventName(), "开始执行: " + applicationDeploymentEventLog.getEventName()));
        applicationDeploymentEventLog.setStdout(buffer.toString());
        applicationDeploymentEventLogService.saveApplicationDeploymentEventLog(applicationDeploymentEventLog);
        try {
            appspec.getHooks().forEach((s, hooks) -> {
                if (s.equalsIgnoreCase(applicationDeploymentEventLog.getEventName())) {
                    Map hook = hooks.get(0);
                    String location = hook.get("location") + "";
                    Integer timeout = Integer.valueOf((String) hook.get("timeout"));
                    String runas = hook.get("runas") + "";

                    CloudServerCredential cloudServerCredential = credentialService.findCloudServerCredential(cloudServer.getId());
                    String username = cloudServerCredential.getUsername();
                    String credential = StringUtils.isNotBlank(cloudServerCredential.getPassword()) ? cloudServerCredential.getPassword() : cloudServerCredential.getSecretKey();
                    CloudServerCredentialType cloudServerCredentialType = StringUtils.isNotBlank(cloudServerCredential.getPassword()) ? CloudServerCredentialType.PASSWORD : CloudServerCredentialType.KEY;
                    TaskResult scriptResult = null;
                    try {
                        ScriptRunRequest scriptRunRequest = new ScriptRunRequest();
                        scriptRunRequest.withUsername(username)
                                .withIp(cloudServer.getManagementIp())
                                .withPort(cloudServer.getManagementPort())
                                .withCredential(credential)
                                .withCredentialType(cloudServerCredentialType)
                                .withHeader(paramsHeader)
                                .withContent(src + location)
                                .withTimeout(Long.valueOf(timeout));

                        if (org.apache.commons.lang.StringUtils.containsIgnoreCase(cloudServer.getOs(), "windows")) {
                            OsUtils.mixWinParams(scriptRunRequest);
                            OsUtils.mixWinExecCmd(scriptRunRequest);
                        } else {
                            OsUtils.mixLinuxParams(scriptRunRequest, timeout, runas, username, credential);
                        }

                        Proxy proxy = proxyService.getProxyByCloudServerId(cloudServer.getId());
                        if (proxy != null) {
                            scriptRunRequest.setProxy(proxy.getIp(), proxy.getPort(), proxy.getUsername(), proxy.getPassword());
                        }

                        scriptResult = ansibleService.runScriptAndGetResult(scriptRunRequest);

                    } catch (Exception e) {
                        F2CException.throwException(e);
                    }
                    if (scriptResult.getResult().isSuccess()) {
                        buffer.append(DevopsLogUtil.info(applicationDeploymentEventLog.getEventName(), "执行" + src + location + "成功"));
                        buffer.append(ansibleService.getStdOut(scriptResult));
                        buffer.append("\n");
                    } else {
                        buffer.append(DevopsLogUtil.error(applicationDeploymentEventLog.getEventName(), "执行" + src + location + "失败"));
                        buffer.append(ansibleService.getStdOut(scriptResult));
                        buffer.append(ansibleService.getMsg(scriptResult));
                        buffer.append("\n");
                        F2CException.throwException(ansibleService.getStdOut(scriptResult));
                    }
                }
            });
        } catch (Exception e) {
            handleException(applicationDeploymentEventLog, buffer, e);
        }

        buffer.append(DevopsLogUtil.info(applicationDeploymentEventLog.getEventName(), "执行" + applicationDeploymentEventLog.getEventName() + "完毕！"));

        String stdout = buffer.toString();
        applicationDeploymentEventLog.setStdout(stdout);
        applicationDeploymentEventLog.setEndTime(System.currentTimeMillis());
        applicationDeploymentEventLog.setStatus(StatusConstants.SUCCESS);
        applicationDeploymentEventLogService.saveApplicationDeploymentEventLog(applicationDeploymentEventLog);
        return stdout;
    }

    public String cleanEvn(ApplicationDeploymentEventLog applicationDeploymentEventLog) {
        ApplicationVersion applicationVersion = applicationVersionService.getApplicationVersion(applicationDeploymentEventLog.getApplicationVersionId());
        DevopsCloudServer devopsCloudServer = devopsCloudServerCommonService.get(applicationDeploymentEventLog.getCloudServerId());
        CloudServer cloudServer = new CloudServer();
        BeanUtils.copyBean(cloudServer,devopsCloudServer);
        StringBuffer buffer = new StringBuffer();
        buffer.append(applicationDeploymentEventLog.getStdout()).append("\n");
        applicationDeploymentEventLog.setStdout("");
        try {
            CloudServerCredential cloudServerCredential = credentialService.findCloudServerCredential(cloudServer.getId());
            Artifact artifact = getArtifact(applicationVersion);
            initDeployService.cleanEnv(artifact, cloudServer, cloudServerCredential, applicationDeploymentEventLog, buffer);
        } catch (Exception e) {
            try {
                handleException(applicationDeploymentEventLog, buffer, e);
            } catch (Exception ex) {
                //ignore
            }
        }
        buffer.append(DevopsLogUtil.info(applicationDeploymentEventLog.getEventName(), "清理环境完毕！"));
        String stdout = buffer.toString();
        applicationDeploymentEventLog.setStdout(stdout);
        applicationDeploymentEventLog.setEndTime(System.currentTimeMillis());
        applicationDeploymentEventLogService.saveApplicationDeploymentEventLog(applicationDeploymentEventLog);
        return buffer.toString();
    }

    private void handleException(ApplicationDeploymentEventLog applicationDeploymentEventLog, StringBuffer buffer, Exception e) {
        applicationDeploymentEventLog.setStatus(StatusConstants.FAIL);
        buffer.append(DevopsLogUtil.error(applicationDeploymentEventLog.getEventName(), "发生错误" + "message:\n" + e.getMessage()));

        String stdout = buffer.toString();
        applicationDeploymentEventLog.setStdout(stdout);
        applicationDeploymentEventLog.setEndTime(System.currentTimeMillis());
        applicationDeploymentEventLogService.saveApplicationDeploymentEventLog(applicationDeploymentEventLog);
        F2CException.throwException(e);
    }

    private Artifact getArtifact(ApplicationVersion applicationVersion) {
        Artifact artifact = new Artifact();
        artifact.setUrl(applicationVersionService.getArtifactUrl(applicationVersion));
        String location = applicationVersion.getLocation();
        String artifactName = location.substring(location.lastIndexOf("/") + 1);
        artifact.setName(artifactName);
        return artifact;
    }

}
