package com.fit2cloud.devops.dto;

import com.fit2cloud.devops.base.domain.Application;
import com.fit2cloud.devops.base.domain.ApplicationRepositorySetting;

import java.util.List;

public class ApplicationWithSetting {

    private Application application;
    private List<ApplicationRepositorySetting> applicationRepositorySettings;


    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public List<ApplicationRepositorySetting> getApplicationRepositorySettings() {
        return applicationRepositorySettings;
    }

    public void setApplicationRepositorySettings(List<ApplicationRepositorySetting> applicationRepositorySettings) {
        this.applicationRepositorySettings = applicationRepositorySettings;
    }
}
