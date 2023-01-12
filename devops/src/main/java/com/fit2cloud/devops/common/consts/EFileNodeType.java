package com.fit2cloud.devops.common.consts;

/**
 * @author wisonic
 */

public enum EFileNodeType {
    DIRECTORY("directory"), FILE("file");
    private String type;

    EFileNodeType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}
