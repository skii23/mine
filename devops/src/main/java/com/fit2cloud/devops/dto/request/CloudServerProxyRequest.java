package com.fit2cloud.devops.dto.request;

import java.util.List;

public class CloudServerProxyRequest {
    public List<String> getCloudServerIds() {
        return cloudServerIds;
    }

    public void setCloudServerIds(List<String> cloudServerIds) {
        this.cloudServerIds = cloudServerIds;
    }

    private List<String> cloudServerIds;

    public String getProxyId() {
        return proxyId;
    }

    public void setProxyId(String proxyId) {
        this.proxyId = proxyId;
    }

    private String proxyId;
    private String sort;

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

}
