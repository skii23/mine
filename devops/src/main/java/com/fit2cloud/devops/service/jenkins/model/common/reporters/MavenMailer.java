package com.fit2cloud.devops.service.jenkins.model.common.reporters;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fit2cloud.devops.service.jenkins.model.common.publisher.BasePublisherModel;
import com.fit2cloud.devops.service.jenkins.model.common.publisher.PublisherType;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author caiwzh
 * @date 2022/11/25
 */
@XStreamAlias("hudson.maven.reporters.MavenMailer")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE)
@Data
public class MavenMailer extends BasePublisherModel {

    {
        this.type = PublisherType.EMAIL_PUBLISHER.getJavaType();
    }

    private String recipients;

    private Boolean dontNotifyEveryUnstableBuild;

    private Boolean sendToIndividuals;

    private Boolean perModuleEmail;

    public String getRecipients() {
        try {
            return Stream.of(recipients.split(" ")).collect(Collectors.joining(","));
        } catch (Exception e) {
            return recipients;
        }
    }

    public void setRecipients(String recipients) {
        try {
            this.recipients = Stream.of(recipients.split(",")).collect(Collectors.joining(" "));
        } catch (Exception e) {
            this.recipients = recipients;
        }
    }

    public Boolean getDontNotifyEveryUnstableBuild() {
        return !dontNotifyEveryUnstableBuild;
    }

    public void setDontNotifyEveryUnstableBuild(Boolean dontNotifyEveryUnstableBuild) {
        this.dontNotifyEveryUnstableBuild = !dontNotifyEveryUnstableBuild;
    }
}
