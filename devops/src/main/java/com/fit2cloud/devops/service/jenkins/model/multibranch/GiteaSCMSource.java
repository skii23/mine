package com.fit2cloud.devops.service.jenkins.model.multibranch;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fit2cloud.devops.common.util.XmlUtils;
import com.fit2cloud.devops.service.jenkins.handler.obj2xml.WorkflowMultiBranchTransformer;
import com.fit2cloud.devops.service.jenkins.model.AbstractBaseModel;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * @author caiwzh
 * @date 2022/8/19
 */
@XStreamAlias("source")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE)
public class GiteaSCMSource extends AbstractBaseModel {
    //默认traits xml节点
    public static final String DEFAULT_GITEA_TRAITS_XML = "<traits>\n" +
            "            <org.jenkinsci.plugin.gitea.BranchDiscoveryTrait>\n" +
            "              <strategyId>1</strategyId>\n" +
            "            </org.jenkinsci.plugin.gitea.BranchDiscoveryTrait>\n" +
            "            <org.jenkinsci.plugin.gitea.OriginPullRequestDiscoveryTrait>\n" +
            "              <strategyId>1</strategyId>\n" +
            "            </org.jenkinsci.plugin.gitea.OriginPullRequestDiscoveryTrait>\n" +
            "            <org.jenkinsci.plugin.gitea.ForkPullRequestDiscoveryTrait>\n" +
            "              <strategyId>1</strategyId>\n" +
            "              <trust class=\"org.jenkinsci.plugin.gitea.ForkPullRequestDiscoveryTrait$TrustContributors\"/>\n" +
            "            </org.jenkinsci.plugin.gitea.ForkPullRequestDiscoveryTrait>\n" +
            "          </traits>";

    public static final WorkflowMultiBranchProject.EleFunction GITEA_TRAITS_ELE =() -> XmlUtils.stringToXmlElement(DEFAULT_GITEA_TRAITS_XML);

    @XStreamAlias("class")
    @XStreamAsAttribute
    private String clazz= "org.jenkinsci.plugin.gitea.GiteaSCMSource";

    @XStreamAsAttribute
    private String plugin= "gitea@1.4.3";

    private String id;

    private String serverUrl;

    private String repoOwner;

    private String repository;

    private String credentialsId;

    public GiteaSCMSource() {
        this.type = WorkflowMultiBranchTransformer.TypeHolder.GITEA;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getRepoOwner() {
        return repoOwner;
    }

    public void setRepoOwner(String repoOwner) {
        this.repoOwner = repoOwner;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }
}
