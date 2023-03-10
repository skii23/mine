package com.fit2cloud.devops.base.domain;

import java.io.Serializable;

public class DevopsJenkinsParams implements Serializable {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column devops_jenkins_params.id
     *
     * @mbg.generated
     */
    private String id;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column devops_jenkins_params.param_key
     *
     * @mbg.generated
     */
    private String paramKey;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column devops_jenkins_params.alias
     *
     * @mbg.generated
     */
    private String alias;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column devops_jenkins_params.param_value
     *
     * @mbg.generated
     */
    private String paramValue;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table devops_jenkins_params
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column devops_jenkins_params.id
     *
     * @return the value of devops_jenkins_params.id
     *
     * @mbg.generated
     */
    public String getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column devops_jenkins_params.id
     *
     * @param id the value for devops_jenkins_params.id
     *
     * @mbg.generated
     */
    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column devops_jenkins_params.param_key
     *
     * @return the value of devops_jenkins_params.param_key
     *
     * @mbg.generated
     */
    public String getParamKey() {
        return paramKey;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column devops_jenkins_params.param_key
     *
     * @param paramKey the value for devops_jenkins_params.param_key
     *
     * @mbg.generated
     */
    public void setParamKey(String paramKey) {
        this.paramKey = paramKey == null ? null : paramKey.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column devops_jenkins_params.alias
     *
     * @return the value of devops_jenkins_params.alias
     *
     * @mbg.generated
     */
    public String getAlias() {
        return alias;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column devops_jenkins_params.alias
     *
     * @param alias the value for devops_jenkins_params.alias
     *
     * @mbg.generated
     */
    public void setAlias(String alias) {
        this.alias = alias == null ? null : alias.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column devops_jenkins_params.param_value
     *
     * @return the value of devops_jenkins_params.param_value
     *
     * @mbg.generated
     */
    public String getParamValue() {
        return paramValue;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column devops_jenkins_params.param_value
     *
     * @param paramValue the value for devops_jenkins_params.param_value
     *
     * @mbg.generated
     */
    public void setParamValue(String paramValue) {
        this.paramValue = paramValue == null ? null : paramValue.trim();
    }
}