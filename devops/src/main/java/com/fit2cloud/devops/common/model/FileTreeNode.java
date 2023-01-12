package com.fit2cloud.devops.common.model;

import java.util.ArrayList;
import java.util.List;

public class FileTreeNode {

    private String name;
    private Object obj;
    private boolean isFolder = false;
    private boolean hasChildren;
    private List<FileTreeNode> children;

    public FileTreeNode() {
    }

    public FileTreeNode(String name) {
        this.name = name;
    }

    public boolean isHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public void setFolder(boolean folder) {
        isFolder = folder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public List<FileTreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<FileTreeNode> children) {
        this.children = children;
    }

    public void addChild(FileTreeNode treeNode) {
        if (children == null){
            children = new ArrayList<>();
        }
        children.add(treeNode);
    }
}
