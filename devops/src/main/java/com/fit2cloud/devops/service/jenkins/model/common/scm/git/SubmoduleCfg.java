package com.fit2cloud.devops.service.jenkins.model.common.scm.git;

import com.fit2cloud.devops.service.jenkins.model.AbstractBaseModel;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * @author wisonic
 */
@XStreamAlias(value="submoduleCfg")
public class SubmoduleCfg {

//    public SubmoduleCfg() {
//        this.classStr = "empty-list";
//    }

    @XStreamAlias("class")
    @XStreamAsAttribute
    private String classStr = "empty-list";

    public String getClassStr() {
        return classStr;
    }

    public void setClassStr(String classStr) {
        this.classStr = classStr;
    }
}
