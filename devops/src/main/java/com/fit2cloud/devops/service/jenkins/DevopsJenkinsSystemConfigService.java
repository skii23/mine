package com.fit2cloud.devops.service.jenkins;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.commons.server.exception.F2CException;
import com.fit2cloud.commons.utils.LogUtil;
import com.fit2cloud.commons.utils.UUIDUtil;
import com.fit2cloud.devops.base.domain.DevopsJenkinsParams;
import com.fit2cloud.devops.base.domain.DevopsJenkinsParamsExample;
import com.fit2cloud.devops.base.mapper.DevopsJenkinsParamsMapper;
import com.fit2cloud.devops.service.jenkins.model.sysconfig.GitLabServers;
import com.fit2cloud.devops.service.jenkins.model.sysconfig.GiteaServers;
import com.fit2cloud.devops.service.jenkins.model.sysconfig.JenkinsSystemConfigParser;
import com.fit2cloud.devops.service.jenkins.model.sysconfig.ServerConfig;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

/**
 * @author caiwzh
 * @date 2022/8/16
 */
@Service
public class DevopsJenkinsSystemConfigService {

    public static final String SYSTEM_CONFIG_KEY = "jenkins.newsource.url";

    public static final String YAML_UUID = UUIDUtil.newUUID();

    @Resource
    private DevopsJenkinsService devopsJenkinsService;

    @Resource
    private DevopsJenkinsParamsMapper devopsJenkinsParamsMapper;

