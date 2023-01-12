package com.fit2cloud.devops.vo;

/**
 * @author wisonic
 */
public class FileNodeVO {

    private String path;
    private String type;
    private String name;

    public FileNodeVO() {
    }

    public FileNodeVO(String name,String type, String path) {
        this.name = name;
        this.type = type;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
