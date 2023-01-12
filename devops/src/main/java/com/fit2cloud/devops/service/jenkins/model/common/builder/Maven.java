package com.fit2cloud.devops.service.jenkins.model.common.builder;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fit2cloud.devops.service.jenkins.handler.obj2xml.BuilderTransformer;
import com.fit2cloud.devops.service.jenkins.handler.xml2obj.BuilderParser;
import com.fit2cloud.devops.service.jenkins.model.AbstractBaseModel;
import com.fit2cloud.devops.service.jenkins.model.common.GlobalSettings;
import com.fit2cloud.devops.service.jenkins.model.common.Settings;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("hudson.tasks.Maven")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE)
public class Maven extends BaseBuilderModel {

    {
        this.type = BuilderTransformer.BuilderTypeHolder.MAVEN;
    }

    private String targets;
    private String mavenName;
    private String jvmOptions;
    private String pom;
    private String properties;
    private Boolean usePrivateRepository;
    private Boolean injectBuildVariables;
    @XStreamAlias("settings")
    private Settings settings;
    @XStreamAlias("globalSettings")
    private GlobalSettings globalSettings;

    public String getTargets() {
        return targets;
    }

    public void setTargets(String targets) {
        this.targets = targets;
    }

    public String getMavenName() {
        return mavenName;
    }

    public void setMavenName(String mavenName) {
        this.mavenName = mavenName;
    }

    public String getJvmOptions() {
        return jvmOptions;
    }

    public void setJvmOptions(String jvmOptions) {
        this.jvmOptions = jvmOptions;
    }

    public String getPom() {
        return pom;
    }

    public void setPom(String pom) {
        this.pom = pom;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public Boolean getUsePrivateRepository() {
        return usePrivateRepository;
    }

    public void setUsePrivateRepository(Boolean usePrivateRepository) {
        this.usePrivateRepository = usePrivateRepository;
    }

    public Boolean getInjectBuildVariables() {
        return injectBuildVariables;
    }

    public void setInjectBuildVariables(Boolean injectBuildVariables) {
        this.injectBuildVariables = injectBuildVariables;
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

}
