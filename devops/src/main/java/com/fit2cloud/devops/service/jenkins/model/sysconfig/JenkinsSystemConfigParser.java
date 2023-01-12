package com.fit2cloud.devops.service.jenkins.model.sysconfig;

import com.alibaba.fastjson.JSONObject;

import java.util.stream.Stream;

/**
 * @author caiwzh
 * @date 2022/8/18
 */
public enum JenkinsSystemConfigParser {
    //对应前端字段 {"gitLabServer":{},"gitType":"Gitea","giteaServer":{"credentialsId":"repo","displayName":"2","manageHooks":true,"serverUrl":"2"}}
    GITLAB("GitLab","gitLabServer",GitLabServers.GitLabServer.class),
    GITEA("Gitea","giteaServer",GiteaServers.GiteaServer.class);

    JenkinsSystemConfigParser(String name, String serverKey, Class<? extends ServerConfig> clazz) {
        this.name = name;
        this.serverKey = serverKey;
        this.clazz = clazz;
    }

    String name;

    String serverKey;

    Class<? extends ServerConfig> clazz;

    public String getName() {
        return name;
    }

    public Class<? extends ServerConfig> getClazz() {
        return clazz;
    }

    public String getServerKey() {
        return serverKey;
    }

    public static JenkinsSystemConfigParser nameOf(String name) {
        return Stream.of(JenkinsSystemConfigParser.values()).filter(e -> e.getName().equals(name)).findFirst().orElse(JenkinsSystemConfigParser.GITLAB);
    }

    public static ServerConfig parse(JSONObject data) {
        JenkinsSystemConfigParser serverType = JenkinsSystemConfigParser.nameOf(data.getString("type"));
        return data.getJSONObject(serverType.getServerKey()).toJavaObject(serverType.getClazz());
    }

}
