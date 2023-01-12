package com.fit2cloud.devops.service.jenkins.model.common.definition;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fit2cloud.devops.service.jenkins.handler.obj2xml.FlowDefinitionTransformer;
import com.fit2cloud.devops.service.jenkins.model.common.scm.git.ScmGit;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("definition")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE)
public class CpsScmFlowDefinition extends Definition{

    public CpsScmFlowDefinition() {
        super.setClazz("org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition");
        super.setType(FlowDefinitionTransformer.DefinitionTypeHolder.CPS_SCM);
    }

    @XStreamAlias(value = "scm",impl = ScmGit.class)
    private ScmGit scm;

    private String scriptPath;

    private Boolean lightweight;

    public ScmGit getScm() {
        return scm;
    }

    public void setScm(ScmGit scm) {
        this.scm = scm;
    }

    public String getScriptPath() {
        return scriptPath;
    }

    public void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    public Boolean getLightweight() {
        return lightweight;
    }

    public void setLightweight(Boolean lightweight) {
        this.lightweight = lightweight;
    }
}
