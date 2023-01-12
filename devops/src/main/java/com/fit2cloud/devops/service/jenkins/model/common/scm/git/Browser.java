package com.fit2cloud.devops.service.jenkins.model.common.scm.git;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias(value = "browser")
public class Browser {

    @XStreamAsAttribute
    private String plugin = "gitea@1.4.3";

    @XStreamAlias("class")
    @XStreamAsAttribute
    private String clazzStr = "org.jenkinsci.plugin.gitea.GiteaBrowser";

    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
