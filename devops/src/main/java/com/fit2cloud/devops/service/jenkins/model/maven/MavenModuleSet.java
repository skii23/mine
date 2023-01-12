package com.fit2cloud.devops.service.jenkins.model.maven;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fit2cloud.devops.common.consts.JenkinsConstants;
import com.fit2cloud.devops.service.jenkins.model.common.*;
import com.fit2cloud.devops.service.jenkins.model.common.builder.BaseBuilderModel;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wisonic
 */
@XStreamAlias("maven2-moduleset")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class MavenModuleSet extends BaseJobModel {

    {
        this.type = JenkinsConstants.JobType.MAVEN.name();
    }

    @XStreamAsAttribute
    private String plugin = "maven-plugin@3.4";
    private Boolean aggregatorStyleBuild;
    private Boolean ignoreUpstremChanges;
    private Boolean ignoreUnsuccessfulUpstreams;
    private String reporters;
    private String mavenName;
    private String rootPOM;
    private String goals;
    private String mavenOpts;
    private Boolean incrementalBuild;
    private Boolean archivingDisabled;
    private Boolean siteArchivingDisabled;
    private Boolean fingerprintingDisabled;
    private Boolean disableTriggerDownstreamProjects;
    private Boolean blockTriggerWhenBuilding;
    private Boolean perModuleBuild;
    private Boolean resolveDependencies;
    private Boolean runHeadless;
    private Boolean processPlugins;
    private String customWorkspace;
    private Integer mavenValidationLevel = -1;
    @XStreamAlias("settings")
    private Settings settings;
    @XStreamAlias("globalSettings")
    private GlobalSettings globalSettings;
    @XStreamAlias("prebuilders")
    private List<BaseBuilderModel> preBuilders;
    @XStreamAlias("postbuilders")
    private List<BaseBuilderModel> postBuilders;
    @XStreamAlias("rootModule")
    private RootModule rootModule;
    @XStreamAlias("runPostStepsIfResult")
    private RunPostStepsIfResult runPostStepsIfResult;
    @XStreamAlias("localRepository")
    private LocalRepository localRepository;
    public MavenModuleSet() {
        this.preBuilders = new ArrayList<>();
        this.postBuilders = new ArrayList<>();
    }

    public RunPostStepsIfResult getRunPostStepsIfResult() {
        return runPostStepsIfResult;
    }

    public void setRunPostStepsIfResult(RunPostStepsIfResult runPostStepsIfResult) {
        this.runPostStepsIfResult = runPostStepsIfResult;
    }

    public Boolean getAggregatorStyleBuild() {
        return aggregatorStyleBuild;
    }

    public Boolean getIgnoreUpstremChanges() {
        return ignoreUpstremChanges;
    }

    public Boolean getIgnoreUnsuccessfulUpstreams() {
        return ignoreUnsuccessfulUpstreams;
    }

    public Boolean getIncrementalBuild() {
        return incrementalBuild;
    }

    public Boolean getArchivingDisabled() {
        return archivingDisabled;
    }

    public Boolean getSiteArchivingDisabled() {
        return siteArchivingDisabled;
    }

    public Boolean getFingerprintingDisabled() {
        return fingerprintingDisabled;
    }

    public Boolean getDisableTriggerDownstreamProjects() {
        return disableTriggerDownstreamProjects;
    }

    public Boolean getBlockTriggerWhenBuilding() {
        return blockTriggerWhenBuilding;
    }

    public Boolean getPerModuleBuild() {
        return perModuleBuild;
    }

    public Boolean getResolveDependencies() {
        return resolveDependencies;
    }

    public Boolean getRunHeadless() {
        return runHeadless;
    }

    public Boolean getProcessPlugins() {
        return processPlugins;
    }

    public Boolean isUseCustomWorkspace() {
        return StringUtils.isBlank(this.customWorkspace);
    }

    public Boolean isUsePrivateMavenRepo() {
        return this.localRepository != null && StringUtils.isNotBlank(this.localRepository.getClassStr());
    }

    public String getCustomWorkspace() {
        return customWorkspace;
    }

    public void setCustomWorkspace(String customWorkspace) {
        this.customWorkspace = customWorkspace;
    }

    public Boolean isPerModuleBuild() {
        return perModuleBuild;
    }

    public void setPerModuleBuild(Boolean perModuleBuild) {
        this.perModuleBuild = perModuleBuild;
    }

    public LocalRepository getLocalRepository() {
        return localRepository;
    }

    public void setLocalRepository(LocalRepository localRepository) {
        this.localRepository = localRepository;
    }

    public String getMavenOpts() {
        return mavenOpts;
    }

    public void setMavenOpts(String mavenOpts) {
        this.mavenOpts = mavenOpts;
    }

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public Boolean isAggregatorStyleBuild() {
        return aggregatorStyleBuild;
    }

    public void setAggregatorStyleBuild(Boolean aggregatorStyleBuild) {
        this.aggregatorStyleBuild = aggregatorStyleBuild;
    }

    public Boolean isIncrementalBuild() {
        return incrementalBuild;
    }

    public void setIncrementalBuild(Boolean incrementalBuild) {
        this.incrementalBuild = incrementalBuild;
    }

    public Boolean isIgnoreUpstremChanges() {
        return ignoreUpstremChanges;
    }

    public void setIgnoreUpstremChanges(Boolean ignoreUpstremChanges) {
        this.ignoreUpstremChanges = ignoreUpstremChanges;
    }

    public Boolean isIgnoreUnsuccessfulUpstreams() {
        return ignoreUnsuccessfulUpstreams;
    }

    public void setIgnoreUnsuccessfulUpstreams(Boolean ignoreUnsuccessfulUpstreams) {
        this.ignoreUnsuccessfulUpstreams = ignoreUnsuccessfulUpstreams;
    }

    public Boolean isArchivingDisabled() {
        return archivingDisabled;
    }

    public void setArchivingDisabled(Boolean archivingDisabled) {
        this.archivingDisabled = archivingDisabled;
    }

    public Boolean isSiteArchivingDisabled() {
        return siteArchivingDisabled;
    }

    public void setSiteArchivingDisabled(Boolean siteArchivingDisabled) {
        this.siteArchivingDisabled = siteArchivingDisabled;
    }

    public Boolean isFingerprintingDisabled() {
        return fingerprintingDisabled;
    }

    public void setFingerprintingDisabled(Boolean fingerprintingDisabled) {
        this.fingerprintingDisabled = fingerprintingDisabled;
    }

    public Boolean isResolveDependencies() {
        return resolveDependencies;
    }

    public void setResolveDependencies(Boolean resolveDependencies) {
        this.resolveDependencies = resolveDependencies;
    }

    public Boolean isProcessPlugins() {
        return processPlugins;
    }

    public void setProcessPlugins(Boolean processPlugins) {
        this.processPlugins = processPlugins;
    }

    public Boolean isRunHeadless() {
        return runHeadless;
    }

    public void setRunHeadless(Boolean runHeadless) {
        this.runHeadless = runHeadless;
    }

    public Boolean isDisableTriggerDownstreamProjects() {
        return disableTriggerDownstreamProjects;
    }

    public void setDisableTriggerDownstreamProjects(Boolean disableTriggerDownstreamProjects) {
        this.disableTriggerDownstreamProjects = disableTriggerDownstreamProjects;
    }

    public Boolean isBlockTriggerWhenBuilding() {
        return blockTriggerWhenBuilding;
    }

    public void setBlockTriggerWhenBuilding(Boolean blockTriggerWhenBuilding) {
        this.blockTriggerWhenBuilding = blockTriggerWhenBuilding;
    }

    public Integer getMavenValidationLevel() {
        return mavenValidationLevel;
    }

    public void setMavenValidationLevel(Integer mavenValidationLevel) {
        this.mavenValidationLevel = mavenValidationLevel;
    }

    public String getReporters() {
        return reporters;
    }

    public void setReporters(String reporters) {
        this.reporters = reporters;
    }

    public String getMavenName() {
        return mavenName;
    }

    public void setMavenName(String mavenName) {
        this.mavenName = mavenName;
    }

    public String getRootPOM() {
        return rootPOM;
    }

    public void setRootPOM(String rootPOM) {
        this.rootPOM = rootPOM;
    }

    public String getGoals() {
        return goals;
    }

    public void setGoals(String goals) {
        this.goals = goals;
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public GlobalSettings getGlobalSettings() {
        return globalSettings;
    }

    public void setGlobalSettings(GlobalSettings globalSettings) {
        this.globalSettings = globalSettings;
    }

    public List<BaseBuilderModel> getPreBuilders() {
        return preBuilders;
    }

    public void setPreBuilders(List<BaseBuilderModel> preBuilders) {
        this.preBuilders = preBuilders;
    }

    public List<BaseBuilderModel> getPostBuilders() {
        return postBuilders;
    }

    public void setPostBuilders(List<BaseBuilderModel> postBuilders) {
        this.postBuilders = postBuilders;
    }

    public RootModule getRootModule() {
        return rootModule;
    }

    public void setRootModule(RootModule rootModule) {
        this.rootModule = rootModule;
    }
}
