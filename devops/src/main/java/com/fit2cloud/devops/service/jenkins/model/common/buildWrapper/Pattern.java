package com.fit2cloud.devops.service.jenkins.model.common.buildWrapper;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("hudson.plugins.ws__cleanup.Pattern")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE)
public class Pattern {
    private String pattern;
    //INCLUDE、EXCLUDE
    private String type;

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
