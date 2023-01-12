package com.fit2cloud.devops.service.jenkins.model.maven;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("rootModule")
public class RootModule {
    private String groupId;
    private String artifactId;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }
}
