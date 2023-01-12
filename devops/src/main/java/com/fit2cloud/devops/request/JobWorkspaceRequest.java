package com.fit2cloud.devops.request;

import com.fit2cloud.devops.vo.FileNodeVO;

public class JobWorkspaceRequest {
    private String jobName;
    private FileNodeVO fileNode;

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public FileNodeVO getFileNode() {
        return fileNode;
    }

    public void setFileNode(FileNodeVO fileNode) {
        this.fileNode = fileNode;
    }
}
