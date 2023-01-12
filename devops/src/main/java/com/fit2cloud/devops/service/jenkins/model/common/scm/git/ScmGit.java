package com.fit2cloud.devops.service.jenkins.model.common.scm.git;

import com.fit2cloud.devops.service.jenkins.handler.obj2xml.BasePropertiesTransformer;
import com.fit2cloud.devops.service.jenkins.model.common.scm.BaseScmModel;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import java.util.List;

/**
 * 源码管理
 */
@XStreamAlias(value = "scm")
public class ScmGit extends BaseScmModel {

    /**
     * 插件版本
     */
    @XStreamAsAttribute
    private String plugin;

    private String configVersion = "2";
    /**
     * 源码地址配置
     */
    @XStreamAlias("userRemoteConfigs")
    private List<UserRemoteConfig> userRemoteConfigs;
    /**
     * 分支配置
     */
    @XStreamAlias(value = "branches")
    private List<BranchSpec> branches;
    @XStreamAlias("submoduleCfg")
    private SubmoduleCfg submoduleCfg;

    private Boolean doGenerateSubmoduleConfigurations = false;

    public ScmGit(){
        this.type = BasePropertiesTransformer.ScmTypeHolder.SCM_GIT;
        this.classStr = "hudson.plugins.git.GitSCM";
    }

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public Boolean getDoGenerateSubmoduleConfigurations() {
        return doGenerateSubmoduleConfigurations;
    }

    public void setDoGenerateSubmoduleConfigurations(Boolean doGenerateSubmoduleConfigurations) {
        this.doGenerateSubmoduleConfigurations = doGenerateSubmoduleConfigurations;
    }

    public List<UserRemoteConfig> getUserRemoteConfigs() {
        return userRemoteConfigs;
    }

    public void setUserRemoteConfigs(List<UserRemoteConfig> userRemoteConfigs) {
        this.userRemoteConfigs = userRemoteConfigs;
    }

    public List<BranchSpec> getBranches() {
        return branches;
    }

    public void setBranches(List<BranchSpec> branches) {
        this.branches = branches;
    }

    public String getConfigVersion() {
        return configVersion;
    }

    public void setConfigVersion(String configVersion) {
        this.configVersion = configVersion;
    }

    public Boolean isDoGenerateSubmoduleConfigurations() {
        return doGenerateSubmoduleConfigurations;
    }

    public SubmoduleCfg getSubmoduleCfg() {
        return submoduleCfg;
    }

    public void setSubmoduleCfg(SubmoduleCfg submoduleCfg) {
        this.submoduleCfg = submoduleCfg;
    }

    @Override
    public String toString() {
        return "ScmGit{" +
                "type='" + type + '\'' +
                ", classStr='" + classStr + '\'' +
                ", plugin='" + plugin + '\'' +
                ", userRemoteConfigs=" + userRemoteConfigs +
                ", branches=" + branches +
                ", submoduleCfg=" + submoduleCfg +
                ", configVersion='" + configVersion + '\'' +
                ", doGenerateSubmoduleConfigurations=" + doGenerateSubmoduleConfigurations +
                '}';
    }
}
