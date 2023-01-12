package com.fit2cloud.devops.service.jenkins.model.common.publisher;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("hudson.maven.RedeployPublisher")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE)
public class RedeployPublisher extends BasePublisherModel {

    {
        this.type = PublisherType.REDEPLOY_PUBLISHER.getJavaType();
    }

    private String id;
    private String url;
    private String releaseEnvVar;
    private Boolean uniqueVersion;
    private Boolean evenIfUnstable;

    public String getReleaseEnvVar() {
        return releaseEnvVar;
    }

    public void setReleaseEnvVar(String releaseEnvVar) {
        this.releaseEnvVar = releaseEnvVar;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean isUniqueVersion() {
        return uniqueVersion;
    }

    public void setUniqueVersion(Boolean uniqueVersion) {
        this.uniqueVersion = uniqueVersion;
    }

    public Boolean isEvenIfUnstable() {
        return evenIfUnstable;
    }

    public void setEvenIfUnstable(Boolean evenIfUnstable) {
        this.evenIfUnstable = evenIfUnstable;
    }

}
