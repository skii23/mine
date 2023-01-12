package com.fit2cloud.devops.service.jenkins.model.common.scm.svn;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Check-out Strategy
 */
@XStreamAlias("workspaceUpdater")
public class WorkspaceUpdater {

    @XStreamAsAttribute
    @XStreamAlias("class")
    private String classStr = "hudson.scm.subversion.UpdateUpdater";

    public String getClassStr() {
        return classStr;
    }

    public void setClassStr(String classStr) {
        this.classStr = classStr;
    }
}
