package com.fit2cloud.devops.service.jenkins.model.common.trigger;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fit2cloud.devops.service.jenkins.handler.obj2xml.TriggerTransformer;
import com.fit2cloud.devops.service.jenkins.model.common.properties.discarder.strategy.BaseStrategyModel;
import com.fit2cloud.devops.service.jenkins.model.common.trigger.filter.MergeRequestLabelFilterConfig;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("com.dabsquared.gitlabjenkins.GitLabPushTrigger")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE)
public class GitLabPushTrigger extends BaseTriggerModel {

    {
        this.type = TriggerTransformer.TriggerTypeHolder.GITLAB_PUSH_TRIGGER;
    }

    @XStreamAsAttribute
    private String plugin = "gitlab-plugin@1.5.20";

    private String spec;

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    private Boolean triggerOnPush;
    private Boolean triggerToBranchDeleteRequest;


    private Boolean triggerOnMergeRequest;
    private Boolean triggerOnlyIfNewCommitsPushed;

    private Boolean triggerOnPipelineEvent;

    private Boolean triggerOnAcceptedMergeRequest;
    private Boolean triggerOnClosedMergeRequest;
    private Boolean triggerOnApprovedMergeRequest;

    // never;source;both
    private String triggerOpenMergeRequestOnPush;



    private Boolean triggerOnNoteRequest;

    private String noteRegex;

    // 高级隐藏内容
    private Boolean ciSkip;
    private Boolean skipWorkInProgressMergeRequest;

    private String labelsThatForcesBuildIfAdded;

    private Boolean setBuildDescription;






    // All;NameBasedFilter;RegexBasedFilter   NameBasedFilter 会触发分支名称入参的接口检测？？
    private String branchFilterType;

    private String includeBranchesSpec;
    private String excludeBranchesSpec;
    private String sourceBranchRegex;
    private String targetBranchRegex;

    // 因为不存在多态继承 直接用原始类
    private MergeRequestLabelFilterConfig mergeRequestLabelFilterConfig;

    private String secretToken;

    private String pendingBuildName;
    private Boolean cancelPendingBuildsOnUpdate;


    public Boolean getTriggerOnPush() {
        return triggerOnPush;
    }

    public void setTriggerOnPush(Boolean triggerOnPush) {
        this.triggerOnPush = triggerOnPush;
    }

    public Boolean getTriggerToBranchDeleteRequest() {
        return triggerToBranchDeleteRequest;
    }

    public void setTriggerToBranchDeleteRequest(Boolean triggerToBranchDeleteRequest) {
        this.triggerToBranchDeleteRequest = triggerToBranchDeleteRequest;
    }

    public Boolean getTriggerOnMergeRequest() {
        return triggerOnMergeRequest;
    }

    public void setTriggerOnMergeRequest(Boolean triggerOnMergeRequest) {
        this.triggerOnMergeRequest = triggerOnMergeRequest;
    }

    public Boolean getTriggerOnlyIfNewCommitsPushed() {
        return triggerOnlyIfNewCommitsPushed;
    }

    public void setTriggerOnlyIfNewCommitsPushed(Boolean triggerOnlyIfNewCommitsPushed) {
        this.triggerOnlyIfNewCommitsPushed = triggerOnlyIfNewCommitsPushed;
    }

    public Boolean getTriggerOnAcceptedMergeRequest() {
        return triggerOnAcceptedMergeRequest;
    }

    public void setTriggerOnAcceptedMergeRequest(Boolean triggerOnAcceptedMergeRequest) {
        this.triggerOnAcceptedMergeRequest = triggerOnAcceptedMergeRequest;
    }

    public Boolean getTriggerOnClosedMergeRequest() {
        return triggerOnClosedMergeRequest;
    }

    public void setTriggerOnClosedMergeRequest(Boolean triggerOnClosedMergeRequest) {
        this.triggerOnClosedMergeRequest = triggerOnClosedMergeRequest;
    }

    public String getTriggerOpenMergeRequestOnPush() {
        return triggerOpenMergeRequestOnPush;
    }

    public void setTriggerOpenMergeRequestOnPush(String triggerOpenMergeRequestOnPush) {
        this.triggerOpenMergeRequestOnPush = triggerOpenMergeRequestOnPush;
    }

