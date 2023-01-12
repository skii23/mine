package com.fit2cloud.devops.service.jenkins.model.sysconfig;

import java.util.List;

/**
 * @author caiwzh
 * @date 2022/8/16
 */
public class GitLabServers {

    private List<GitLabServer> servers;

    public List<GitLabServer> getServers() {
        return servers;
    }

    public void setServers(List<GitLabServer> servers) {
        this.servers = servers;
    }

    public static class GitLabServer implements ServerConfig {

        private String credentialsId;

        private String manageSystemHooks;

        private String manageWebHooks;

        private String name;

        private String secretToken;

        private String serverUrl;

        private String hooksRootUrl;

        public String getHooksRootUrl() {
            return hooksRootUrl;
        }

        public void setHooksRootUrl(String hooksRootUrl) {
            this.hooksRootUrl = hooksRootUrl;
        }

        public String getCredentialsId() {
            return credentialsId;
        }

        public void setCredentialsId(String credentialsId) {
            this.credentialsId = credentialsId;
        }

        public String getManageSystemHooks() {
            return manageSystemHooks;
        }

        public void setManageSystemHooks(String manageSystemHooks) {
            this.manageSystemHooks = manageSystemHooks;
        }

        public String getManageWebHooks() {
            return manageWebHooks;
        }

        public void setManageWebHooks(String manageWebHooks) {
            this.manageWebHooks = manageWebHooks;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSecretToken() {
            return secretToken;
        }

        public void setSecretToken(String secretToken) {
            this.secretToken = secretToken;
        }

        public void setServerUrl(String serverUrl) {
            this.serverUrl = serverUrl;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public String getParamKey() {
            return SYSTEM_CONFIG_GITLAB_SERVERS + getName();
        }

        @Override
        public String getAlias() {
            return JenkinsSystemConfigParser.GITLAB.getServerKey();
        }

        @Override
        public String getServerUrl() {
            return serverUrl;
        }

        @Override
        public String getCheckUrl() {
            return GITLAB_CHECK_SERVER_URL;
        }

    }
}
