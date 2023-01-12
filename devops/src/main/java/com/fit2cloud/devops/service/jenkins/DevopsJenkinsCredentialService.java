package com.fit2cloud.devops.service.jenkins;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.commons.server.utils.SessionUtils;
import com.fit2cloud.commons.utils.CommonThreadPool;
import com.fit2cloud.commons.utils.ResultHolder;
import com.fit2cloud.commons.utils.UUIDUtil;
import com.fit2cloud.devops.base.domain.DevopsJenkinsCredential;
import com.fit2cloud.devops.base.domain.DevopsJenkinsCredentialExample;
import com.fit2cloud.devops.base.domain.DevopsJenkinsCredentialWithBLOBs;
import com.fit2cloud.devops.base.mapper.DevopsJenkinsCredentialMapper;
import com.fit2cloud.devops.common.consts.JenkinsConstants;
import com.fit2cloud.devops.common.util.CommonUtils;
import com.fit2cloud.devops.dao.ext.ExtDevopsJenkinsCredentialMapper;
import com.fit2cloud.devops.dto.DevopsJenkinsCredentialDto;
import com.offbytwo.jenkins.client.JenkinsHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

/**
 * @author wisonic
 */
@Service
public class DevopsJenkinsCredentialService {

    private static final String ROOT_CREDENTIAL_URL = "credentials/store/system/domain/_/api/json?depth=1";
    private static final String DELETE_CREDENTIAL_URL = "credentials/store/system/domain/_/credential/${credentialId}/doDelete";
    private static final String CREATE_CREDENTIALS_URL = "credentials/store/system/domain/_/createCredentials";
    private static final String UPDATE_CREDENTIAL_URL = "credentials/store/system/domain/_/credential/${credentialId}/updateSubmit";
    private static final String DETAIL_CREDENTIAL_URL = "credentials/store/system/domain/_/credential/${credentialId}/api/json";


    @Resource
    private DevopsJenkinsService devopsJenkinsService;

    @Resource
    private DevopsJenkinsCredentialMapper devopsJenkinsCredentialMapper;

    @Resource
    private ExtDevopsJenkinsCredentialMapper extDevopsJenkinsCredentialMapper;

    @Resource
    private CommonThreadPool commonThreadPool;

