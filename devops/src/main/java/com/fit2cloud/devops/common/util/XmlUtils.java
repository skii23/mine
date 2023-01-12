package com.fit2cloud.devops.common.util;

import com.fit2cloud.devops.service.jenkins.model.common.GlobalSettings;
import com.fit2cloud.devops.service.jenkins.model.common.Settings;
import com.fit2cloud.devops.service.jenkins.model.common.builder.Shell;
import com.fit2cloud.devops.service.jenkins.model.common.properties.discarder.strategy.LogRotatorStrategy;
import com.fit2cloud.devops.service.jenkins.model.common.properties.parameters.parameter.*;
import com.fit2cloud.devops.service.jenkins.model.common.publisher.F2CPublisher;
import com.fit2cloud.devops.service.jenkins.model.common.scm.ScmNull;
import com.fit2cloud.devops.service.jenkins.model.common.scm.git.ScmGit;
import com.fit2cloud.devops.service.jenkins.model.common.scm.git.SubmoduleCfg;
import com.fit2cloud.devops.service.jenkins.model.common.scm.svn.ScmSvn;
import com.fit2cloud.devops.service.jenkins.model.common.scm.svn.WorkspaceUpdater;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;
import com.thoughtworks.xstream.io.xml.Xpp3DomDriver;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import javax.xml.XMLConstants;
import java.io.IOException;
import java.io.StringReader;

/**
 * @author shaochuan.wu
 */
public class XmlUtils {

    private XmlUtils() {

    }

    private static XMLOutputter defaultXmlOutputter() {
        return new XMLOutputter(Format.getPrettyFormat());
    }

    public static XStream defaultXstream() {
//      解决两个下划线转义问题
        XStream xStream = new XStream(new Xpp3DomDriver(new XmlFriendlyNameCoder("-_", "_")));
        XStream.setupDefaultSecurity(xStream);
        xStream.allowTypesByRegExp(new String[]{"com.fit2cloud.devops.service.jenkins.model.*"});
//      去除class属性 会导致其他场景下的兼容问题 需要引入不包含此配置的构造对象
        xStream.aliasSystemAttribute(null, "class");
//      去除子类中的defined-in属性
        xStream.aliasSystemAttribute(null, "defined-in");
//      自动解析字段注解
        xStream.autodetectAnnotations(true);
//      忽略未定义字段
        xStream.ignoreUnknownElements();

//        /*
//         * 给指定的类型指定别名
//         */
//        xStream.alias("hudson.model.StringParameterDefinition", StringParameterDefinition.class);
//        xStream.alias("hudson.model.PasswordParameterDefinition", PasswordParameterDefinition.class);
//        xStream.alias("hudson.model.BooleanParameterDefinition", BooleanParameterDefinition.class);
//        xStream.alias("hudson.model.FileParameterDefinition", FileParameterDefinition.class);
//        xStream.alias("hudson.model.TextParameterDefinition", TextParameterDefinition.class);
//        xStream.alias("hudson.model.RunParameterDefinition", RunParameterDefinition.class);
//        xStream.alias("hudson.model.ChoiceParameterDefinition", ChoiceParameterDefinition.class);

//        xStream.aliasAttribute("class","classStr");
        //也许类似的方式可以考虑用 xStream.addImplicitCollection();解决

        return xStream;
    }

