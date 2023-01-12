package com.fit2cloud.devops.service.deployment.job;

import com.fit2cloud.ansible.AnsibleService;
import com.fit2cloud.ansible.model.TaskResult;
import com.fit2cloud.ansible.model.consts.CloudServerCredentialType;
import com.fit2cloud.ansible.model.request.ScriptRunRequest;
import com.fit2cloud.commons.server.base.domain.CloudServer;
import com.fit2cloud.commons.server.base.domain.CloudServerCredential;
import com.fit2cloud.commons.server.exception.F2CException;
import com.fit2cloud.devops.base.domain.ApplicationDeployment;
import com.fit2cloud.devops.base.domain.ApplicationDeploymentEventLog;
import com.fit2cloud.devops.base.domain.ApplicationDeploymentLog;
import com.fit2cloud.devops.base.domain.Proxy;
import com.fit2cloud.devops.common.model.Appspec;
import com.fit2cloud.devops.common.util.DevopsLogUtil;
import com.fit2cloud.devops.common.util.OsUtils;
import com.fit2cloud.devops.service.ApplicationDeploymentEventLogService;
import com.fit2cloud.devops.service.ApplicationDeploymentLogService;
import com.fit2cloud.devops.service.ApplicationDeploymentService;
import com.fit2cloud.devops.service.ProxyService;
import com.fit2cloud.devops.service.deployment.scripts.CodeDeployScripts;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class InstallService {

    @Resource
    private AnsibleService ansibleClient;
    @Resource
    private ApplicationDeploymentEventLogService applicationDeploymentEventLogService;
    @Resource
    private ApplicationDeploymentLogService applicationDeploymentLogService;
    @Resource
    private ApplicationDeploymentService applicationDeploymentService;
    @Resource
    private CodeDeployScripts codeDeployScripts;
    @Resource
    private ProxyService proxyService;


    public void install(ApplicationDeploymentEventLog applicationDeploymentEventLog, CloudServer cloudServer,
                        CloudServerCredential cloudServerCredential, StringBuffer buffer) throws Exception {
        Appspec appspec = AsycDeploymentEventJob.appspecs.get(applicationDeploymentEventLog.getDeploymentLogId());
        String rootPath = AsycDeploymentEventJob.downloadPath.get(applicationDeploymentEventLog.getDeploymentLogId());
        String credential = org.apache.commons.lang.StringUtils.isBlank(cloudServerCredential.getPassword()) ? cloudServerCredential.getSecretKey() : cloudServerCredential.getPassword();
        CloudServerCredentialType cloudServerCredentialType = org.apache.commons.lang.StringUtils.isBlank(cloudServerCredential.getPassword()) ? CloudServerCredentialType.KEY : CloudServerCredentialType.PASSWORD;

        ApplicationDeploymentLog applicationDeploymentLog = applicationDeploymentLogService.getApplicationDeploymentLogById(applicationDeploymentEventLog.getDeploymentLogId());
        ApplicationDeployment applicationDeployment = applicationDeploymentService.getApplicationDeployment(applicationDeploymentLog.getDeploymentId());

        String script = codeDeployScripts.getInstallScript(rootPath, appspec, cloudServer.getOs());

        ScriptRunRequest scriptRunRequest = new ScriptRunRequest();
        scriptRunRequest
                .withPort(cloudServer.getManagementPort())
                .withIp(cloudServer.getManagementIp())
                .withUsername(cloudServerCredential.getUsername())
                .withCredential(credential)
                .withCredentialType(cloudServerCredentialType)
                .withExecutePath("/usr/bin/python");

        if (org.apache.commons.lang.StringUtils.containsIgnoreCase(cloudServer.getOs(), "windows")) {
            OsUtils.mixWinParams(scriptRunRequest);
            scriptRunRequest.withExecutePath("powershell")
                    .withContent(OsUtils.winPython(script));
        } else {
            scriptRunRequest.withExecutePath("/usr/bin/python")
                    .withContent(script);
        }

        Proxy proxy = proxyService.getProxyByCloudServerId(cloudServer.getId());
        if (proxy != null) {
            scriptRunRequest.setProxy(proxy.getIp(), proxy.getPort(), proxy.getUsername(), proxy.getPassword());
        }

        buffer.append(DevopsLogUtil.info(applicationDeploymentEventLog.getEventName(), "代码部署中..."));
        applicationDeploymentEventLog.setStdout(buffer.toString());
        applicationDeploymentEventLogService.saveApplicationDeploymentEventLog(applicationDeploymentEventLog);
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
