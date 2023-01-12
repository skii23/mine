package com.fit2cloud.devops.common.model;

public enum DeploymentLevel {
    ALL("all"), INCREMENT("increment");

    private String value;

    DeploymentLevel(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
