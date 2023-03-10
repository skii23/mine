package com.fit2cloud.devops.base.domain;

import java.io.Serializable;

public class InstanceType implements Serializable {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column vm_instance_type.id
     *
     * @mbg.generated
     */
    private Integer id;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column vm_instance_type.name
     *
     * @mbg.generated
     */
    private String name;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column vm_instance_type.service
     *
     * @mbg.generated
     */
    private String service;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column vm_instance_type.cpu
     *
     * @mbg.generated
     */
    private Integer cpu;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column vm_instance_type.memory
     *
     * @mbg.generated
     */
    private Double memory;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table vm_instance_type
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column vm_instance_type.id
     *
     * @return the value of vm_instance_type.id
     *
     * @mbg.generated
     */
    public Integer getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column vm_instance_type.id
     *
     * @param id the value for vm_instance_type.id
     *
     * @mbg.generated
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column vm_instance_type.name
     *
     * @return the value of vm_instance_type.name
     *
     * @mbg.generated
     */
    public String getName() {
        return name;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column vm_instance_type.name
     *
     * @param name the value for vm_instance_type.name
     *
     * @mbg.generated
     */
    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column vm_instance_type.service
     *
     * @return the value of vm_instance_type.service
     *
     * @mbg.generated
     */
    public String getService() {
        return service;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column vm_instance_type.service
     *
     * @param service the value for vm_instance_type.service
     *
     * @mbg.generated
     */
    public void setService(String service) {
        this.service = service == null ? null : service.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column vm_instance_type.cpu
     *
     * @return the value of vm_instance_type.cpu
     *
     * @mbg.generated
     */
    public Integer getCpu() {
        return cpu;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column vm_instance_type.cpu
     *
     * @param cpu the value for vm_instance_type.cpu
     *
     * @mbg.generated
     */
    public void setCpu(Integer cpu) {
        this.cpu = cpu;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column vm_instance_type.memory
     *
     * @return the value of vm_instance_type.memory
     *
     * @mbg.generated
     */
    public Double getMemory() {
        return memory;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column vm_instance_type.memory
     *
     * @param memory the value for vm_instance_type.memory
     *
     * @mbg.generated
     */
    public void setMemory(Double memory) {
        this.memory = memory;
    }
}