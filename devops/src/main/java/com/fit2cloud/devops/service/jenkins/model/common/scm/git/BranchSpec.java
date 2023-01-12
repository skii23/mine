package com.fit2cloud.devops.service.jenkins.model.common.scm.git;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 分支名称
 */
@XStreamAlias("hudson.plugins.git.BranchSpec")
public class BranchSpec {
    /**
     * 分支名称
     */
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
