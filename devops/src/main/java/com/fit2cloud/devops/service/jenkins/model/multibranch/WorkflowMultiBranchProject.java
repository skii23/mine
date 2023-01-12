package com.fit2cloud.devops.service.jenkins.model.multibranch;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fit2cloud.devops.common.util.XmlUtils;
import com.fit2cloud.devops.service.jenkins.model.common.BaseJobModel;
import com.fit2cloud.devops.service.jenkins.model.common.actions.Actions;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import org.jdom2.Element;

/**
 * @author caiwzh
 * @date 2022/8/11
 */
@XStreamAlias("org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE)
public class WorkflowMultiBranchProject extends BaseJobModel {

    //默认属性properties
    public static final String DEFAULT_PROPERTIES_XML="<properties>\n" +
            "    <org.jenkinsci.plugins.docker.workflow.declarative.FolderConfig plugin=\"docker-workflow@1.29\">\n" +
            "      <dockerLabel></dockerLabel>\n" +
            "      <registry plugin=\"docker-commons@1.19\"/>\n" +
            "    </org.jenkinsci.plugins.docker.workflow.declarative.FolderConfig>\n" +
            "  </properties>";
    public static final EleFunction PROPERTIES_ELE = () -> XmlUtils.stringToXmlElement(DEFAULT_PROPERTIES_XML);

    //默认属性folderViews
    public static final String DEFAULT_FOLDERVIEWS_XML="<folderViews class=\"jenkins.branch.MultiBranchProjectViewHolder\" plugin=\"branch-api@2.1046.v0ca_37783ecc5\">\n" +
            "    <owner class=\"org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject\" reference=\"../..\"/>\n" +
            "  </folderViews>";
    public static final EleFunction FOLDERVIEWS_ELE = () -> XmlUtils.stringToXmlElement(DEFAULT_FOLDERVIEWS_XML);

    //默认属性healthMetrics
    public static final String DEFAULT_HEALTHMETRICS_XML="<healthMetrics/>";
    public static final EleFunction HEALTHMETRICS_ELE = () -> XmlUtils.stringToXmlElement(DEFAULT_HEALTHMETRICS_XML);

    //默认属性icon
    public static final String DEFAULT_ICON_XML="<icon class=\"jenkins.branch.MetadataActionFolderIcon\" plugin=\"branch-api@2.1046.v0ca_37783ecc5\">\n" +
            "    <owner class=\"org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject\" reference=\"../..\"/>\n" +
            "  </icon>";
    public static final EleFunction ICON_ELE = () -> XmlUtils.stringToXmlElement(DEFAULT_ICON_XML);

    //默认属性orphanedItemStrategy
    public static final String DEFAULT_ORPHANEDITEMSTRATEGY_XML="<orphanedItemStrategy class=\"com.cloudbees.hudson.plugins.folder.computed.DefaultOrphanedItemStrategy\" plugin=\"cloudbees-folder@6.729.v2b_9d1a_74d673\">\n" +
            "    <pruneDeadBranches>true</pruneDeadBranches>\n" +
            "    <daysToKeep>-1</daysToKeep>\n" +
            "    <numToKeep>-1</numToKeep>\n" +
            "    <abortBuilds>false</abortBuilds>\n" +
            "  </orphanedItemStrategy>";
    public static final EleFunction ORPHANEDITEMSTRATEGY_ELE = () -> XmlUtils.stringToXmlElement(DEFAULT_ORPHANEDITEMSTRATEGY_XML);

    @XStreamAsAttribute
    private String plugin = "workflow-multibranch@716.vc692a_e52371b_";

    private Actions actions;

    private String displayName;

    private BranchSourceList sources;

    private Factory factory;

    public Factory getFactory() {
        return factory;
    }

    public void setFactory(Factory factory) {
        this.factory = factory;
    }

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public Actions getActions() {
        return actions;
    }

    public void setActions(Actions actions) {
        this.actions = actions;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public BranchSourceList getSources() {
        return sources;
    }

    public void setSources(BranchSourceList sources) {
        this.sources = sources;
    }

    @FunctionalInterface
    public interface EleFunction{
        Element get();
    }
}
