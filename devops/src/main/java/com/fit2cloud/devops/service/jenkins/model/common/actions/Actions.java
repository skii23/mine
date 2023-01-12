package com.fit2cloud.devops.service.jenkins.model.common.actions;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("actions")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE)
public class Actions {

    @XStreamAlias(value="org.jenkinsci.plugins.pipeline.modeldefinition.actions.DeclarativeJobAction",impl = DeclarativeJobAction.class)
    private DeclarativeJobAction declarativeJobAction;

    @XStreamAlias(value="org.jenkinsci.plugins.pipeline.modeldefinition.actions.DeclarativeJobPropertyTrackerAction",impl = DeclarativeJobPropertyTrackerAction.class)
    private DeclarativeJobPropertyTrackerAction declarativeJobPropertyTrackerAction;

    public DeclarativeJobAction getDeclarativeJobAction() {
        return declarativeJobAction;
    }

    public void setDeclarativeJobAction(DeclarativeJobAction declarativeJobAction) {
        this.declarativeJobAction = declarativeJobAction;
    }

    public DeclarativeJobPropertyTrackerAction getDeclarativeJobPropertyTrackerAction() {
        return declarativeJobPropertyTrackerAction;
    }

    public void setDeclarativeJobPropertyTrackerAction(DeclarativeJobPropertyTrackerAction declarativeJobPropertyTrackerAction) {
        this.declarativeJobPropertyTrackerAction = declarativeJobPropertyTrackerAction;
    }
}