    public void syncAllCredentials() {
        JenkinsHttpClient jenkinsClient = devopsJenkinsService.getJenkinsClient();
        Optional.ofNullable(jenkinsClient).ifPresent(jenkinsHttpClient -> {
            try {
                String result = jenkinsHttpClient.get(ROOT_CREDENTIAL_URL);
                JSONObject jsonObject = JSONObject.parseObject(result);
                JSONArray remoteCredentials = jsonObject.getJSONArray("credentials");
                Map<String, DevopsJenkinsCredentialWithBLOBs> remoteCredentialMap = new HashMap<>();
                remoteCredentials.forEach(credential -> {
                    JSONObject credentialObj = (JSONObject) credential;
                    DevopsJenkinsCredentialWithBLOBs devopsJenkinsCredential = credentialObj.toJavaObject(DevopsJenkinsCredentialWithBLOBs.class);
                    remoteCredentialMap.put(devopsJenkinsCredential.getId(), devopsJenkinsCredential);
                });

                List<DevopsJenkinsCredentialWithBLOBs> localCredentials = devopsJenkinsCredentialMapper.selectByExampleWithBLOBs(new DevopsJenkinsCredentialExample());
                Map<String, DevopsJenkinsCredentialWithBLOBs> localCredentialMap = new HashMap<>();
                localCredentials.forEach(credential -> localCredentialMap.put(credential.getId(), credential));

                remoteCredentialMap.forEach((id, credential) -> {
                    DevopsJenkinsCredentialWithBLOBs localCredential = localCredentialMap.get(id);
                    if (localCredential == null) {
                        devopsJenkinsCredentialMapper.insert(credential);
                    } else {
                        localCredential.setDisplayName(credential.getDisplayName());
                        localCredential.setFullName(credential.getFullName());
                        localCredential.setFingerprint(credential.getFingerprint());
                        localCredential.setDescription(credential.getDescription());
                        localCredential.setTypeName(credential.getTypeName());
                        devopsJenkinsCredentialMapper.updateByPrimaryKeyWithBLOBs(localCredential);
                    }
                    localCredentialMap.remove(id);
                });
                localCredentialMap.keySet().forEach(id -> devopsJenkinsCredentialMapper.deleteByPrimaryKey(id));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public List<DevopsJenkinsCredentialDto> getCredential(Map<String, Object> params) {
        CommonUtils.filterPermission(params);
        return extDevopsJenkinsCredentialMapper.getCredentials(params);
    }

    public void syncCredentials(List<DevopsJenkinsCredentialWithBLOBs> credentials) {
        JenkinsHttpClient jenkinsClient = devopsJenkinsService.getJenkinsClient();
        try {

            String result = jenkinsClient.get(ROOT_CREDENTIAL_URL);
            JSONObject jsonObject = JSONObject.parseObject(result);
            JSONArray remoteCredentials = jsonObject.getJSONArray("credentials");

            Map<String, DevopsJenkinsCredential> remoteCredentialMap = new HashMap<>(16);
            remoteCredentials.forEach(credential -> {
                JSONObject credentialObj;
                if (credential instanceof Map) {
                    credentialObj = new JSONObject(((Map) credential));
                }else {
                    credentialObj = (JSONObject) credential;
                }
                String id = credentialObj.getString("id");
                DevopsJenkinsCredential devopsJenkinsCredential = credentialObj.toJavaObject(DevopsJenkinsCredential.class);
                remoteCredentialMap.put(id, devopsJenkinsCredential);
            });

            credentials.forEach(credential -> {
                DevopsJenkinsCredential remoteCredential = remoteCredentialMap.get(credential.getId());
                if (remoteCredential == null) {
                    devopsJenkinsCredentialMapper.deleteByPrimaryKey(credential.getId());
                } else {
                    credential.setDescription(remoteCredential.getDescription());
                    credential.setDisplayName(remoteCredential.getDisplayName());
                    credential.setFullName(remoteCredential.getFullName());
                    credential.setFingerprint(remoteCredential.getFingerprint());
                    devopsJenkinsCredentialMapper.updateByPrimaryKeySelective(credential);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void grantCredential(List<DevopsJenkinsCredentialWithBLOBs> devopsJenkinsCredentials) {
        devopsJenkinsCredentials.forEach(credential -> devopsJenkinsCredentialMapper.updateByPrimaryKeySelective(credential));
    }

    public ResultHolder updateCredential(JSONObject jsonObject) {
        ResultHolder resultHolder = new ResultHolder();
        DevopsJenkinsCredentialWithBLOBs devopsJenkinsCredential = jsonObject.toJavaObject(DevopsJenkinsCredentialWithBLOBs.class);
        DevopsJenkinsCredentialWithBLOBs localCredential = devopsJenkinsCredentialMapper.selectByPrimaryKey(devopsJenkinsCredential.getId());
        devopsJenkinsCredential.setOrganization(localCredential.getOrganization());
        devopsJenkinsCredential.setWorkspace(localCredential.getWorkspace());
        JSONObject wrapperObj = new JSONObject();
        JenkinsHttpClient jenkinsClient = devopsJenkinsService.getJenkinsClient();
        List<NameValuePair> data = new ArrayList<>();
        wrapperObj.fluentPut("id", devopsJenkinsCredential.getId())
                .fluentPut("username", devopsJenkinsCredential.getUsername())
                .fluentPut("scope", "GLOBAL")
                .put("description", devopsJenkinsCredential.getDescription());
        boolean pipeline = false;
        switch (devopsJenkinsCredential.getTypeName()) {
            case "Username with password": {
                wrapperObj.fluentPut("stapler-class", JenkinsConstants.NORMAL_CREDENTIAL_STAPLER_CLASS)
                        .put("password", devopsJenkinsCredential.getPassword());
                break;
            }
            case "SSH Username with private key": {
                JSONObject innerObj = new JSONObject();
                innerObj.fluentPut("value", "0")
                        .fluentPut("privateKey", devopsJenkinsCredential.getPrivateKey())
                        .put("stapler-class", JenkinsConstants.PRIVATE_KEY_CREDENTIAL_STAPLER_CLASS);
                wrapperObj.fluentPut("stapler-class", JenkinsConstants.SSH_CREDENTIAL_STAPLER_CLASS)
                        .fluentPut("passphrase", devopsJenkinsCredential.getPassword())
                        .put("privateKeySource", innerObj);
                break;
            }
            case "Try sample Pipeline":
                pipeline = true;
                break;
            default: {
                resultHolder.setSuccess(false);
                resultHolder.setMessage("不支持该类型的凭据！");
                return resultHolder;
            }
        }
        BasicNameValuePair json = new BasicNameValuePair("json", wrapperObj.toJSONString());
        data.add(json);
        try {
            //流水线脚本模板
            if (pipeline) {
                devopsJenkinsCredential.setFingerprint("");
                devopsJenkinsCredential.setDisplayName("Sample Pipeline/" + devopsJenkinsCredential.getUsername());
                devopsJenkinsCredential.setFullName("");
            } else {
                HttpResponse httpResponse = jenkinsClient.post_form_with_result(UPDATE_CREDENTIAL_URL.replace("${credentialId}", devopsJenkinsCredential.getId()), data, true);
                String newCredentialStr = jenkinsClient.get(DETAIL_CREDENTIAL_URL.replace("${credentialId}", devopsJenkinsCredential.getId()));
                DevopsJenkinsCredential newCredential = JSONObject.parseObject(newCredentialStr, DevopsJenkinsCredential.class);
                devopsJenkinsCredential.setFingerprint(newCredential.getFingerprint());
                devopsJenkinsCredential.setDisplayName(newCredential.getDisplayName());
                devopsJenkinsCredential.setFullName(newCredential.getFullName());
            }
            devopsJenkinsCredentialMapper.updateByPrimaryKeyWithBLOBs(devopsJenkinsCredential);
        } catch (IOException e) {
            resultHolder.setSuccess(false);
            resultHolder.setMessage("保存凭据失败！");
            e.printStackTrace();
        }
        return resultHolder;
    }

    public ResultHolder createCredential(JSONObject jsonObject) {
        ResultHolder resultHolder = new ResultHolder();
        boolean isReset = jsonObject.getBooleanValue("isReset");
        String id = jsonObject.getString("id");
        if (StringUtils.isBlank(id)) {
            jsonObject.put("id", UUIDUtil.newUUID());
        } else {
            DevopsJenkinsCredentialWithBLOBs localCredential = devopsJenkinsCredentialMapper.selectByPrimaryKey(id);
            if (localCredential != null && !isReset) {
                resultHolder.setMessage("相同ID的凭据已存在！");
                resultHolder.setSuccess(false);
                return resultHolder;
            }
        }
        DevopsJenkinsCredentialWithBLOBs devopsJenkinsCredential = jsonObject.toJavaObject(DevopsJenkinsCredentialWithBLOBs.class);
        JSONObject wrapperObj = new JSONObject();
        JSONObject innerObj = new JSONObject();
        innerObj.fluentPut("scope", "GLOBAL")
                .fluentPut("id", devopsJenkinsCredential.getId())
                .fluentPut("username", devopsJenkinsCredential.getUsername())
                .fluentPut("description", devopsJenkinsCredential.getDescription());
        devopsJenkinsCredential.setOrganization(SessionUtils.getOrganizationId());
        devopsJenkinsCredential.setWorkspace(SessionUtils.getWorkspaceId());
        JenkinsHttpClient jenkinsClient = devopsJenkinsService.getJenkinsClient();
        List<NameValuePair> data = new ArrayList<>();
        boolean pipeline = false;
        switch (devopsJenkinsCredential.getTypeName()) {
            case "Username with password": {
                innerObj.fluentPut("password", devopsJenkinsCredential.getPassword())
                        .put("stapler-class", JenkinsConstants.NORMAL_CREDENTIAL_STAPLER_CLASS);
                break;
            }
            case "SSH Username with private key": {
                JSONObject privateKeyObj = new JSONObject();
                privateKeyObj.fluentPut("value", "0")
                        .fluentPut("privateKey", devopsJenkinsCredential.getPrivateKey())
                        .put("stapler-class", JenkinsConstants.PRIVATE_KEY_CREDENTIAL_STAPLER_CLASS);
                innerObj.fluentPut("passphrase", devopsJenkinsCredential.getPassword())
                        .fluentPut("stapler-class", JenkinsConstants.SSH_CREDENTIAL_STAPLER_CLASS)
                        .put("privateKeySource", privateKeyObj);
                break;
            }
            case "Try sample Pipeline":
                pipeline = true;
                break;
            default: {
                resultHolder.setSuccess(false);
                resultHolder.setMessage("不支持该类型的凭据！");
                return resultHolder;
            }
        }
        wrapperObj.put("credentials", innerObj);
        BasicNameValuePair json = new BasicNameValuePair("json", wrapperObj.toJSONString());
        data.add(json);
        try {
            //流水线脚本模板
            if (pipeline) {
                devopsJenkinsCredential.setFingerprint("");
                devopsJenkinsCredential.setDisplayName("Sample Pipeline/" + devopsJenkinsCredential.getUsername());
                devopsJenkinsCredential.setFullName("");
            } else {
                HttpResponse httpResponse = jenkinsClient.post_form_with_result(CREATE_CREDENTIALS_URL, data, true);
                String newCredentialStr = jenkinsClient.get(DETAIL_CREDENTIAL_URL.replace("${credentialId}", devopsJenkinsCredential.getId()));
                DevopsJenkinsCredential newCredential = JSONObject.parseObject(newCredentialStr, DevopsJenkinsCredential.class);
                devopsJenkinsCredential.setFingerprint(newCredential.getFingerprint());
                devopsJenkinsCredential.setDisplayName(newCredential.getDisplayName());
                devopsJenkinsCredential.setFullName(newCredential.getFullName());
            }
            devopsJenkinsCredentialMapper.insert(devopsJenkinsCredential);
        } catch (IOException e) {
            resultHolder.setSuccess(false);
            resultHolder.setMessage("保存凭据失败！");
            e.printStackTrace();
        }
        return resultHolder;
    }

    public ResultHolder saveCredential(JSONObject jsonObject) {
        if (jsonObject.getBooleanValue("isReset")) {
            return updateCredential(jsonObject);
        } else {
            return createCredential(jsonObject);
        }
    }

    public void deleteCredentials(List<DevopsJenkinsCredential> credentials) {
        JenkinsHttpClient jenkinsClient = devopsJenkinsService.getJenkinsClient();
        credentials.forEach(credential -> {
            try {
                jenkinsClient.post(DELETE_CREDENTIAL_URL.replace("${credentialId}", credential.getId()), true);
                devopsJenkinsCredentialMapper.deleteByPrimaryKey(credential.getId());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}
