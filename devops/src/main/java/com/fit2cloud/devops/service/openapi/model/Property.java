package com.fit2cloud.devops.service.openapi.model;

/**
 * @author caiwzh
 * @date 2022/9/19
 */
public class Property {
    private String port;
    private String host;
    private String protocol;

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}
