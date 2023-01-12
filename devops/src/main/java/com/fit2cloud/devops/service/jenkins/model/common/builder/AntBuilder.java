package com.fit2cloud.devops.service.jenkins.model.common.builder;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fit2cloud.devops.service.jenkins.handler.obj2xml.BuilderTransformer;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import lombok.Data;

/**
 * @author caiwzh
 * @date 2022/10/10
 */
@XStreamAlias("hudson.tasks.Ant")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE)
@Data
public class AntBuilder extends BaseBuilderModel{

    {
        this.type = BuilderTransformer.BuilderTypeHolder.ANT;
    }

    @XStreamAsAttribute
    private String plugin = "ant@475.vf34069fef73c";

    private String targets;

    private String antName;

    private String antOpts;

    private String buildFile;

    private String properties;

}
