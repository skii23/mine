package com.fit2cloud.devops.service.jenkins.model.common.scm.git;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("hudson.plugins.git.extensions.impl.RelativeTargetDirectory")
public class Extension {

    private String relativeTargetDir;

    public String getRelativeTargetDir() {
        return relativeTargetDir;
    }

    public void setRelativeTargetDir(String relativeTargetDir) {
        this.relativeTargetDir = relativeTargetDir;
    }
}
