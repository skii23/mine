package com.fit2cloud.devops.service.jenkins.handler.obj2xml;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.devops.common.consts.JenkinsConstants;
import com.fit2cloud.devops.common.util.XmlUtils;
import com.fit2cloud.devops.service.jenkins.handler.AbstractConvertor;
import com.fit2cloud.devops.service.jenkins.model.common.reporters.MavenMailer;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.Optional;

public class MavenPropertiesTransformer extends AbstractConvertor<JSONObject, Document> {

    public MavenPropertiesTransformer() {
        super("mavenPropertiesTransformer");
    }

    @Override
    public void doConvert(JSONObject source, Document target) {
        Element rootElement = target.getRootElement();
//        设置基础属性
        JenkinsConstants.FieldMapHolder.MAVEN_JOB_MODEL_FIELDS_MAP.forEach((name, clazz) ->
                Optional.ofNullable(source.getObject(name,clazz)).ifPresent(obj -> {
                    rootElement.removeChild(name);
                    if (JenkinsConstants.BASE_TYPE_SET.contains(clazz)) {
                        Element child = new Element(name);
                        child.setText(obj.toString());
                        rootElement.addContent(child);
                    } else {
                        Element child = XmlUtils.objToXmlElement(obj);
                        child.setName(name);
                        rootElement.addContent(child);
                    }
                }));
        //maven 邮件通知处理
        JSONArray publishers = source.getJSONArray("publishers");
        if(CollectionUtils.isNotEmpty(publishers)){
            for (int i = 0; i < publishers.size(); i++) {
                JSONObject pubData = publishers.getJSONObject(i);
                if(StringUtils.equalsIgnoreCase("EMAIL_PUBLISHER",pubData.getString("type"))){
                    rootElement.removeChild("reporters");
                    Element reporters = new Element("reporters");
                    rootElement.addContent(reporters);
                    MavenMailer mavenMailer = new MavenMailer();
                    mavenMailer.setRecipients(pubData.getString("recipients"));
                    mavenMailer.setDontNotifyEveryUnstableBuild(pubData.getBooleanValue("dontNotifyEveryUnstableBuild"));
                    mavenMailer.setSendToIndividuals(pubData.getBooleanValue("sendToIndividuals"));
                    mavenMailer.setPerModuleEmail(pubData.getBooleanValue("perModuleEmail"));
                    reporters.addContent(XmlUtils.objToElement(mavenMailer));
                }
            }
        }
    }

}
