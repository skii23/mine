package com.fit2cloud.devops.service.jenkins.model.common.scm;

import com.fit2cloud.devops.service.jenkins.handler.obj2xml.BasePropertiesTransformer;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author shaochuan.wu
 * @date 19.12.08
 */
@XStreamAlias("scm")
public class ScmNull extends BaseScmModel {

    public ScmNull()
    {
        this.type = BasePropertiesTransformer.ScmTypeHolder.SCM_NULL;
        this.classStr = "hudson.scm.NullSCM";
    }
}
