package com.fit2cloud.devops.service.jenkins.model.common.publisher;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import lombok.Data;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@XStreamAlias("hudson.tasks.Mailer")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE)
@Data
public class MailerPublisher extends BasePublisherModel {

    {
        this.type = PublisherType.EMAIL_PUBLISHER.getJavaType();
    }

    @XStreamAsAttribute
    private String plugin = "mailer@414.vcc4c33714601";

    private String recipients;

    private Boolean dontNotifyEveryUnstableBuild;

    private Boolean sendToIndividuals;

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