    public void addGitServer(ServerConfig server) {
        checkUrl(server);
        DevopsJenkinsParamsExample example = new DevopsJenkinsParamsExample();
        example.createCriteria().andParamKeyEqualTo(server.getParamKey());
        List<DevopsJenkinsParams> devopsJenkinsParams = devopsJenkinsParamsMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(devopsJenkinsParams)) {
            F2CException.throwException(server.getAlias() + " 名称重复！");
        } else {
            DevopsJenkinsParams record = new DevopsJenkinsParams();
            record.setId(UUIDUtil.newUUID());
            record.setParamKey(server.getParamKey());
            record.setAlias(server.getAlias());
            record.setParamValue(JSON.toJSONString(server));
            devopsJenkinsParamsMapper.insert(record);
            // 触发替换配置
            replaceYamlConfig();
        }
    }

    public void deleteGitServer(String id) {
        devopsJenkinsParamsMapper.deleteByPrimaryKey(id);
        // 触发替换配置
        replaceYamlConfig();
    }

    public void updateGitServer(ServerConfig server, String id) {
        DevopsJenkinsParamsExample example = new DevopsJenkinsParamsExample();
        example.createCriteria().andIdEqualTo(id);
        List<DevopsJenkinsParams> devopsJenkinsParamList = devopsJenkinsParamsMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(devopsJenkinsParamList)) {
            checkUrl(server);
            DevopsJenkinsParams devopsJenkinsParam = devopsJenkinsParamList.get(0);
            devopsJenkinsParam.setParamValue(JSON.toJSONString(server));
            devopsJenkinsParamsMapper.updateByPrimaryKey(devopsJenkinsParam);
            // 触发替换配置
            replaceYamlConfig();
        }
    }

    public GitLabServers getDbGitLabServers() {
        DevopsJenkinsParamsExample example = new DevopsJenkinsParamsExample();
        example.createCriteria().andParamKeyLike(ServerConfig.SYSTEM_CONFIG_GITLAB_SERVERS + "%");
        List<DevopsJenkinsParams> devopsJenkinsParams = devopsJenkinsParamsMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(devopsJenkinsParams)) {
            GitLabServers gitLabServers = new GitLabServers();
            List<GitLabServers.GitLabServer> servers = new ArrayList<>(devopsJenkinsParams.size());
            gitLabServers.setServers(servers);
            for (DevopsJenkinsParams devopsJenkinsParam : devopsJenkinsParams) {
                servers.add(JSON.parseObject(devopsJenkinsParam.getParamValue())
                        .toJavaObject(GitLabServers.GitLabServer.class));
            }
            return gitLabServers;
        }
        return null;
    }

    public GiteaServers getDBGiteaServers() {
        DevopsJenkinsParamsExample example = new DevopsJenkinsParamsExample();
        example.createCriteria().andParamKeyLike(ServerConfig.SYSTEM_CONFIG_GITEA_SERVERS + "%");
        List<DevopsJenkinsParams> devopsJenkinsParams = devopsJenkinsParamsMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(devopsJenkinsParams)) {
            GiteaServers giteaServers = new GiteaServers();
            List<GiteaServers.GiteaServer> servers = new ArrayList<>(devopsJenkinsParams.size());
            giteaServers.setServers(servers);
            for (DevopsJenkinsParams devopsJenkinsParam : devopsJenkinsParams) {
                servers.add(JSON.parseObject(devopsJenkinsParam.getParamValue())
                        .toJavaObject(GiteaServers.GiteaServer.class));
            }
            return giteaServers;
        }
        return null;
    }

    public JSONObject getConfigYaml() {
        JSONObject data = new JSONObject();
        // GitLabServers 服务列表
        GitLabServers dbGitLabServers = getDbGitLabServers();
        if (dbGitLabServers != null && CollectionUtils.isNotEmpty(dbGitLabServers.getServers())) {
            data.put("gitLabServers", JSON.toJSON(dbGitLabServers));
        }
        // GiteaServers 服务列表
        GiteaServers dbGiteaServers = getDBGiteaServers();
        if (dbGiteaServers != null && CollectionUtils.isNotEmpty(dbGiteaServers.getServers())) {
            data.put("giteaServers", JSON.toJSON(dbGiteaServers));
        }
        return new JSONObject().fluentPut("unclassified", data);
    }

    /**
     * 检测配置的git服务列表是否可用
     *
     * @param server
     */
    public void checkUrl(ServerConfig server) {
        String res = "";
        try {
            List<NameValuePair> data = new ArrayList<>();
            data.add(new BasicNameValuePair("value", server.getServerUrl()));
            data.add(new BasicNameValuePair("serverUrl", server.getServerUrl()));
            HttpResponse httpResponse = devopsJenkinsService.getJenkinsClient()
                    .post_form_with_result(server.getCheckUrl(), data, true);
            res = EntityUtils.toString(httpResponse.getEntity());
        } catch (Exception e) {
            LogUtil.error("checkUrl error", e);
            F2CException.throwException("checkUrl error:" + e.getMessage());
        }
        if (res.contains("error")) {
            F2CException.throwException("源码url无效");
        }
    }

    /**
     * 更新jenkins 全局配置
     */
    public void replaceYamlConfig() {
        try {
            DevopsJenkinsParamsExample devopsJenkinsParamsExample = new DevopsJenkinsParamsExample();
            devopsJenkinsParamsExample.createCriteria().andParamKeyEqualTo(SYSTEM_CONFIG_KEY);
            List<DevopsJenkinsParams> devopsJenkinsParams = devopsJenkinsParamsMapper
                    .selectByExample(devopsJenkinsParamsExample);
            if (CollectionUtils.isEmpty(devopsJenkinsParams)
                    || StringUtils.isBlank(devopsJenkinsParams.get(0).getParamValue())) {
                return;
            }
            String newSourceUrl = devopsJenkinsParams.get(0).getParamValue() + "?uuid=" + YAML_UUID;
            String url = "/configuration-as-code/replace";
            List<NameValuePair> data = new ArrayList<>();
            JSONObject json = new JSONObject();
            json.put("newSource", devopsJenkinsParams.get(0).getParamValue());
            data.add(new BasicNameValuePair("json", json.toJSONString()));
            data.add(new BasicNameValuePair("_.newSource", newSourceUrl));
            HttpResponse httpResponse = devopsJenkinsService.getJenkinsClient().post_form_with_result(url, data, true);
            EntityUtils.toString(httpResponse.getEntity());
        } catch (Exception e) {
            LogUtil.error("replaceYamlConfig error", e);
            F2CException.throwException("replaceYamlConfig error:" + e.getMessage());
        }
    }

    public Object fillProjectPathItems(JSONObject source) {
        try {
            String type = source.getString("type");
            List<NameValuePair> pairs = new ArrayList<>();
            String url = "";
            if (StringUtils.equalsIgnoreCase(type, JenkinsSystemConfigParser.GITEA.name())) {
                String serverUrl = source.getString("serverUrl");
                if (serverUrl.contains("@")) {
                    serverUrl = serverUrl.split("@")[1];
                }
                pairs.add(new BasicNameValuePair("serverUrl", serverUrl));
                pairs.add(new BasicNameValuePair("credentialsId", source.getString("credentialsId")));
                pairs.add(new BasicNameValuePair("repoOwner", source.getString("repoOwner")));
                pairs.add(new BasicNameValuePair("repository", source.getString("repository")));
                url = "/descriptorByName/org.jenkinsci.plugin.gitea.GiteaSCMSource/fillRepositoryItems";
            } else if (StringUtils.equalsIgnoreCase(type, JenkinsSystemConfigParser.GITLAB.name())) {
                pairs.add(new BasicNameValuePair("serverName", source.getString("serverName")));
                pairs.add(new BasicNameValuePair("projectOwner", source.getString("projectOwner")));
                url = "/descriptorByName/io.jenkins.plugins.gitlabbranchsource.GitLabSCMSource/fillProjectPathItems";
            } else {
                return null;
            }
            HttpResponse httpResponse = devopsJenkinsService.getJenkinsClient().post_form_with_result(url, pairs, true);
            return JSON.parseObject(EntityUtils.toString(httpResponse.getEntity()));
        } catch (Exception e) {
            LogUtil.error("fillProjectPathItems error", e);
            F2CException.throwException("fillProjectPathItems error:" + e.getMessage());
        }
        return "";
    }

    public Map<String, List<DevopsJenkinsParams>> getToolParams() {
        Map<String, List<DevopsJenkinsParams>> param = new HashMap<>(16);
        JSONObject data = getJenkinsSystemConfig();
        JSONObject tools = data.getJSONObject("tool");
        tools.keySet().forEach(k -> {
            try {
                List<DevopsJenkinsParams> names = param.computeIfAbsent(k, key -> Lists.newLinkedList());
                JSONArray installations = tools.getJSONObject(k).getJSONArray("installations");
                if (CollectionUtils.isNotEmpty(installations)) {
                    Set<String> nameSet = Sets.newHashSet();
                    for (int i = 0; i < installations.size(); i++) {
                        JSONObject node = installations.getJSONObject(i);
                        String name = node.getString("name");
                        if (StringUtils.isNotBlank(name) && nameSet.add(name)) {
                            DevopsJenkinsParams params = new DevopsJenkinsParams();
                            params.setParamValue(name);
                            params.setAlias(name);
                            names.add(params);
                        }
                    }
                }
            } catch (Exception e) {
            }
        });
        return param;
    }

    public Map<String, List<String>> getSonarParams() {
        Map<String, List<String>> param = new HashMap<>();
        try {
            JSONObject data = getJenkinsSystemConfig();
            JSONArray array = data.getJSONObject("unclassified").getJSONObject("sonarGlobalConfiguration")
                    .getJSONArray("installations");
            List<String> sonarInstallationNames = Lists.newArrayList();
            for (int i = 0; i < array.size(); i++) {
                JSONObject node = array.getJSONObject(i);
                sonarInstallationNames.add(node.getString("name"));
            }
            List<String> jdkVersions = Lists.newArrayList();
            array = data.getJSONObject("tool").getJSONObject("jdk").getJSONArray("installations");
            for (int i = 0; i < array.size(); i++) {
                JSONObject node = array.getJSONObject(i);
                jdkVersions.add(node.getString("name"));
            }
            List<String> sonarRunnerInstallations = Lists.newArrayList();
            array = data.getJSONObject("tool").getJSONObject("sonarRunnerInstallation").getJSONArray("installations");
            for (int i = 0; i < array.size(); i++) {
                JSONObject node = array.getJSONObject(i);
                sonarRunnerInstallations.add(node.getString("name"));
            }
            param.put("sonarInstallationNames", sonarInstallationNames);
            param.put("jdkVersions", jdkVersions);
            param.put("sonarRunnerInstallations", sonarRunnerInstallations);
        } catch (Exception e) {
        }
        return param;
    }

    public Map<String, String> getSonarServers() {
        Map<String, String> sonarServers = new HashMap<>();
        try {
            JSONObject data = getJenkinsSystemConfig();
            JSONArray array = data.getJSONObject("unclassified").getJSONObject("sonarGlobalConfiguration")
                    .getJSONArray("installations");
            for (int i = 0; i < array.size(); i++) {
                JSONObject node = array.getJSONObject(i);
                if (!node.containsKey("name") || !node.containsKey("serverUrl")) {
                    continue;
                }
                if (StringUtils.isBlank(node.getString("name"))) {
                    continue;
                }
                sonarServers.put(node.getString("name"), node.getString("serverUrl"));
            }
        } catch (Exception e) {
            LogUtil.error("get sonarqube srvers error, maybe sonarqube plugin not install.", e);
        }

        return sonarServers;
    }

    public JSONObject getJenkinsSystemConfig() {
        try {
            HttpResponse httpResponse = devopsJenkinsService.getJenkinsClient()
                    .post_form_with_result("/configuration-as-code/export", null, true);
            String response = EntityUtils.toString(httpResponse.getEntity());
            DumperOptions dumperOptions = new DumperOptions();
            dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.AUTO);
            return new JSONObject(new Yaml(dumperOptions).loadAs(response, Map.class));
        } catch (IOException e) {
            LogUtil.error("getJenkinsSystemConfig error", e);
        }
        return new JSONObject();
    }

    public List<String> getExecutorNodes(){
        List<String> result = Lists.newArrayList();
        try {
            JSONObject data = getJenkinsSystemConfig();
            JSONArray nodes = data.getJSONObject("jenkins").getJSONArray("nodes");
            for (int i = 0; i < nodes.size(); i++) {
                JSONObject nodeData = nodes.getJSONObject(i);
                JSONObject permanent = nodeData.getJSONObject("permanent");
                String name = permanent.getString("name");
                if(StringUtils.isNotBlank(name)){
                    result.add(name);
                }
            }
        } catch (Exception e) {
        }
        if(CollectionUtils.isEmpty(result)){
            return Lists.newArrayList("jenkins-slave-8C32G");
        }
        return result;
    }
}
