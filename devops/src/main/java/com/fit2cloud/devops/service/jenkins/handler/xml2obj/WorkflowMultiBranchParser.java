package com.fit2cloud.devops.service.jenkins.handler.xml2obj;

import com.fit2cloud.devops.common.util.XmlUtils;
import com.fit2cloud.devops.service.jenkins.handler.AbstractConvertor;
import com.fit2cloud.devops.service.jenkins.model.common.actions.Actions;
import com.fit2cloud.devops.service.jenkins.model.multibranch.*;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.List;

public class WorkflowMultiBranchParser extends AbstractConvertor<Document, WorkflowMultiBranchProject> {


    @Override
    public void doConvert(Document source, WorkflowMultiBranchProject target) {
        Element rootElement = source.getRootElement();
        //设置基础属性
        Element description = rootElement.getChild("description");
        if (description != null) {
            target.setDescription(description.getTextTrim());
        }
        Element displayName = rootElement.getChild("displayName");
        if (displayName != null) {
            target.setDisplayName(displayName.getTextTrim());
        }
        Element disabled = rootElement.getChild("disabled");
        if (disabled != null) {
            target.setDisabled(Boolean.valueOf(disabled.getTextTrim()));
        }
        //默认actions
        Actions actions = new Actions();
        target.setActions(actions);

        //设置sources
        BranchSourceList branchSourceList = new BranchSourceList();
        branchSourceList.setOwner(new Owner());
        Element sourcesElem = rootElement.getChild("sources");
        if (sourcesElem != null) {
            Element dataEle = sourcesElem.getChild("data");
            if (dataEle != null) {
                BranchSourceList.Data data = new BranchSourceList.Data();
                branchSourceList.setData(data);
                List<Element> children = dataEle.getChildren("jenkins.branch.BranchSource");
                //jenkins.branch.BranchSource
                List<BranchSource> branchSources = new ArrayList<>();
                data.setBranchSource(branchSources);
                children.forEach(e -> {
                    BranchSource branchSource = new BranchSource();
                    Element sourceEle = e.getChild("source");
                    String classStr = sourceEle.getAttributeValue("class");
                    String scmXml = XmlUtils.outputXml(sourceEle);
                    if (StringUtils.equals(classStr, TypeHolder.GITLAB)) {
                        GitLabSCMSource gitLabSCMSource = XmlUtils.fromXmlWithoutAttr(scmXml, GitLabSCMSource.class);
                        branchSource.setSource(gitLabSCMSource);
                    }
                    if (StringUtils.equals(classStr, TypeHolder.GITEA)) {
                        GiteaSCMSource giteaSCMSource = XmlUtils.fromXmlWithoutAttr(scmXml, GiteaSCMSource.class);
                        branchSource.setSource(giteaSCMSource);
                    }
                    branchSources.add(branchSource);
                });
            }
        }
        target.setSources(branchSourceList);
        Element factoryEle = rootElement.getChild("factory");
        if(factoryEle != null){
            target.setFactory(XmlUtils.fromXmlWithoutAttr(XmlUtils.outputXml(factoryEle), Factory.class));
        }
    }

    public static final class TypeHolder {
        public static final String GITLAB = "io.jenkins.plugins.gitlabbranchsource.GitLabSCMSource";
        public static final String GITEA = "org.jenkinsci.plugin.gitea.GiteaSCMSource";
    }
}