    public Boolean getTriggerOnApprovedMergeRequest() {
        return triggerOnApprovedMergeRequest;
    }

    public void setTriggerOnApprovedMergeRequest(Boolean triggerOnApprovedMergeRequest) {
        this.triggerOnApprovedMergeRequest = triggerOnApprovedMergeRequest;
    }

    public Boolean getTriggerOnNoteRequest() {
        return triggerOnNoteRequest;
    }

    public void setTriggerOnNoteRequest(Boolean triggerOnNoteRequest) {
        this.triggerOnNoteRequest = triggerOnNoteRequest;
    }

    public String getNoteRegex() {
        return noteRegex;
    }

    public void setNoteRegex(String noteRegex) {
        this.noteRegex = noteRegex;
    }

    public Boolean getCiSkip() {
        return ciSkip;
    }

    public void setCiSkip(Boolean ciSkip) {
        this.ciSkip = ciSkip;
    }

    public Boolean getSkipWorkInProgressMergeRequest() {
        return skipWorkInProgressMergeRequest;
    }

    public void setSkipWorkInProgressMergeRequest(Boolean skipWorkInProgressMergeRequest) {
        this.skipWorkInProgressMergeRequest = skipWorkInProgressMergeRequest;
    }

    public String getLabelsThatForcesBuildIfAdded() {
        return labelsThatForcesBuildIfAdded;
    }

    public void setLabelsThatForcesBuildIfAdded(String labelsThatForcesBuildIfAdded) {
        this.labelsThatForcesBuildIfAdded = labelsThatForcesBuildIfAdded;
    }

    public Boolean getSetBuildDescription() {
        return setBuildDescription;
    }

    public void setSetBuildDescription(Boolean setBuildDescription) {
        this.setBuildDescription = setBuildDescription;
    }

    public Boolean getTriggerOnPipelineEvent() {
        return triggerOnPipelineEvent;
    }

    public void setTriggerOnPipelineEvent(Boolean triggerOnPipelineEvent) {
        this.triggerOnPipelineEvent = triggerOnPipelineEvent;
    }

    public String getPendingBuildName() {
        return pendingBuildName;
    }

    public void setPendingBuildName(String pendingBuildName) {
        this.pendingBuildName = pendingBuildName;
    }

    public Boolean getCancelPendingBuildsOnUpdate() {
        return cancelPendingBuildsOnUpdate;
    }

    public void setCancelPendingBuildsOnUpdate(Boolean cancelPendingBuildsOnUpdate) {
        this.cancelPendingBuildsOnUpdate = cancelPendingBuildsOnUpdate;
    }

    public String getBranchFilterType() {
        return branchFilterType;
    }

    public void setBranchFilterType(String branchFilterType) {
        this.branchFilterType = branchFilterType;
    }

    public String getIncludeBranchesSpec() {
        return includeBranchesSpec;
    }

    public void setIncludeBranchesSpec(String includeBranchesSpec) {
        this.includeBranchesSpec = includeBranchesSpec;
    }

    public String getExcludeBranchesSpec() {
        return excludeBranchesSpec;
    }

    public void setExcludeBranchesSpec(String excludeBranchesSpec) {
        this.excludeBranchesSpec = excludeBranchesSpec;
    }

    public String getSourceBranchRegex() {
        return sourceBranchRegex;
    }

    public void setSourceBranchRegex(String sourceBranchRegex) {
        this.sourceBranchRegex = sourceBranchRegex;
    }

    public String getTargetBranchRegex() {
        return targetBranchRegex;
    }

    public void setTargetBranchRegex(String targetBranchRegex) {
        this.targetBranchRegex = targetBranchRegex;
    }

    public MergeRequestLabelFilterConfig getMergeRequestLabelFilterConfig() {
        return mergeRequestLabelFilterConfig;
    }

    public void setMergeRequestLabelFilterConfig(MergeRequestLabelFilterConfig mergeRequestLabelFilterConfig) {
        this.mergeRequestLabelFilterConfig = mergeRequestLabelFilterConfig;
    }

    public String getSecretToken() {
        return secretToken;
    }

    public void setSecretToken(String secretToken) {
        this.secretToken = secretToken;
    }

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }
}
