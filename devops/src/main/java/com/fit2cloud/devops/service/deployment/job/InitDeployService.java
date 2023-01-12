package com.fit2cloud.devops.service.deployment.job;

import com.esotericsoftware.yamlbeans.YamlReader;
import com.fit2cloud.ansible.AnsibleService;
import com.fit2cloud.ansible.model.TaskResult;
import com.fit2cloud.ansible.model.consts.CloudServerCredentialType;
import com.fit2cloud.ansible.model.request.ScriptRunRequest;
import com.fit2cloud.commons.server.base.domain.CloudServer;
import com.fit2cloud.commons.server.base.domain.CloudServerCredential;
import com.fit2cloud.commons.server.exception.F2CException;
import com.fit2cloud.devops.base.domain.ApplicationDeploymentEventLog;
import com.fit2cloud.devops.base.domain.Proxy;
import com.fit2cloud.devops.common.model.Appspec;
import com.fit2cloud.devops.common.model.Artifact;
import com.fit2cloud.devops.common.util.DevopsLogUtil;
import com.fit2cloud.devops.common.util.OsUtils;
import com.fit2cloud.devops.service.ApplicationDeploymentEventLogService;
import com.fit2cloud.devops.service.ProxyService;
import com.fit2cloud.devops.service.deployment.scripts.CodeDeployScripts;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class InitDeployService {

    @Resource
    private AnsibleService ansibleClient;
    @Resource
    private ApplicationDeploymentEventLogService applicationDeploymentEventLogService;
    @Resource
    private CodeDeployScripts codeDeployScripts;
    @Resource
    private ProxyService proxyService;

    public void checkPython(CloudServer cloudServer, CloudServerCredential cloudServerCredential, ApplicationDeploymentEventLog applicationDeploymentEventLog, StringBuffer buffer) throws Exception {
        String credential = org.apache.commons.lang.StringUtils.isBlank(cloudServerCredential.getPassword()) ? cloudServerCredential.getSecretKey() : cloudServerCredential.getPassword();
        CloudServerCredentialType cloudServerCredentialType = org.apache.commons.lang.StringUtils.isBlank(cloudServerCredential.getPassword()) ? CloudServerCredentialType.KEY : CloudServerCredentialType.PASSWORD;
        String scriptContent = codeDeployScripts.getCheckPythonScript();

        ScriptRunRequest scriptRunRequest = new ScriptRunRequest()
                .withUsername(cloudServerCredential.getUsername())
                .withCredentialType(cloudServerCredentialType)
                .withIp(cloudServer.getManagementIp())
                .withPort(cloudServer.getManagementPort())
                .withContent(scriptContent)
                .withCredential(credential)
                .withCredentialType(cloudServerCredentialType);
        if (org.apache.commons.lang.StringUtils.containsIgnoreCase(cloudServer.getOs(), "windows")) {
            OsUtils.mixWinParams(scriptRunRequest);
        }


        Proxy proxy = proxyService.getProxyByCloudServerId(cloudServer.getId());
        if (proxy != null) {
            scriptRunRequest.setProxy(proxy.getIp(), proxy.getPort(), proxy.getUsername(), proxy.getPassword());
        }


        buffer.append(DevopsLogUtil.info(applicationDeploymentEventLog.getEventName(), "检查Python版本..."));
        applicationDeploymentEventLog.setStdout(buffer.toString());
        applicationDeploymentEventLogService.saveApplicationDeploymentEventLog(applicationDeploymentEventLog);

        TaskResult taskResult = ansibleClient.runScriptAndGetResult(scriptRunRequest);
        handleException(taskResult);
        buffer.append(DevopsLogUtil.info(applicationDeploymentEventLog.getEventName(), ansibleClient.getStdOut(taskResult)));
        applicationDeploymentEventLog.setStdout(buffer.toString());
        applicationDeploymentEventLogService.saveApplicationDeploymentEventLog(applicationDeploymentEventLog);
    }


    public void initEnv(Artifact artifact, CloudServer cloudServer, CloudServerCredential cloudServerCredential, ApplicationDeploymentEventLog applicationDeploymentEventLog, StringBuffer buffer) throws Exception {
        String credential = org.apache.commons.lang.StringUtils.isBlank(cloudServerCredential.getPassword()) ? cloudServerCredential.getSecretKey() : cloudServerCredential.getPassword();
        CloudServerCredentialType cloudServerCredentialType = org.apache.commons.lang.StringUtils.isBlank(cloudServerCredential.getPassword()) ? CloudServerCredentialType.KEY : CloudServerCredentialType.PASSWORD;
        ScriptRunRequest scriptRunRequest = new ScriptRunRequest();
        String scriptContent = codeDeployScripts.getInitScript(artifact.getName(), artifact.getUrl(), cloudServer.getOs());
        scriptRunRequest
                .withPort(cloudServer.getManagementPort())
                .withIp(cloudServer.getManagementIp())
                .withUsername(cloudServerCredential.getUsername())
                .withCredential(credential)
                .withCredentialType(cloudServerCredentialType);

        Proxy proxy = proxyService.getProxyByCloudServerId(cloudServer.getId());
        if (proxy != null) {
            scriptRunRequest.setProxy(proxy.getIp(), proxy.getPort(), proxy.getUsername(), proxy.getPassword());
        }


        if (org.apache.commons.lang.StringUtils.containsIgnoreCase(cloudServer.getOs(), "windows")) {
            OsUtils.mixWinParams(scriptRunRequest);
            scriptRunRequest.withExecutePath("powershell")
                    .withContent(OsUtils.winPython(scriptContent));
        } else {
            scriptRunRequest.withExecutePath("/usr/bin/python")
                    .withContent(scriptContent);
        }


        buffer.append(DevopsLogUtil.info(applicationDeploymentEventLog.getEventName(), "初始化FIT2CLOUD代码部署环境..."));
        applicationDeploymentEventLog.setStdout(buffer.toString());
        applicationDeploymentEventLogService.saveApplicationDeploymentEventLog(applicationDeploymentEventLog);
        TaskResult taskResult = ansibleClient.runScriptAndGetResult(scriptRunRequest);
        handleException(taskResult);
        buffer.append(DevopsLogUtil.info(applicationDeploymentEventLog.getEventName(), ansibleClient.getStdOut(taskResult)));
        applicationDeploymentEventLog.setStdout(buffer.toString());
        applicationDeploymentEventLogService.saveApplicationDeploymentEventLog(applicationDeploymentEventLog);
    }

    public void loadAppspec(Artifact artifact, CloudServer cloudServer, CloudServerCredential cloudServerCredential, ApplicationDeploymentEventLog applicationDeploymentEventLog, StringBuffer buffer) throws Exception {

        String path;
        String credential = org.apache.commons.lang.StringUtils.isBlank(cloudServerCredential.getPassword()) ? cloudServerCredential.getSecretKey() : cloudServerCredential.getPassword();
        CloudServerCredentialType cloudServerCredentialType = org.apache.commons.lang.StringUtils.isBlank(cloudServerCredential.getPassword()) ? CloudServerCredentialType.KEY : CloudServerCredentialType.PASSWORD;
        if (org.apache.commons.lang.StringUtils.containsIgnoreCase(cloudServer.getOs(), "windows")) {
            path = "C:/Windows/Temp/fit2cloud/" + artifact.getName().substring(0, artifact.getName().lastIndexOf(".zip"));
        } else {
            path = "/tmp/fit2cloud/" + artifact.getName().substring(0, artifact.getName().lastIndexOf(".zip"));
        }
        String script = codeDeployScripts.getLoadAppspec(path);
        ScriptRunRequest scriptRunRequest = new ScriptRunRequest()
                .withPort(cloudServer.getManagementPort())
                .withIp(cloudServer.getManagementIp())
                .withUsername(cloudServerCredential.getUsername())
                .withCredential(credential)
                .withCredentialType(cloudServerCredentialType)
                .withContent(script);
        if (org.apache.commons.lang.StringUtils.containsIgnoreCase(cloudServer.getOs(), "windows")) {
            OsUtils.mixWinParams(scriptRunRequest);
        }


        Proxy proxy = proxyService.getProxyByCloudServerId(cloudServer.getId());
        if (proxy != null) {
            scriptRunRequest.setProxy(proxy.getIp(), proxy.getPort(), proxy.getUsername(), proxy.getPassword());
        }

        buffer.append(DevopsLogUtil.info(applicationDeploymentEventLog.getEventName(), "加载appspec.yml..."));
        applicationDeploymentEventLog.setStdout(buffer.toString());
        applicationDeploymentEventLogService.saveApplicationDeploymentEventLog(applicationDeploymentEventLog);
        TaskResult taskResult = ansibleClient.runScriptAndGetResult(scriptRunRequest);
        handleException(taskResult);
        buffer.append(DevopsLogUtil.info(applicationDeploymentEventLog.getEventName(), ansibleClient.getStdOut(taskResult)));
        applicationDeploymentEventLog.setStdout(buffer.toString());
        applicationDeploymentEventLogService.saveApplicationDeploymentEventLog(applicationDeploymentEventLog);
        String appYml = ansibleClient.getStdOut(taskResult);
        YamlReader reader = new YamlReader(appYml);
        Appspec appspec = reader.read(Appspec.class);
        AsycDeploymentEventJob.appspecs.put(applicationDeploymentEventLog.getDeploymentLogId(), appspec);
        AsycDeploymentEventJob.downloadPath.put(applicationDeploymentEventLog.getDeploymentLogId(), path);

    }


    public void cleanEnv(Artifact artifact, CloudServer cloudServer, CloudServerCredential cloudServerCredential, ApplicationDeploymentEventLog applicationDeploymentEventLog, StringBuffer buffer) throws Exception {
        String credential = org.apache.commons.lang.StringUtils.isBlank(cloudServerCredential.getPassword()) ? cloudServerCredential.getSecretKey() : cloudServerCredential.getPassword();
        CloudServerCredentialType cloudServerCredentialType = org.apache.commons.lang.StringUtils.isBlank(cloudServerCredential.getPassword()) ? CloudServerCredentialType.KEY : CloudServerCredentialType.PASSWORD;
        ScriptRunRequest scriptRunRequest = new ScriptRunRequest();
        String scriptContent = codeDeployScripts.getCleanScript(artifact.getName(), cloudServer.getOs());
        scriptRunRequest
                .withPort(cloudServer.getManagementPort())
                .withIp(cloudServer.getManagementIp())
                .withUsername(cloudServerCredential.getUsername())
                .withCredential(credential)
                .withCredentialType(cloudServerCredentialType);

        Proxy proxy = proxyService.getProxyByCloudServerId(cloudServer.getId());
        if (proxy != null) {
            scriptRunRequest.setProxy(proxy.getIp(), proxy.getPort(), proxy.getUsername(), proxy.getPassword());
        }

        if (org.apache.commons.lang.StringUtils.containsIgnoreCase(cloudServer.getOs(), "windows")) {
            OsUtils.mixWinParams(scriptRunRequest);
            scriptRunRequest.withExecutePath("powershell")
                    .withContent(OsUtils.winPython(scriptContent));
        } else {
            scriptRunRequest.withExecutePath("/usr/bin/python")
                    .withContent(scriptContent);
        }
        buffer.append(DevopsLogUtil.info(applicationDeploymentEventLog.getEventName(), "清理FIT2CLOUD代码部署环境..."));
        TaskResult taskResult = ansibleClient.runScriptAndGetResult(scriptRunRequest);
        handleException(taskResult);
        buffer.append(DevopsLogUtil.info(applicationDeploymentEventLog.getEventName(), ansibleClient.getStdOut(taskResult)));
        applicationDeploymentEventLog.setStdout(buffer.toString());
        applicationDeploymentEventLogService.saveApplicationDeploymentEventLog(applicationDeploymentEventLog);
    }


    private void handleException(TaskResult taskResult) {
        if (!taskResult.getResult().isSuccess()) {
            StringBuilder buffer = new StringBuilder();
            String out = ansibleClient.getStdOut(taskResult);
            String msg = ansibleClient.getMsg(taskResult);

            if (StringUtils.isNotBlank(out)) {
                buffer.append(out);
            }
            if (StringUtils.isNotBlank(msg)) {
                buffer.append(msg);
            }
            F2CException.throwException(buffer.toString());
        }
    }


}
