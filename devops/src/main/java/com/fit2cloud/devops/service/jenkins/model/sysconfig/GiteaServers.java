package com.fit2cloud.devops.service.jenkins.model.sysconfig;

import java.util.List;

/**
 * @author caiwzh
 * @date 2022/8/17
 */
public class GiteaServers {
    private List<GiteaServer> servers;

    public List<GiteaServer> getServers() {
        return servers;
    }

    public void setServers(List<GiteaServer> servers) {
        this.servers = servers;
    }

    public static class GiteaServer implements ServerConfig {
        private String credentialsId;
        private String displayName;
        private String manageHooks;
        private String serverUrl;
        private String aliasUrl;

        public String getAliasUrl() {
            return aliasUrl;
        }

        public void setAliasUrl(String aliasUrl) {
            this.aliasUrl = aliasUrl;
        }

        public String getCredentialsId() {
            return credentialsId;
        }

        public void setCredentialsId(String credentialsId) {
            this.credentialsId = credentialsId;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getManageHooks() {
            return manageHooks;
        }

        public void setManageHooks(String manageHooks) {
            this.manageHooks = manageHooks;
        }

        public void setServerUrl(String serverUrl) {
            this.serverUrl = serverUrl;
        }

        @Override
        public String getParamKey() {
            return SYSTEM_CONFIG_GITEA_SERVERS + getDisplayName();
        }

        @Override
        public String getAlias() {
            return JenkinsSystemConfigParser.GITEA.getServerKey();
        }

        @Override
        public String getServerUrl() {
            return serverUrl;
        }

        @Override
        public String getCheckUrl() {
            return GITEA_CHECK_SERVER_URL;
        }
    }
}
