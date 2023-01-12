package com.fit2cloud.devops.service.jenkins.model.common.scm.svn;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("hudson.scm.SubversionSCM_-ModuleLocation")
public class ModuleLocation {
    private String remote;
    private String credentialsId;
    private String local = ".";
    private String depthOption = "infinity";
    private Boolean ignoreExternalsOption = true;
    private Boolean cancelProcessOnExternalsFail = true;

    public String getRemote() {
        return remote;
    }

    public void setRemote(String remote) {
        this.remote = remote;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getDepthOption() {
        return depthOption;
    }

    public void setDepthOption(String depthOption) {
        this.depthOption = depthOption;
    }

    public Boolean isIgnoreExternalsOption() {
        return ignoreExternalsOption;
    }

    public void setIgnoreExternalsOption(Boolean ignoreExternalsOption) {
        this.ignoreExternalsOption = ignoreExternalsOption;
    }

    public Boolean isCancelProcessOnExternalsFail() {
        return cancelProcessOnExternalsFail;
    }

    public void setCancelProcessOnExternalsFail(Boolean cancelProcessOnExternalsFail) {
        this.cancelProcessOnExternalsFail = cancelProcessOnExternalsFail;
    }
}
