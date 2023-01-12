package com.fit2cloud.devops.service.jenkins.model.common.scm.git;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 源码地址配置
 * @author wisonic
 */
@XStreamAlias("hudson.plugins.git.UserRemoteConfig")
public class UserRemoteConfig {

    /**
     * url
     */
    @XStreamAlias("url")
    private String url;

    /**
     * 凭据id
     */
    @XStreamAlias("credentialsId")
    private String credentialsId;

    @XStreamAlias("name")
    private String name;

    @XStreamAlias("refspec")
    private String refspec;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRefspec() {
        return refspec;
    }

    public void setRefspec(String refspec) {
        this.refspec = refspec;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }
}
