package com.fit2cloud.devops.dto;


import com.fit2cloud.devops.base.domain.ApplicationRepository;

public class ApplicationRepositoryDTO extends ApplicationRepository {

    private String organizationName;
    private String node;
    private String locationType;

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }
}
