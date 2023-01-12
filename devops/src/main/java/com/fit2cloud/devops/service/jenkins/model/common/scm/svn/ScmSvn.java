package com.fit2cloud.devops.service.jenkins.model.common.scm.svn;

import com.fit2cloud.devops.service.jenkins.handler.obj2xml.BasePropertiesTransformer;
import com.fit2cloud.devops.service.jenkins.model.common.properties.parameters.parameter.StringParameterDefinition;
import com.fit2cloud.devops.service.jenkins.model.common.scm.BaseScmModel;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import java.util.List;

@XStreamAlias("scm")
public class ScmSvn extends BaseScmModel {

    public ScmSvn(){
        super.type = BasePropertiesTransformer.ScmTypeHolder.SCM_SVN;
        this.classStr = "hudson.scm.SubversionSCM";
    }

    @XStreamAsAttribute
    private String plugin;



    @XStreamAlias("locations")
    private List<ModuleLocation> locations;

    private String excludedRegions;
    private String includedRegions;
    private String excludedUsers;
    private String excludedRevprop;
    private String excludedCommitMessages;

    @XStreamAlias("workspaceUpdater")
    private WorkspaceUpdater workspaceUpdater;

    private Boolean ignoreDirPropChanges = false;
    private Boolean filterChangelog = false;
    private Boolean quietOperation = true;

    public List<ModuleLocation> getLocations() {
        return locations;
    }

    public void setLocations(List<ModuleLocation> locations) {
        this.locations = locations;
    }

    public Boolean getIgnoreDirPropChanges() {
        return ignoreDirPropChanges;
    }

    public Boolean getFilterChangelog() {
        return filterChangelog;
    }

    public Boolean getQuietOperation() {
        return quietOperation;
    }

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public WorkspaceUpdater getWorkspaceUpdater() {
        return workspaceUpdater;
    }

    public void setWorkspaceUpdater(WorkspaceUpdater workspaceUpdater) {
        this.workspaceUpdater = workspaceUpdater;
    }

    public String getExcludedRegions() {
        return excludedRegions;
    }

    public void setExcludedRegions(String excludedRegions) {
        this.excludedRegions = excludedRegions;
    }

    public String getIncludedRegions() {
        return includedRegions;
    }

    public void setIncludedRegions(String includedRegions) {
        this.includedRegions = includedRegions;
    }

    public String getExcludedUsers() {
        return excludedUsers;
    }

    public void setExcludedUsers(String excludedUsers) {
        this.excludedUsers = excludedUsers;
    }

    public String getExcludedRevprop() {
        return excludedRevprop;
    }

    public void setExcludedRevprop(String excludedRevprop) {
        this.excludedRevprop = excludedRevprop;
    }

    public String getExcludedCommitMessages() {
        return excludedCommitMessages;
    }

    public void setExcludedCommitMessages(String excludedCommitMessages) {
        this.excludedCommitMessages = excludedCommitMessages;
    }

    public boolean isIgnoreDirPropChanges() {
        return ignoreDirPropChanges;
    }

    public void setIgnoreDirPropChanges(Boolean ignoreDirPropChanges) {
        this.ignoreDirPropChanges = ignoreDirPropChanges;
    }

    public void setIgnoreDirPropChanges(boolean ignoreDirPropChanges) {
        this.ignoreDirPropChanges = ignoreDirPropChanges;
    }

    public boolean isFilterChangelog() {
        return filterChangelog;
    }

    public void setFilterChangelog(Boolean filterChangelog) {
        this.filterChangelog = filterChangelog;
    }

    public void setFilterChangelog(boolean filterChangelog) {
        this.filterChangelog = filterChangelog;
    }

    public boolean isQuietOperation() {
        return quietOperation;
    }

    public void setQuietOperation(Boolean quietOperation) {
        this.quietOperation = quietOperation;
    }

    public void setQuietOperation(boolean quietOperation) {
        this.quietOperation = quietOperation;
    }

    @Override
    public String toString() {
        return "ScmSvn{" +
                "type='" + type + '\'' +
                ", classStr='" + classStr + '\'' +
                ", plugin='" + plugin + '\'' +
                ", workspaceUpdater=" + workspaceUpdater +
                ", locations=" + locations +
                ", excludedRegions='" + excludedRegions + '\'' +
                ", includedRegions='" + includedRegions + '\'' +
                ", excludedUsers='" + excludedUsers + '\'' +
                ", excludedRevprop='" + excludedRevprop + '\'' +
                ", excludedCommitMessages='" + excludedCommitMessages + '\'' +
                ", ignoreDirPropChanges=" + ignoreDirPropChanges +
                ", filterChangelog=" + filterChangelog +
                ", quietOperation=" + quietOperation +
                '}';
    }
}
