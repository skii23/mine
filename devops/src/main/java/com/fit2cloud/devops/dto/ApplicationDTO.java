package com.fit2cloud.devops.dto;


import com.fit2cloud.devops.base.domain.Application;
import io.swagger.annotations.ApiModel;

import java.util.List;

@ApiModel
public class ApplicationDTO extends Application {
    private String businessValueId;
    private String organizationName;
    private String workspaceName;
    private Integer versionCount;
    private Integer deployCount;
    private String systemTagValueAlias;
    private List<ApplicationRepositorySettingDTO> applicationRepositorySettings;
    private String prodName;
    private String planName;

    public String getProdName() {
        return prodName;
    }

    public void setProdName(String prodName) {
        this.prodName = prodName;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public String getSystemTagValueAlias() {
        return systemTagValueAlias;
    }

    public void setSystemTagValueAlias(String systemTagValueAlias) {
        this.systemTagValueAlias = systemTagValueAlias;
    }

    public List<ApplicationRepositorySettingDTO> getApplicationRepositorySettings() {
        return applicationRepositorySettings;
    }

    public void setApplicationRepositorySettings(List<ApplicationRepositorySettingDTO> applicationRepositorySettings) {
        this.applicationRepositorySettings = applicationRepositorySettings;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }


    public String getBusinessValueId() {
        return businessValueId;
    }

    public void setBusinessValueId(String businessValueId) {
        this.businessValueId = businessValueId;
    }

    public Integer getDeployCount() {
        return deployCount;
    }

    public void setDeployCount(Integer deployCount) {
        this.deployCount = deployCount;
    }

    public Integer getVersionCount() {
        return versionCount;
    }

    public void setVersionCount(Integer versionCount) {
        this.versionCount = versionCount;
    }
}
