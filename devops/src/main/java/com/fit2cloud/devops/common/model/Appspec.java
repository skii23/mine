package com.fit2cloud.devops.common.model;

import java.util.List;
import java.util.Map;

public class Appspec {
    private String version;
    private String os;
    private List<Map<String, String>> files;
    private Map<String, List<Map<String, Object>>> hooks;
    private List<Map<String, String>> permissions;
    private Map<String, List<Map<String, Object>>> app;

    public Map<String, List<Map<String, Object>>> getApp() {
        return app;
    }

    public void setApp(Map<String, List<Map<String, Object>>> app) {
        this.app = app;
    }

    public List<Map<String, String>> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Map<String, String>> permissions) {
        this.permissions = permissions;
    }

    public Map<String, List<Map<String, Object>>> getHooks() {
        return hooks;
    }

    public void setHooks(Map<String, List<Map<String, Object>>> hooks) {
        this.hooks = hooks;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public List<Map<String, String>> getFiles() {
        return files;
    }

    public void setFiles(List<Map<String, String>> files) {
        this.files = files;
    }
}
