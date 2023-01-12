package com.fit2cloud.devops.service.jenkins.handler.xml2obj;

import com.fit2cloud.devops.common.util.XmlUtils;
import com.fit2cloud.devops.service.jenkins.handler.AbstractConvertor;
import com.fit2cloud.devops.service.jenkins.model.common.BaseJobModel;
import com.fit2cloud.devops.service.jenkins.model.common.publisher.*;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PublisherParser extends AbstractConvertor<Document,BaseJobModel> {

    private static final Map<String, Class<?>> TYPE_MAP = Stream.of(PublisherType.values()).collect(Collectors.toMap(PublisherType::getXmlTag,PublisherType::getClazz,(k1,k2) ->k1));

    public PublisherParser() {
        super("publisherParser","publishers");
    }

    public PublisherParser(String name) {
        super(name);
    }

    public PublisherParser(String name, String sourceName) {
        super(name, sourceName);
    }

    public PublisherParser(String name, String sourceName,String targetName) {
        super(name, sourceName,targetName);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doConvert(Document source, BaseJobModel target) {
        Element rootElement = source.getRootElement();
        this.convertMap.forEach((xmlNodeName,fieldName) -> {
            Element publishersElem = rootElement.getChild(xmlNodeName);
            Optional.ofNullable(publishersElem).ifPresent(publishers -> {
                Field field = FieldUtils.getField(target.getClass(), fieldName, true);
                try {
                    Object publishersListObj = field.get(target);
                    if (publishersListObj == null) {
                        FieldUtils.writeField(field,target,new ArrayList<BasePublisherModel>(),true);
                    }
                    final List<BasePublisherModel> publishersList = ((List<BasePublisherModel>) field.get(target));
                    publishers.getChildren().forEach(publisher -> {
                        Class<?> clazz = TYPE_MAP.get(publisher.getName());
                        String publisherXml = XmlUtils.outputXml(publisher);
                        if (clazz != null) {
                            Object o = XmlUtils.fromXml(publisherXml, clazz);
                            publishersList.add((BasePublisherModel) o);
                        } else {
                            BasePublisherModel publisherNode = new BasePublisherModel();
                            publisherNode.setXmlNodeName(publisher.getName());
                            publisherNode.setXmlNodeData(publisherXml);
                            publishersList.add(publisherNode);
                        }
                    });
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
        });
    }
}
