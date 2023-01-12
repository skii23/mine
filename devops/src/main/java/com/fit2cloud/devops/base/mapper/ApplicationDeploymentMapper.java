package com.fit2cloud.devops.base.mapper;

import com.fit2cloud.devops.base.domain.ApplicationDeployment;
import com.fit2cloud.devops.base.domain.ApplicationDeploymentExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApplicationDeploymentMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table devops_application_deployment
     *
     * @mbg.generated
     */
    long countByExample(ApplicationDeploymentExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table devops_application_deployment
     *
     * @mbg.generated
     */
    int deleteByExample(ApplicationDeploymentExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table devops_application_deployment
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(String id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table devops_application_deployment
     *
     * @mbg.generated
     */
    int insert(ApplicationDeployment record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table devops_application_deployment
     *
     * @mbg.generated
     */
    int insertSelective(ApplicationDeployment record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table devops_application_deployment
     *
     * @mbg.generated
     */
    List<ApplicationDeployment> selectByExample(ApplicationDeploymentExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table devops_application_deployment
     *
     * @mbg.generated
     */
    ApplicationDeployment selectByPrimaryKey(String id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table devops_application_deployment
     *
     * @mbg.generated
     */
    int updateByExampleSelective(@Param("record") ApplicationDeployment record, @Param("example") ApplicationDeploymentExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table devops_application_deployment
     *
     * @mbg.generated
     */
    int updateByExample(@Param("record") ApplicationDeployment record, @Param("example") ApplicationDeploymentExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table devops_application_deployment
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(ApplicationDeployment record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table devops_application_deployment
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(ApplicationDeployment record);
}