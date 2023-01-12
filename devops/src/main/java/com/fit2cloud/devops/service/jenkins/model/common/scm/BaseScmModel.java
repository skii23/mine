package com.fit2cloud.devops.service.jenkins.model.common.scm;

import com.fit2cloud.devops.service.jenkins.model.AbstractBaseModel;
import com.fit2cloud.devops.service.jenkins.model.common.scm.git.ScmGit;
import com.fit2cloud.devops.service.jenkins.model.common.scm.svn.ScmSvn;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamInclude;

@XStreamAlias(value = "scm")
@XStreamInclude({ScmNull.class, ScmGit.class, ScmSvn.class})
public class BaseScmModel extends AbstractBaseModel {

    @XStreamAlias("class")
    @XStreamAsAttribute
    protected String classStr;

    public String getClassStr() {
        return classStr;
    }

    public void setClassStr(String classStr) {
        this.classStr = classStr;
    }

}
