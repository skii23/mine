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
public class GitLabSCMSource extends AbstractBaseModel {
    //默认traits xml节点
    public static final String DEFAULT_GITLAB_TRAITS_XML = "<traits>\n" +
            "            <io.jenkins.plugins.gitlabbranchsource.BranchDiscoveryTrait>\n" +
            "              <strategyId>1</strategyId>\n" +
            "            </io.jenkins.plugins.gitlabbranchsource.BranchDiscoveryTrait>\n" +
            "            <io.jenkins.plugins.gitlabbranchsource.OriginMergeRequestDiscoveryTrait>\n" +
            "              <strategyId>1</strategyId>\n" +
            "            </io.jenkins.plugins.gitlabbranchsource.OriginMergeRequestDiscoveryTrait>\n" +
            "            <io.jenkins.plugins.gitlabbranchsource.ForkMergeRequestDiscoveryTrait>\n" +
            "              <strategyId>1</strategyId>\n" +
            "              <trust class=\"io.jenkins.plugins.gitlabbranchsource.ForkMergeRequestDiscoveryTrait$TrustPermission\"/>\n" +
            "            </io.jenkins.plugins.gitlabbranchsource.ForkMergeRequestDiscoveryTrait>\n" +
            "          </traits>";

    public static final WorkflowMultiBranchProject.EleFunction GITLAB_TRAITS_ELE =() -> XmlUtils.stringToXmlElement(DEFAULT_GITLAB_TRAITS_XML);

    @XStreamAlias("class")
    @XStreamAsAttribute
    private String clazz= "io.jenkins.plugins.gitlabbranchsource.GitLabSCMSource";

    @XStreamAsAttribute
    private String plugin= "gitlab-branch-source@633.ved9984f943da_";

    private String id;

    private String serverName;

    private String projectOwner;

    private String projectPath;

    private String credentialsId;

    private String sshRemote;

    private String httpRemote;

    private String projectId;

    public GitLabSCMSource() {
        this.type = WorkflowMultiBranchTransformer.TypeHolder.GITLAB;
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

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getProjectOwner() {
        return projectOwner;
    }

    public void setProjectOwner(String projectOwner) {
        this.projectOwner = projectOwner;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    public String getSshRemote() {
        return sshRemote;
    }

    public void setSshRemote(String sshRemote) {
        this.sshRemote = sshRemote;
    }

    public String getHttpRemote() {
        return httpRemote;
    }

    public void setHttpRemote(String httpRemote) {
        this.httpRemote = httpRemote;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
}
