package com.fit2cloud.devops.service.jenkins.model.common;

import com.fit2cloud.devops.service.jenkins.model.XmlNode;
import com.fit2cloud.devops.service.jenkins.model.common.buildWrapper.BaseBuildWrapperModel;
import com.fit2cloud.devops.service.jenkins.model.common.properties.BasePropertyModel;
import com.fit2cloud.devops.service.jenkins.model.common.publisher.BasePublisherModel;
import com.fit2cloud.devops.service.jenkins.model.common.scm.BaseScmModel;
import com.fit2cloud.devops.service.jenkins.model.common.trigger.BaseTriggerModel;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import java.util.ArrayList;
import java.util.List;

public class BaseJobModel {
    @XStreamOmitField
    private String name;
    @XStreamOmitField
    protected String type;
    @XStreamOmitField
    private String id;
    private String description = "";
    private String jdk = "";
    private Integer quietPeriod;
    private Integer scmCheckoutRetryCount;
    private Boolean canRoam;
    private Boolean keepDependencies;
    private Boolean disabled;
    private Boolean blockBuildWhenDownstreamBuilding;
    private Boolean blockBuildWhenUpstreamBuilding;
    private Boolean concurrentBuild;
    private String authToken;
    private String assignedNode;
    @XStreamAlias("triggers")
    private List<BaseTriggerModel> triggers;
    @XStreamAlias(value = "scm")
    private BaseScmModel scm;
    @XStreamAlias("properties")
    private List<BasePropertyModel> properties;
    @XStreamAlias("publishers")
    private List<BasePublisherModel> publishers;

    @XStreamAlias("buildWrappers")
    private List<BaseBuildWrapperModel> buildWrappers;

    @XStreamOmitField
    private List<XmlNode> unknownNodes;

    public BaseJobModel() {
        this.triggers = new ArrayList<>();
        this.publishers = new ArrayList<>();
        this.buildWrappers = new ArrayList<>();
        this.properties = new ArrayList<>();
        this.unknownNodes = new ArrayList<>();
    }

    public Boolean getCanRoam() {
        return canRoam;
    }

    public Boolean getKeepDependencies() {
        return keepDependencies;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public Boolean getBlockBuildWhenDownstreamBuilding() {
        return blockBuildWhenDownstreamBuilding;
    }

    public Boolean getBlockBuildWhenUpstreamBuilding() {
        return blockBuildWhenUpstreamBuilding;
    }

    public Boolean getConcurrentBuild() {
        return concurrentBuild;
    }

    public String getAssignedNode() {
        return assignedNode;
    }

    public void setAssignedNode(String assignedNode) {
        this.assignedNode = assignedNode;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public List<XmlNode> getUnknownNodes() {
        return unknownNodes;
    }

    public void setUnknownNodes(List<XmlNode> unknownNodes) {
        this.unknownNodes = unknownNodes;
    }

    public Integer getQuietPeriod() {
        return quietPeriod;
    }

    public void setQuietPeriod(Integer quietPeriod) {
        this.quietPeriod = quietPeriod;
    }

    public Integer getScmCheckoutRetryCount() {
        return scmCheckoutRetryCount;
    }

    public void setScmCheckoutRetryCount(Integer scmCheckoutRetryCount) {
        this.scmCheckoutRetryCount = scmCheckoutRetryCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getJdk() {
        return jdk;
    }

    public void setJdk(String jdk) {
        this.jdk = jdk;
    }

    public Boolean isCanRoam() {
        return canRoam;
    }

    public void setCanRoam(Boolean canRoam) {
        this.canRoam = canRoam;
    }

    public Boolean isKeepDependencies() {
        return keepDependencies;
    }

    public void setKeepDependencies(Boolean keepDependencies) {
        this.keepDependencies = keepDependencies;
    }

    public Boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public Boolean isBlockBuildWhenDownstreamBuilding() {
        return blockBuildWhenDownstreamBuilding;
    }

    public void setBlockBuildWhenDownstreamBuilding(Boolean blockBuildWhenDownstreamBuilding) {
        this.blockBuildWhenDownstreamBuilding = blockBuildWhenDownstreamBuilding;
    }

    public Boolean isBlockBuildWhenUpstreamBuilding() {
        return blockBuildWhenUpstreamBuilding;
    }

    public void setBlockBuildWhenUpstreamBuilding(Boolean blockBuildWhenUpstreamBuilding) {
        this.blockBuildWhenUpstreamBuilding = blockBuildWhenUpstreamBuilding;
    }

    public Boolean isConcurrentBuild() {
        return concurrentBuild;
    }

    public void setConcurrentBuild(Boolean concurrentBuild) {
        this.concurrentBuild = concurrentBuild;
    }


    public BaseScmModel getScm() {
        return scm;
    }

    public void setScm(BaseScmModel scm) {
        this.scm = scm;
    }

    public List<BaseTriggerModel> getTriggers() {
        return triggers;
    }

    public void setTriggers(List<BaseTriggerModel> triggers) {
        this.triggers = triggers;
    }

    public List<BasePropertyModel> getProperties() {
        return properties;
    }

    public void setProperties(List<BasePropertyModel> properties) {
        this.properties = properties;
    }

    public List<BasePublisherModel> getPublishers() {
        return publishers;
    }

    public void setPublishers(List<BasePublisherModel> publishers) {
        this.publishers = publishers;
    }


    public List<BaseBuildWrapperModel> getBuildWrappers() {
        return buildWrappers;
    }

    public void setBuildWrappers(List<BaseBuildWrapperModel> buildWrappers) {
        this.buildWrappers = buildWrappers;
    }
}
