package com.fit2cloud.devops.service.jenkins.model.common.trigger;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fit2cloud.devops.service.jenkins.handler.obj2xml.TriggerTransformer;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("hudson.triggers.TimerTrigger")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE)
public class TimerTrigger extends BaseTriggerModel {

    {
        this.type = TriggerTransformer.TriggerTypeHolder.TIMER_TRIGGER;
    }

    private String spec;

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

}
