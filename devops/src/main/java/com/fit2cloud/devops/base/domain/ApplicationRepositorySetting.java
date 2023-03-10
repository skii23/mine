package com.fit2cloud.devops.base.domain;

import java.io.Serializable;

public class ApplicationRepositorySetting implements Serializable {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column devops_application_repository_setting.id
     *
     * @mbg.generated
     */
    private String id;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column devops_application_repository_setting.application_id
     *
     * @mbg.generated
     */
    private String applicationId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column devops_application_repository_setting.repository_id
     *
     * @mbg.generated
     */
    private String repositoryId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column devops_application_repository_setting.env_id
     *
     * @mbg.generated
     */
    private String envId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table devops_application_repository_setting
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column devops_application_repository_setting.id
     *
     * @return the value of devops_application_repository_setting.id
     *
     * @mbg.generated
     */
    public String getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column devops_application_repository_setting.id
     *
     * @param id the value for devops_application_repository_setting.id
     *
     * @mbg.generated
     */
    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column devops_application_repository_setting.application_id
     *
     * @return the value of devops_application_repository_setting.application_id
     *
     * @mbg.generated
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column devops_application_repository_setting.application_id
     *
     * @param applicationId the value for devops_application_repository_setting.application_id
     *
     * @mbg.generated
     */
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId == null ? null : applicationId.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column devops_application_repository_setting.repository_id
     *
     * @return the value of devops_application_repository_setting.repository_id
     *
     * @mbg.generated
     */
    public String getRepositoryId() {
        return repositoryId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column devops_application_repository_setting.repository_id
     *
     * @param repositoryId the value for devops_application_repository_setting.repository_id
     *
     * @mbg.generated
     */
    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId == null ? null : repositoryId.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column devops_application_repository_setting.env_id
     *
     * @return the value of devops_application_repository_setting.env_id
     *
     * @mbg.generated
     */
    public String getEnvId() {
        return envId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column devops_application_repository_setting.env_id
     *
     * @param envId the value for devops_application_repository_setting.env_id
     *
     * @mbg.generated
     */
    public void setEnvId(String envId) {
        this.envId = envId == null ? null : envId.trim();
    }
}