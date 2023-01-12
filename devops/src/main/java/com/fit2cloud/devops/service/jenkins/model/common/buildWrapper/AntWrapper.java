package com.fit2cloud.devops.service.jenkins.model.common.buildWrapper;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fit2cloud.devops.service.jenkins.handler.obj2xml.BuildWrapperTransformer;
import com.fit2cloud.devops.service.jenkins.handler.obj2xml.PublisherTransformer;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("hudson.tasks.AntWrapper")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE)
public class AntWrapper extends BaseBuildWrapperModel{


    {
        this.type = BuildWrapperTransformer.BuildWrapperTypeHolder.ANT_WRAPPER;
    }

    @XStreamAsAttribute
    private String plugin = "ant@1.11";

    private String installation;

    private String jdk;

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        // 避免被旧插件名称覆盖 强制替换插件名称
        if (!this.plugin.equalsIgnoreCase(plugin)){
            return;
        }
        this.plugin = plugin;
    }


    public String getInstallation() {
        return installation;
    }

    public void setInstallation(String installation) {
        this.installation = installation;
    }

    public String getJdk() {
        return jdk;
    }

    public void setJdk(String jdk) {
        this.jdk = jdk;
    }
}
