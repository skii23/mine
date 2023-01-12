package com.fit2cloud.devops.service.jenkins.handler.obj2xml;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.devops.common.util.XmlUtils;
import com.fit2cloud.devops.service.jenkins.handler.AbstractConvertor;
import com.fit2cloud.devops.service.jenkins.model.common.publisher.PublisherType;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PublisherTransformer extends AbstractConvertor<JSONObject, Document> {

    private static final Map<String, Class<?>> TYPE_MAP = Stream.of(PublisherType.values()).collect(Collectors.toMap(PublisherType::getJavaType, PublisherType::getClazz, (k1, k2) -> k1));

    public PublisherTransformer() {
        super("publisherTransformer","publishers");
    }

    public PublisherTransformer(String name) {
        super(name);
    }

    public PublisherTransformer(String name, String sourceName) {
        super(name, sourceName);
    }

    public PublisherTransformer(String name, String sourceName,String targetName) {
        super(name, sourceName,targetName);
    }

    @Override
    public void doConvert(JSONObject source, Document target) {
        Element rootElement = target.getRootElement();
        this.convertMap.forEach((fieldName, xmlNodeName) -> {
            rootElement.removeChild(xmlNodeName);
            Element publishersElem = new Element(xmlNodeName);
            JSONArray publishersArray = source.getJSONArray(fieldName);
            Optional.ofNullable(publishersArray).ifPresent(publishers -> publishers.forEach(publisher -> {
                JSONObject publisherObj;
                if (publisher instanceof Map) {
                    publisherObj = new JSONObject(((Map) publisher));
                }else {
                    publisherObj = (JSONObject) publisher;
                }
                String type = publisherObj.getString("type");
                //maven类型的EMAIL_PUBLISHER已处理，跳过
                if(StringUtils.equalsIgnoreCase("MAVEN",source.getString("type")) && StringUtils.equalsIgnoreCase("EMAIL_PUBLISHER",publisherObj.getString("type"))){
                    return;
                }
                Class<?> clazz = TYPE_MAP.get(type);
                if (clazz != null) {
                    Object o = publisherObj.toJavaObject(clazz);
                    Element publisherElem = XmlUtils.objToXmlElement(o);
                    publishersElem.addContent(publisherElem);
                } else {
                    String xmlNodeData = publisherObj.getString("xmlNodeData");
                    if (StringUtils.isNotBlank(xmlNodeData)) {
                        Element publisherElem = XmlUtils.stringToXmlElement(xmlNodeData);
                        publishersElem.addContent(publisherElem);
                    }
                }
            }));
            rootElement.addContent(publishersElem);
        });
    }
}