    public static XStream normalXstream() {
//      解决两个下划线转义问题
        XStream xstream = new XStream(new Xpp3DomDriver(new XmlFriendlyNameCoder("-_", "_")));
        XStream.setupDefaultSecurity(xstream);
        // 开启安全性设置后， 需要显式指定许可的类型
        xstream.allowTypesByRegExp(new String[]{"com.fit2cloud.devops.service.jenkins.model.*"});
//        xstream.allowTypesByWildcard(new String[] {
//                "com.mydomain.mynewapp.**",
//                "com.mydomain.utilitylibraries.**
//        });

//      去除子类中的defined-in属性
//      xStream.aliasSystemAttribute(null, "defined-in");
//      自动解析字段注解
        xstream.autodetectAnnotations(true);
//      忽略未定义字段
        xstream.ignoreUnknownElements();


        /*
         * 给指定的类型指定别名
         */


        xstream.alias("hudson.tasks.LogRotator", LogRotatorStrategy.class);
        xstream.alias("hudson.tasks.Shell", Shell.class);


        xstream.alias("hudson.model.StringParameterDefinition", StringParameterDefinition.class);
        xstream.alias("hudson.model.PasswordParameterDefinition", PasswordParameterDefinition.class);
        xstream.alias("hudson.model.BooleanParameterDefinition", BooleanParameterDefinition.class);
        xstream.alias("hudson.model.FileParameterDefinition", FileParameterDefinition.class);
        xstream.alias("hudson.model.TextParameterDefinition", TextParameterDefinition.class);
        xstream.alias("hudson.model.RunParameterDefinition", RunParameterDefinition.class);
        xstream.alias("hudson.model.ChoiceParameterDefinition", ChoiceParameterDefinition.class);



        //class定义 去掉会出现 com.thoughtworks.xstream.mapper.CannotResolveClassException
        xstream.alias("hudson.scm.NullSCM", ScmNull.class);

        xstream.alias("hudson.plugins.git.GitSCM", ScmGit.class);
        xstream.alias("empty-list", SubmoduleCfg.class);
        xstream.alias("list", SubmoduleCfg.class);

        xstream.alias("hudson.scm.SubversionSCM", ScmSvn.class);

        xstream.alias("hudson.scm.subversion.UpdateUpdater", WorkspaceUpdater.class);
        xstream.alias("hudson.scm.subversion.CheckoutUpdater", WorkspaceUpdater.class);
        xstream.alias("hudson.scm.subversion.NoopUpdater", WorkspaceUpdater.class);
        xstream.alias("hudson.scm.subversion.UpdateWithCleanUpdater", WorkspaceUpdater.class);
        xstream.alias("hudson.scm.subversion.UpdateWithRevertUpdater", WorkspaceUpdater.class);

//        xstream.alias("hudson.scm.SubversionSCM", ModuleLocation.class);

        xstream.alias("jenkins.mvn.DefaultSettingsProvider", Settings.class);
        xstream.alias("jenkins.mvn.FilePathSettingsProvider", Settings.class);
        xstream.alias("org.jenkinsci.plugins.configfiles.maven.job.MvnSettingsProvider", Settings.class);
        xstream.alias("jenkins.mvn.DefaultGlobalSettingsProvider", GlobalSettings.class);
        xstream.alias("jenkins.mvn.FilePathGlobalSettingsProvider", GlobalSettings.class);
        xstream.alias("org.jenkinsci.plugins.configfiles.maven.job.MvnGlobalSettingsProvider", GlobalSettings.class);

        xstream.alias("com.fit2cloud.codedeploy.F2CCodeDeployPublisher", F2CPublisher.class);
        xstream.alias("com.fit2cloud.codedeploy2.F2CCodeDeploySouthPublisher", F2CPublisher.class);




//        com.fit2cloud.devops.service.jenkins.model.common.scm.git.SubmoduleCfg


        return xstream;
    }

    // 常规转换 不包含特定class字段被占用的场景
    public static Element objToElement(Object obj) {
        XStream xStream = normalXstream();
        xStream.processAnnotations(obj.getClass());
        String objXml = xStream.toXML(obj);
        return stringToXmlElement(objXml);
    }


    public static String toXml(Object o) {
        XStream xStream = defaultXstream();
        xStream.processAnnotations(o.getClass());
        return xStream.toXML(o);
    }


    @SuppressWarnings("unchecked")
    public static <T> T fromXml(String xml, Class<T> clazz) {
        XStream xStream = normalXstream();
        // 因为springboot项目中不是使用的默认classloader类加载器。 通过手动重设xtream的classloader 设置类加载器
        // 否则会报同类型的转换错误 com.thoughtworks.xstream.converters.ConversionException
        xStream.setClassLoader(clazz.getClassLoader());
        xStream.processAnnotations(clazz);
        try {
            return (T) xStream.fromXML(xml, clazz.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T fromXmlWithoutAttr(String xml, Class<T> clazz) {
        XStream xStream = normalXstream();
        // 因为springboot项目中不是使用的默认classloader类加载器。 通过手动重设xtream的classloader 设置类加载器
        // 否则会报同类型的转换错误 com.thoughtworks.xstream.converters.ConversionException
        xStream.setClassLoader(clazz.getClassLoader());
        xStream.processAnnotations(clazz);
        //去除class属性 会导致其他场景下的兼容问题 需要引入不包含此配置的构造对象
        xStream.aliasSystemAttribute(null, "class");
        xStream.aliasSystemAttribute(null, "reference");
        try {
            return (T) xStream.fromXML(xml, clazz.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    // 带接口类型需要指定默认实现时
    public static <T, K> T fromXml(String xml, Class<T> clazz, Class<K> parent) {
        XStream xStream = normalXstream();
        xStream.addDefaultImplementation(clazz, parent);
        xStream.processAnnotations(clazz);
        return (T) xStream.fromXML(xml);
    }

    public static String outputXml(Element element) {
        return defaultXmlOutputter().outputString(element);
    }

    public static Element objToXmlElement(Object obj) {
        String objXml = toXml(obj);
        return stringToXmlElement(objXml);
    }

    public static Element stringToXmlElement(String xml) {
        SAXBuilder saxBuilder = getSaxBuilder();
        Document document;
        try {
            document = saxBuilder.build(new StringReader(xml));
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
            return null;
        }
        return document.getRootElement().detach();
    }

    public static SAXBuilder getSaxBuilder() {
        SAXBuilder saxBuilder = new SAXBuilder();
        saxBuilder.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        saxBuilder.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return saxBuilder;
    }

}
