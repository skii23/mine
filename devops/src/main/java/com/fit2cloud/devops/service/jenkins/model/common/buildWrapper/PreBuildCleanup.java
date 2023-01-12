package com.fit2cloud.devops.service.jenkins.model.common.buildWrapper;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fit2cloud.devops.service.jenkins.handler.obj2xml.BuildWrapperTransformer;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import java.util.List;

@XStreamAlias("hudson.plugins.ws__cleanup.PreBuildCleanup")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE)
public class PreBuildCleanup extends BaseBuildWrapperModel{


    {
        this.type = BuildWrapperTransformer.BuildWrapperTypeHolder.PRE_BUILD_CLEANUP;
    }

    @XStreamAsAttribute
    private String plugin = "ws-cleanup@0.42";

    private List<Pattern> patterns;

    private Boolean deleteDirs;

    private String cleanupParameter;

    private String externalDelete;

    private Boolean disableDeferredWipeout;

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        // 避免被旧插件名称覆盖 强制替换插件名称
        if (!this.plugin.equalsIgnoreCase(plugin)){
            return;
        }
        this.plugin = plugin;
    }

    public List<Pattern> getPatterns() {
        return patterns;
    }

    public void setPatterns(List<Pattern> patterns) {
        this.patterns = patterns;
    }

    public Boolean getDeleteDirs() {
        return deleteDirs;
    }

    public void setDeleteDirs(Boolean deleteDirs) {
        this.deleteDirs = deleteDirs;
    }

    public String getCleanupParameter() {
        return cleanupParameter;
    }

    public void setCleanupParameter(String cleanupParameter) {
        this.cleanupParameter = cleanupParameter;
    }

    public String getExternalDelete() {
        return externalDelete;
    }

    public void setExternalDelete(String externalDelete) {
        this.externalDelete = externalDelete;
    }

    public Boolean getDisableDeferredWipeout() {
        return disableDeferredWipeout;
    }

    public void setDisableDeferredWipeout(Boolean disableDeferredWipeout) {
        this.disableDeferredWipeout = disableDeferredWipeout;
    }
}
