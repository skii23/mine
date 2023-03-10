package com.fit2cloud.devops.base.domain;

import java.io.Serializable;

public class DevopsJenkinsCredentialWithBLOBs extends DevopsJenkinsCredential implements Serializable {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column devops_jenkins_credential.organization
     *
     * @mbg.generated
     */
    private String organization;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column devops_jenkins_credential.workspace
     *
     * @mbg.generated
     */
    private String workspace;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column devops_jenkins_credential.private_key
     *
     * @mbg.generated
     */
    private String privateKey;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table devops_jenkins_credential
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column devops_jenkins_credential.organization
     *
     * @return the value of devops_jenkins_credential.organization
     *
     * @mbg.generated
     */
    public String getOrganization() {
        return organization;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column devops_jenkins_credential.organization
     *
     * @param organization the value for devops_jenkins_credential.organization
     *
     * @mbg.generated
     */
    public void setOrganization(String organization) {
        this.organization = organization == null ? null : organization.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column devops_jenkins_credential.workspace
     *
     * @return the value of devops_jenkins_credential.workspace
     *
     * @mbg.generated
     */
    public String getWorkspace() {
        return workspace;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column devops_jenkins_credential.workspace
     *
     * @param workspace the value for devops_jenkins_credential.workspace
     *
     * @mbg.generated
     */
    public void setWorkspace(String workspace) {
        this.workspace = workspace == null ? null : workspace.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column devops_jenkins_credential.private_key
     *
     * @return the value of devops_jenkins_credential.private_key
     *
     * @mbg.generated
     */
    public String getPrivateKey() {
        return privateKey;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column devops_jenkins_credential.private_key
     *
     * @param privateKey the value for devops_jenkins_credential.private_key
     *
     * @mbg.generated
     */
    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey == null ? null : privateKey.trim();
    }
}