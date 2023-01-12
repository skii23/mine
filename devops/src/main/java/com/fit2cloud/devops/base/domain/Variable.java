package com.fit2cloud.devops.base.domain;

import java.io.Serializable;

public class Variable implements Serializable {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column devops_variable.id
     *
     * @mbg.generated
     */
    private String id;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column devops_variable.name
     *
     * @mbg.generated
     */
    private String name;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column devops_variable.value
     *
     * @mbg.generated
     */
    private String value;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column devops_variable.created_time
     *
     * @mbg.generated
     */
    private Long createdTime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column devops_variable.resource_type
     *
     * @mbg.generated
     */
    private String resourceType;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column devops_variable.resource_id
     *
     * @mbg.generated
     */
    private String resourceId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table devops_variable
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column devops_variable.id
     *
     * @return the value of devops_variable.id
     *
     * @mbg.generated
     */
    public String getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column devops_variable.id
     *
     * @param id the value for devops_variable.id
     *
     * @mbg.generated
     */
    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column devops_variable.name
     *
     * @return the value of devops_variable.name
     *
     * @mbg.generated
     */
    public String getName() {
        return name;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column devops_variable.name
     *
     * @param name the value for devops_variable.name
     *
     * @mbg.generated
     */
    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column devops_variable.value
     *
     * @return the value of devops_variable.value
     *
     * @mbg.generated
     */
    public String getValue() {
        return value;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column devops_variable.value
     *
     * @param value the value for devops_variable.value
     *
     * @mbg.generated
     */
    public void setValue(String value) {
        this.value = value == null ? null : value.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column devops_variable.created_time
     *
     * @return the value of devops_variable.created_time
     *
     * @mbg.generated
     */
    public Long getCreatedTime() {
        return createdTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column devops_variable.created_time
     *
     * @param createdTime the value for devops_variable.created_time
     *
     * @mbg.generated
     */
    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column devops_variable.resource_type
     *
     * @return the value of devops_variable.resource_type
     *
     * @mbg.generated
     */
    public String getResourceType() {
        return resourceType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column devops_variable.resource_type
     *
     * @param resourceType the value for devops_variable.resource_type
     *
     * @mbg.generated
     */
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType == null ? null : resourceType.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column devops_variable.resource_id
     *
     * @return the value of devops_variable.resource_id
     *
     * @mbg.generated
     */
    public String getResourceId() {
        return resourceId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column devops_variable.resource_id
     *
     * @param resourceId the value for devops_variable.resource_id
     *
     * @mbg.generated
     */
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId == null ? null : resourceId.trim();
    }
}