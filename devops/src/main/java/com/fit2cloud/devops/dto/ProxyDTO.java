package com.fit2cloud.devops.dto;

import com.fit2cloud.devops.base.domain.Proxy;

public class ProxyDTO extends Proxy {
    private String organizationName;

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }
}
