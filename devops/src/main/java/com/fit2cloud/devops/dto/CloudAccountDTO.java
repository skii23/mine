package com.fit2cloud.devops.dto;

import com.fit2cloud.commons.server.base.domain.CloudAccount;

public class CloudAccountDTO extends CloudAccount {

    private String icon;

    private String pluginDesc;

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getPluginDesc() {
        return pluginDesc;
    }

    public void setPluginDesc(String pluginDesc) {
        this.pluginDesc = pluginDesc;
    }
}
