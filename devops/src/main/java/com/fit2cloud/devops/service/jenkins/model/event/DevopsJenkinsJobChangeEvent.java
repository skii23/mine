package com.fit2cloud.devops.service.jenkins.model.event;

import com.fit2cloud.devops.base.domain.DevopsJenkinsJob;
import org.springframework.context.ApplicationEvent;

/**
 * @author caiwzh
 * @date 2022/8/25
 */
public class DevopsJenkinsJobChangeEvent extends ApplicationEvent {

    private DevopsJenkinsJob devopsJenkinsJob;

    private OperationType type;
    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public DevopsJenkinsJobChangeEvent(DevopsJenkinsJob source,OperationType type) {
        super(source);
        this.devopsJenkinsJob = source;
        this.type = type;
    }

    public DevopsJenkinsJob getDevopsJenkinsJob() {
        return devopsJenkinsJob;
    }

    public OperationType getType() {
        return type;
    }

    public void setType(OperationType type) {
        this.type = type;
    }
}
