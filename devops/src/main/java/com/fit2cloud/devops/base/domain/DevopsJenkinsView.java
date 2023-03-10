package com.fit2cloud.devops.base.domain;

import java.io.Serializable;

public class DevopsJenkinsView implements Serializable {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column devops_jenkins_view.id
     *
     * @mbg.generated
     */
    private String id;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column devops_jenkins_view.name
     *
     * @mbg.generated
     */
    private String name;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column devops_jenkins_view.organization
     *
     * @mbg.generated
     */
    private String organization;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column devops_jenkins_view.workspace
     *
     * @mbg.generated
     */
    private String workspace;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column devops_jenkins_view.creator
     *
     * @mbg.generated
     */
    private String creator;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column devops_jenkins_view.create_time
     *
     * @mbg.generated
     */
    private Long createTime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column devops_jenkins_view.update_time
     *
     * @mbg.generated
     */
    private Long updateTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table devops_jenkins_view
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column devops_jenkins_view.id
     *
     * @return the value of devops_jenkins_view.id
     *
     * @mbg.generated
     */
    public String getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column devops_jenkins_view.id
     *
     * @param id the value for devops_jenkins_view.id
     *
     * @mbg.generated
     */
    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column devops_jenkins_view.name
     *
     * @return the value of devops_jenkins_view.name
     *
     * @mbg.generated
     */
    public String getName() {
        return name;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column devops_jenkins_view.name
     *
     * @param name the value for devops_jenkins_view.name
     *
     * @mbg.generated
     */
    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column devops_jenkins_view.organization
     *
     * @return the value of devops_jenkins_view.organization
     *
     * @mbg.generated
     */
    public String getOrganization() {
        return organization;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column devops_jenkins_view.organization
     *
     * @param organization the value for devops_jenkins_view.organization
     *
     * @mbg.generated
     */
    public void setOrganization(String organization) {
        this.organization = organization == null ? null : organization.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column devops_jenkins_view.workspace
     *
     * @return the value of devops_jenkins_view.workspace
     *
     * @mbg.generated
     */
    public String getWorkspace() {
        return workspace;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column devops_jenkins_view.workspace
     *
     * @param workspace the value for devops_jenkins_view.workspace
     *
     * @mbg.generated
     */
    public void setWorkspace(String workspace) {
        this.workspace = workspace == null ? null : workspace.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column devops_jenkins_view.creator
     *
     * @return the value of devops_jenkins_view.creator
     *
     * @mbg.generated
     */
    public String getCreator() {
        return creator;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column devops_jenkins_view.creator
     *
     * @param creator the value for devops_jenkins_view.creator
     *
     * @mbg.generated
     */
    public void setCreator(String creator) {
        this.creator = creator == null ? null : creator.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column devops_jenkins_view.create_time
     *
     * @return the value of devops_jenkins_view.create_time
     *
     * @mbg.generated
     */
    public Long getCreateTime() {
        return createTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column devops_jenkins_view.create_time
     *
     * @param createTime the value for devops_jenkins_view.create_time
     *
     * @mbg.generated
     */
    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column devops_jenkins_view.update_time
     *
     * @return the value of devops_jenkins_view.update_time
     *
     * @mbg.generated
     */
    public Long getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column devops_jenkins_view.update_time
     *
     * @param updateTime the value for devops_jenkins_view.update_time
     *
     * @mbg.generated
     */
    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }
}