package com.fit2cloud.devops.service.jenkins.model.common;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang3.StringUtils;

@XStreamAlias("threshold")
public class Threshold {

    private String name;
    private Integer ordinal;
    private String color;
    private Boolean completeBuild =true;

    public Threshold() {

    }

    public Threshold(String type) {
        if (StringUtils.equalsIgnoreCase(type, "SUCCESS")) {
            this.name = "SUCCESS";
            this.ordinal = 0;
            this.color = "BLUE";
        } else if (StringUtils.equalsIgnoreCase(type,"UNSTABLE")) {
            this.name = "UNSTABLE";
            this.ordinal = 1;
            this.color = "YELLOW";
        } else if (StringUtils.equalsIgnoreCase(type, "FAILURE")) {
            this.name = "FAILURE";
            this.ordinal = 2;
            this.color = "RED";
        }

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Boolean isCompleteBuild() {
        return completeBuild;
    }

    public void setCompleteBuild(Boolean completeBuild) {
        this.completeBuild = completeBuild;
    }
}
