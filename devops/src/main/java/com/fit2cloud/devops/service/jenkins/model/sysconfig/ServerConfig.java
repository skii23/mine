package com.fit2cloud.devops.service.jenkins.model.sysconfig;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author caiwzh
 * @date 2022/8/17
 */
public interface ServerConfig extends Config{

    String BASE_CONFIG_PREFIX = "configuration-as-code.";

    String SYSTEM_CONFIG_GITLAB_SERVERS = BASE_CONFIG_PREFIX+ "GitLab.Servers.";

    String SYSTEM_CONFIG_GITEA_SERVERS = BASE_CONFIG_PREFIX + "Gitea.Servers.";

    String GITLAB_CHECK_SERVER_URL = "/descriptorByName/io.jenkins.plugins.gitlabserverconfig.servers.GitLabServer/checkServerUrl";

    String GITEA_CHECK_SERVER_URL = "/descriptorByName/org.jenkinsci.plugin.gitea.servers.GiteaServer/checkServerUrl";

    @JSONField(serialize = false)
    String getParamKey();

    String getServerUrl();

    @JSONField(serialize = false)
    String getCheckUrl();

}
