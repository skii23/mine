package com.fit2cloud.devops.service.jenkins.model.common.definition;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fit2cloud.devops.service.jenkins.handler.obj2xml.FlowDefinitionTransformer;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("definition")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE)
public class CpsFlowDefinition extends Definition{

    public CpsFlowDefinition() {
        super.setClazz("org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition");
        super.setType(FlowDefinitionTransformer.DefinitionTypeHolder.CPS_NULL);
    }

    private String script;

    private Boolean sandbox;

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public Boolean getSandbox() {
        return sandbox;
    }

    public void setSandbox(Boolean sandbox) {
        this.sandbox = sandbox;
    }
}
