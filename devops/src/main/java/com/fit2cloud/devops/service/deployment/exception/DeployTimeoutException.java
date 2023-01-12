package com.fit2cloud.devops.service.deployment.exception;

public class DeployTimeoutException extends RuntimeException {
    public DeployTimeoutException() {
        super("deploy time out");
    }
}
