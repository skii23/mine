package com.fit2cloud.devops.service.jenkins.handler.obj2xml;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.devops.common.util.XmlUtils;
import com.fit2cloud.devops.service.jenkins.handler.AbstractConvertor;
import com.fit2cloud.devops.service.jenkins.model.XmlNode;
import com.fit2cloud.devops.service.jenkins.model.common.actions.Actions;
import com.fit2cloud.devops.service.jenkins.model.multibranch.*;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class WorkflowMultiBranchTransformer extends AbstractConvertor<JSONObject, Document> {

    private List<String> flowbase = Lists.newArrayList("description", "disabled");


    @Override
    public void doConvert(JSONObject source, Document target) {
        Element rootElement = target.getRootElement();
        rootElement.removeContent();
        //处理flow暂时支持的基础属性节点description、keepDependencies、disabled
        flowbase.forEach(str -> Optional.ofNullable(source.getString(str)).ifPresent(value -> {
            Element child = new Element(str);
            child.setText(value);
            rootElement.addContent(child);
        }));
        Element displayNameEle = new Element("displayName");
        displayNameEle.setText(source.getString("name"));
        rootElement.addContent(displayNameEle);
        //默认actions
        Element actionsElem = XmlUtils.objToXmlElement(new Actions());
        rootElement.addContent(actionsElem);
        //默认属性properties
        rootElement.addContent(WorkflowMultiBranchProject.PROPERTIES_ELE.get());

        //默认属性folderViews
        rootElement.addContent(WorkflowMultiBranchProject.FOLDERVIEWS_ELE.get());

        //默认属性healthMetrics
        rootElement.addContent(WorkflowMultiBranchProject.HEALTHMETRICS_ELE.get());

        //默认属性icon
        rootElement.addContent(WorkflowMultiBranchProject.ICON_ELE.get());

        //默认属性orphanedItemStrategy
        rootElement.addContent(WorkflowMultiBranchProject.ORPHANEDITEMSTRATEGY_ELE.get());

        Factory factory = new Factory();
        if(source.containsKey("scriptPath")){
            factory.setScriptPath(source.getString("scriptPath"));
        }
        rootElement.addContent( XmlUtils.objToElement(factory));

        //处理sources
        JSONArray sources = source.getJSONArray("sources");
        rootElement.removeChild("sources");

        Element branchSourceListEle = XmlUtils.objToElement(new BranchSourceList());
        rootElement.addContent(branchSourceListEle);

        branchSourceListEle.addContent(XmlUtils.objToElement(new Owner()));
        List<Element> branchSourceEle = new ArrayList<>(sources.size());
        if (CollectionUtils.isNotEmpty(sources)) {
            for (int i = 0; i < sources.size(); i++) {
                Element element = XmlUtils.objToElement(new BranchSource());
                element.addContent(BranchSource.STRATEGY_ELE.get());
                JSONObject branchSourceJson = sources.getJSONObject(i);
                String type = branchSourceJson.getString("type");
                Element sourceEle = null;
                if (StringUtils.equalsIgnoreCase(type, TypeHolder.GITLAB)) {
                    GitLabSCMSource gitLabSCMSource = branchSourceJson.toJavaObject(GitLabSCMSource.class);
                    gitLabSCMSource.setId(UUID.randomUUID().toString());
                    sourceEle = XmlUtils.objToElement(gitLabSCMSource);
                    sourceEle.addContent(GitLabSCMSource.GITLAB_TRAITS_ELE.get());
                }
                if (StringUtils.equalsIgnoreCase(type, TypeHolder.GITEA)) {
                    GiteaSCMSource giteaSCMSource = branchSourceJson.toJavaObject(GiteaSCMSource.class);
                    giteaSCMSource.setId(UUID.randomUUID().toString());
                    sourceEle = XmlUtils.objToElement(giteaSCMSource);
                    sourceEle.addContent(GiteaSCMSource.GITEA_TRAITS_ELE.get());
                }
                element.addContent(sourceEle);
                branchSourceEle.add(element);
            }
        }
        Element dataEle = XmlUtils.objToElement(new BranchSourceList.Data());
        dataEle.addContent(branchSourceEle);
        branchSourceListEle.addContent(dataEle);

//        然后处理不支持的节点
        JSONArray unknownNodes = source.getJSONArray("unknownNodes");
        Optional.ofNullable(unknownNodes).ifPresent(nodes -> nodes.forEach(node -> {
            JSONObject nodeObj = (JSONObject) node;
            XmlNode xmlNode = nodeObj.toJavaObject(XmlNode.class);
            if (rootElement.getChild(xmlNode.getXmlNodeName()) == null) {
                Element element = XmlUtils.stringToXmlElement(xmlNode.getXmlNodeData());
                rootElement.addContent(element);
            }
        }));
    }

    public static final class TypeHolder {
        public static final String GITLAB = "gitlab";
        public static final String GITEA = "gitea";
    }
}
