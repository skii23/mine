package com.fit2cloud.devops.base.domain;

import java.io.Serializable;

public class ClusterRole implements Serializable {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column devops_cluster_role.id
     *
     * @mbg.generated
     */
    private String id;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column devops_cluster_role.name
     *
     * @mbg.generated
     */
    private String name;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column devops_cluster_role.cluster_id
     *
     * @mbg.generated
     */
    private String clusterId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column devops_cluster_role.created_time
     *
     * @mbg.generated
     */
    private Long createdTime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column devops_cluster_role.description
     *
     * @mbg.generated
     */
    private String description;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table devops_cluster_role
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column devops_cluster_role.id
     *
     * @return the value of devops_cluster_role.id
     *
     * @mbg.generated
     */
    public String getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column devops_cluster_role.id
     *
     * @param id the value for devops_cluster_role.id
     *
     * @mbg.generated
     */
    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column devops_cluster_role.name
     *
     * @return the value of devops_cluster_role.name
     *
     * @mbg.generated
     */
    public String getName() {
        return name;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column devops_cluster_role.name
     *
     * @param name the value for devops_cluster_role.name
     *
     * @mbg.generated
     */
    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column devops_cluster_role.cluster_id
     *
     * @return the value of devops_cluster_role.cluster_id
     *
     * @mbg.generated
     */
    public String getClusterId() {
        return clusterId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column devops_cluster_role.cluster_id
     *
     * @param clusterId the value for devops_cluster_role.cluster_id
     *
     * @mbg.generated
     */
    public void setClusterId(String clusterId) {
        this.clusterId = clusterId == null ? null : clusterId.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column devops_cluster_role.created_time
     *
     * @return the value of devops_cluster_role.created_time
     *
     * @mbg.generated
     */
    public Long getCreatedTime() {
        return createdTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column devops_cluster_role.created_time
     *
     * @param createdTime the value for devops_cluster_role.created_time
     *
     * @mbg.generated
     */
    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column devops_cluster_role.description
     *
     * @return the value of devops_cluster_role.description
     *
     * @mbg.generated
     */
    public String getDescription() {
        return description;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column devops_cluster_role.description
     *
     * @param description the value for devops_cluster_role.description
     *
     * @mbg.generated
     */
    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }
}