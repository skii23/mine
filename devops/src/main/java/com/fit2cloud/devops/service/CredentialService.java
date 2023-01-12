package com.fit2cloud.devops.service;

import com.fit2cloud.ansible.AnsibleService;
import com.fit2cloud.ansible.model.TaskResult;
import com.fit2cloud.ansible.model.consts.CloudServerCredentialType;
import com.fit2cloud.ansible.model.request.ScriptRunRequest;
import com.fit2cloud.commons.server.base.domain.CloudServer;
import com.fit2cloud.commons.server.base.domain.CloudServerCredential;
import com.fit2cloud.commons.server.exception.F2CException;
import com.fit2cloud.commons.server.service.CloudServerCredentialService;
import com.fit2cloud.commons.utils.BeanUtils;
import com.fit2cloud.devops.base.domain.DevopsCloudServer;
import com.fit2cloud.devops.base.domain.Proxy;
import com.fit2cloud.devops.base.mapper.DevopsCloudServerMapper;
import com.fit2cloud.devops.common.util.OsUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CredentialService {

    @Resource
    private AnsibleService ansibleService;
    @Resource
    private CloudServerCredentialService cloudServerCredentialService;
    @Resource
    private ProxyService proxyService;
    @Resource
    private DevopsCloudServerMapper devopsCloudServerMapper;

    public CloudServerCredential findCloudServerCredential(String cloudServerId) {
        return findDevOpsCloudServerCredential(cloudServerId, 3);
    }

    public CloudServerCredential findDevOpsCloudServerCredential(String cloudServerId, int retry) {
        DevopsCloudServer cloudServer = devopsCloudServerMapper.selectByPrimaryKey(cloudServerId);
        if (cloudServer == null) {
            F2CException.throwException("未找到主机！" + cloudServerId);
        }

        List<CloudServerCredential> cloudServerCredentials = cloudServerCredentialService.getCloudServerCredentialList(cloudServer.getId());
        String manageIp = cloudServer.getManagementIp();
        if ((cloudServerCredentials.isEmpty()) || manageIp == null) {
            F2CException.throwException("此云主机未设置管理信息！");
        }
        CloudServerCredential resut = null;
        try {
            for (CloudServerCredential cloudServerCredential : cloudServerCredentials) {
                TaskResult taskResult = find(cloudServerCredential, cloudServer, retry);
                if (taskResult.getResult().isSuccess()) {
                    resut = cloudServerCredential;
                    break;
                }
            }
            if (resut == null) {
                F2CException.throwException("主机无法连通，请检查管理信息是否正确和确定主机SSH/WINRM服务有效！");
            }
        } catch (Exception e) {
            F2CException.throwException(e.getMessage());
        }
        return resut;
    }

    public TaskResult find(CloudServerCredential cloudServerCredential, DevopsCloudServer devopsCloudServer, int retry) throws Exception {
        CloudServer cloudServer = new CloudServer();
        BeanUtils.copyBean(cloudServer, devopsCloudServer);
        return find(cloudServerCredential, cloudServer, retry);
    }

    private TaskResult find(CloudServerCredential cloudServerCredential, CloudServer cloudServer, int retry) throws Exception {
        if (retry == 0) {
            F2CException.throwException("ansible 连接异常！");
        }
        final String cmd = "echo hello";
        TaskResult keyResult = null;
        TaskResult passResult = null;
        String manageIp = cloudServer.getManagementIp();
        Integer port = cloudServer.getManagementPort();
        ScriptRunRequest scriptRunRequest = new ScriptRunRequest();
        scriptRunRequest.withIp(manageIp)
                .withUsername(cloudServerCredential.getUsername())
                .withPort(port)
                .withContent(cmd);

        Proxy proxy = proxyService.getProxyByCloudServerId(cloudServer.getId());
        if (proxy != null) {
            scriptRunRequest.setProxy(proxy.getIp(), proxy.getPort(), proxy.getUsername(), proxy.getPassword());
        }

        if (StringUtils.containsIgnoreCase(cloudServer.getOs(), "windows")) {
            OsUtils.mixWinParams(scriptRunRequest);
        }

        if (StringUtils.isNotBlank(cloudServerCredential.getPassword())) {
            scriptRunRequest.withCredentialType(CloudServerCredentialType.PASSWORD)
                    .withCredential(cloudServerCredential.getPassword());
            passResult = ansibleService.runScriptAndGetResult(scriptRunRequest);
            if (passResult.getResult() == null) {
                Thread.sleep(3000);
                return find(cloudServerCredential, cloudServer, --retry);
            } else {
                return passResult;
            }

        }
        if (StringUtils.isNotBlank(cloudServerCredential.getSecretKey())) {
            scriptRunRequest.withCredentialType(CloudServerCredentialType.KEY)
                    .withCredential(cloudServerCredential.getSecretKey());
            keyResult = ansibleService.runScriptAndGetResult(scriptRunRequest);
            if (keyResult.getResult() == null) {
                Thread.sleep(3000);
                return find(cloudServerCredential, cloudServer, --retry);
            } else {
                return keyResult;
            }
        }

        return null;
    }

    /**
     * TODO  这里后续可能需要支持多密码，以及key的方式
     *
     * @param id
     * @return
     */
    public CloudServerCredential selectByServerId(String id) {
        List<CloudServerCredential> cloudServerCredentialList = cloudServerCredentialService.getCloudServerCredentialList(id);
        return cloudServerCredentialList.size() > 0 ? cloudServerCredentialList.get(0) : null;
    }
}
