package com.fit2cloud.devops.service.jenkins.model.multibranch;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fit2cloud.devops.common.util.XmlUtils;
import com.fit2cloud.devops.service.jenkins.model.AbstractBaseModel;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author caiwzh
 * @date 2022/8/19
 */
@XStreamAlias("jenkins.branch.BranchSource")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE)
public class BranchSource {

    public static final String DEFAULT_STRATEGY = "<strategy class=\"jenkins.branch.DefaultBranchPropertyStrategy\">\n" +
            "          <properties class=\"empty-list\"/>\n" +
            "        </strategy>";

    public static final WorkflowMultiBranchProject.EleFunction STRATEGY_ELE =() -> XmlUtils.stringToXmlElement(DEFAULT_STRATEGY);

    private AbstractBaseModel source;

    public AbstractBaseModel getSource() {
        return source;
    }

    public void setSource(AbstractBaseModel source) {
        this.source = source;
    }
}
