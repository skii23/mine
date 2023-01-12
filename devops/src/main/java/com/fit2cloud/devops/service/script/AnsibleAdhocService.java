package com.fit2cloud.devops.service.script;


import com.fit2cloud.ansible.AnsibleService;
import com.fit2cloud.ansible.model.consts.CloudServerCredentialType;
import com.fit2cloud.ansible.model.request.ScriptRunRequest;
import com.fit2cloud.commons.server.base.domain.CloudServerCredential;
import com.fit2cloud.commons.server.exception.F2CException;
import com.fit2cloud.commons.server.utils.SessionUtils;
import com.fit2cloud.commons.utils.CommonThreadPool;
import com.fit2cloud.devops.base.domain.*;
import com.fit2cloud.devops.common.consts.StatusConstants;
import com.fit2cloud.devops.common.util.OsUtils;
import com.fit2cloud.devops.dto.ServerDTO;
import com.fit2cloud.devops.service.CredentialService;
import com.fit2cloud.devops.service.ProxyService;
import com.fit2cloud.devops.service.ScriptImplementLogService;
import com.fit2cloud.devops.service.VariableService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class AnsibleAdhocService {


    @Resource
    private CredentialService credentialService;
    @Resource
    private ScriptImplementLogService scriptImplementLogService;
    @Resource
    private AnsibleService ansibleService;
    @Resource
    private ScriptResultHandler scriptResultHandler;
    @Resource
    private VariableService variableService;
    @Resource
    private ProxyService proxyService;
    @Resource
    private CommonThreadPool commonThreadPool;

    private void createRunAdhoc(Script script, String executePath, ServerDTO serverDTO, ScriptImplementLogWithBLOBs scriptImplementLog) {
        commonThreadPool.addTask(() -> {
            try {
                String content = script.getContent();
                if (script.getId() != null) {
                    String scriptOs = script.getOs();
                    String scriptOsVersions = script.getOsVersion();
                    List<String> osVersionList = Arrays.asList(scriptOsVersions.split(","));
                    if (serverDTO.getOs() != null && serverDTO.getOsVersion() != null) {
                        if (!serverDTO.getOs().equalsIgnoreCase(scriptOs) || !osVersionList.contains(serverDTO.getOsVersion())) {
                            F2CException.throwException("操作系统不匹配！");
                        }
                    } else {
                        F2CException.throwException("未获取到有效操作系统版本信息！");
                    }

                }
                CloudServerCredential cloudServerCredential = credentialService.findCloudServerCredential(serverDTO.getId());
                String username = cloudServerCredential.getUsername();
                String credential =
                        StringUtils.isBlank(cloudServerCredential.getPassword()) ? cloudServerCredential.getSecretKey() : cloudServerCredential.getPassword();
                Map<String, String> vars = variableService.getHostVariable(serverDTO.getId());
                String target = serverDTO.getManagementIp();
                Integer port = serverDTO.getManagementPort();
                CloudServerCredentialType cloudServerCredentialType =
                        StringUtils.isBlank(cloudServerCredential.getPassword()) ? CloudServerCredentialType.KEY : CloudServerCredentialType.PASSWORD;
                ScriptRunRequest scriptRunRequest = new ScriptRunRequest();
                scriptRunRequest.withIp(target)
                        .withUsername(username)
                        .withPort(port)
                        .withExecutePath(executePath)
                        .withContent(content)
                        .withCredential(credential)
                        .addVars(vars)
                        .withCredentialType(cloudServerCredentialType);
                //判断是否是win系统
                if (StringUtils.containsIgnoreCase(serverDTO.getOs(), "windows")) {
                    OsUtils.mixWinParams(scriptRunRequest);
                }

                Proxy proxy = proxyService.getProxyByCloudServerId(serverDTO.getId());
                if (proxy != null) {
                    if (StringUtils.containsIgnoreCase(serverDTO.getOs(), "windows")) {
                        OsUtils.setUpWindowsProxy(scriptRunRequest, proxy);
                    } else {
                        scriptRunRequest.setProxy(proxy.getIp(), proxy.getPort(), proxy.getUsername(), proxy.getPassword());
                    }
                }

                String taskId = ansibleService.runScript(scriptRunRequest);
                scriptImplementLog.setAnsibleTaskId(taskId);
                scriptImplementLog.setStatus(StatusConstants.RUNNING);
                scriptImplementLogService.saveScriptImplementLog(scriptImplementLog);
                //向消息队列发送同步状态消息
                scriptResultHandler.push(taskId);
            } catch (Exception e) {
                scriptImplementLog.setStatus(StatusConstants.FAIL);
                scriptImplementLog.setStdoutContent(e.getMessage());
                scriptImplementLog.setCompletedTime(System.currentTimeMillis());
                scriptImplementLogService.saveScriptImplementLog(scriptImplementLog);
            }
        });
    }


    public void createRunAdhoc(Script script, String executePath, List<ServerDTO> serverDTOS) {
        serverDTOS.forEach(serverDTO -> {
            ScriptImplementLogWithBLOBs scriptImplementLog = new ScriptImplementLogWithBLOBs();
            scriptImplementLog.setCreatedTime(System.currentTimeMillis());
            scriptImplementLog.setCloudServerId(serverDTO.getId());
            scriptImplementLog.setScriptId(script.getId());
            scriptImplementLog.setScriptExecContent(script.getContent());
            scriptImplementLog.setStatus(StatusConstants.PENDING);
            scriptImplementLog.setWorkspaceId(SessionUtils.getWorkspaceId());
            scriptImplementLogService.saveScriptImplementLog(scriptImplementLog);
            createRunAdhoc(script, executePath, serverDTO, scriptImplementLog);
        });
    }


}

