package com.fit2cloud.devops.base.domain;

import java.util.ArrayList;
import java.util.List;

public class ScriptImplementLogExample {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table devops_script_implement_log
     *
     * @mbg.generated
     */
    protected String orderByClause;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table devops_script_implement_log
     *
     * @mbg.generated
     */
    protected boolean distinct;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table devops_script_implement_log
     *
     * @mbg.generated
     */
    protected List<Criteria> oredCriteria;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table devops_script_implement_log
     *
     * @mbg.generated
     */
    public ScriptImplementLogExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table devops_script_implement_log
     *
     * @mbg.generated
     */
    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table devops_script_implement_log
     *
     * @mbg.generated
     */
    public String getOrderByClause() {
        return orderByClause;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table devops_script_implement_log
     *
     * @mbg.generated
     */
    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table devops_script_implement_log
     *
     * @mbg.generated
     */
    public boolean isDistinct() {
        return distinct;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table devops_script_implement_log
     *
     * @mbg.generated
     */
    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table devops_script_implement_log
     *
     * @mbg.generated
     */
    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table devops_script_implement_log
     *
     * @mbg.generated
     */
    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table devops_script_implement_log
     *
     * @mbg.generated
     */
    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table devops_script_implement_log
     *
     * @mbg.generated
     */
    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table devops_script_implement_log
     *
     * @mbg.generated
     */
    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    /**
     * This class was generated by MyBatis Generator.
     * This class corresponds to the database table devops_script_implement_log
     *
     * @mbg.generated
     */
    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(String value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(String value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(String value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(String value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(String value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(String value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLike(String value) {
            addCriterion("id like", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotLike(String value) {
            addCriterion("id not like", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<String> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<String> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(String value1, String value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(String value1, String value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andScriptIdIsNull() {
            addCriterion("script_id is null");
            return (Criteria) this;
        }

        public Criteria andScriptIdIsNotNull() {
            addCriterion("script_id is not null");
            return (Criteria) this;
        }

        public Criteria andScriptIdEqualTo(String value) {
            addCriterion("script_id =", value, "scriptId");
            return (Criteria) this;
        }

        public Criteria andScriptIdNotEqualTo(String value) {
            addCriterion("script_id <>", value, "scriptId");
            return (Criteria) this;
        }

        public Criteria andScriptIdGreaterThan(String value) {
            addCriterion("script_id >", value, "scriptId");
            return (Criteria) this;
        }

        public Criteria andScriptIdGreaterThanOrEqualTo(String value) {
            addCriterion("script_id >=", value, "scriptId");
            return (Criteria) this;
        }

        public Criteria andScriptIdLessThan(String value) {
            addCriterion("script_id <", value, "scriptId");
            return (Criteria) this;
        }

        public Criteria andScriptIdLessThanOrEqualTo(String value) {
            addCriterion("script_id <=", value, "scriptId");
            return (Criteria) this;
        }

        public Criteria andScriptIdLike(String value) {
            addCriterion("script_id like", value, "scriptId");
            return (Criteria) this;
        }

        public Criteria andScriptIdNotLike(String value) {
            addCriterion("script_id not like", value, "scriptId");
            return (Criteria) this;
        }

        public Criteria andScriptIdIn(List<String> values) {
            addCriterion("script_id in", values, "scriptId");
            return (Criteria) this;
        }

        public Criteria andScriptIdNotIn(List<String> values) {
            addCriterion("script_id not in", values, "scriptId");
            return (Criteria) this;
        }

        public Criteria andScriptIdBetween(String value1, String value2) {
            addCriterion("script_id between", value1, value2, "scriptId");
            return (Criteria) this;
        }

        public Criteria andScriptIdNotBetween(String value1, String value2) {
            addCriterion("script_id not between", value1, value2, "scriptId");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeIsNull() {
            addCriterion("created_time is null");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeIsNotNull() {
            addCriterion("created_time is not null");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeEqualTo(Long value) {
            addCriterion("created_time =", value, "createdTime");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeNotEqualTo(Long value) {
            addCriterion("created_time <>", value, "createdTime");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeGreaterThan(Long value) {
            addCriterion("created_time >", value, "createdTime");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("created_time >=", value, "createdTime");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeLessThan(Long value) {
            addCriterion("created_time <", value, "createdTime");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeLessThanOrEqualTo(Long value) {
            addCriterion("created_time <=", value, "createdTime");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeIn(List<Long> values) {
            addCriterion("created_time in", values, "createdTime");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeNotIn(List<Long> values) {
            addCriterion("created_time not in", values, "createdTime");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeBetween(Long value1, Long value2) {
            addCriterion("created_time between", value1, value2, "createdTime");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeNotBetween(Long value1, Long value2) {
            addCriterion("created_time not between", value1, value2, "createdTime");
            return (Criteria) this;
        }

        public Criteria andStatusIsNull() {
            addCriterion("status is null");
            return (Criteria) this;
        }

        public Criteria andStatusIsNotNull() {
            addCriterion("status is not null");
            return (Criteria) this;
        }

        public Criteria andStatusEqualTo(String value) {
            addCriterion("status =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(String value) {
            addCriterion("status <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(String value) {
            addCriterion("status >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(String value) {
            addCriterion("status >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(String value) {
            addCriterion("status <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(String value) {
            addCriterion("status <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLike(String value) {
            addCriterion("status like", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotLike(String value) {
            addCriterion("status not like", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<String> values) {
            addCriterion("status in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<String> values) {
            addCriterion("status not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(String value1, String value2) {
            addCriterion("status between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(String value1, String value2) {
            addCriterion("status not between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andCompletedTimeIsNull() {
            addCriterion("completed_time is null");
            return (Criteria) this;
        }

        public Criteria andCompletedTimeIsNotNull() {
            addCriterion("completed_time is not null");
            return (Criteria) this;
        }

        public Criteria andCompletedTimeEqualTo(Long value) {
            addCriterion("completed_time =", value, "completedTime");
            return (Criteria) this;
        }

        public Criteria andCompletedTimeNotEqualTo(Long value) {
            addCriterion("completed_time <>", value, "completedTime");
            return (Criteria) this;
        }

        public Criteria andCompletedTimeGreaterThan(Long value) {
            addCriterion("completed_time >", value, "completedTime");
            return (Criteria) this;
        }

        public Criteria andCompletedTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("completed_time >=", value, "completedTime");
            return (Criteria) this;
        }

        public Criteria andCompletedTimeLessThan(Long value) {
            addCriterion("completed_time <", value, "completedTime");
            return (Criteria) this;
        }

        public Criteria andCompletedTimeLessThanOrEqualTo(Long value) {
            addCriterion("completed_time <=", value, "completedTime");
            return (Criteria) this;
        }

        public Criteria andCompletedTimeIn(List<Long> values) {
            addCriterion("completed_time in", values, "completedTime");
            return (Criteria) this;
        }

        public Criteria andCompletedTimeNotIn(List<Long> values) {
            addCriterion("completed_time not in", values, "completedTime");
            return (Criteria) this;
        }

        public Criteria andCompletedTimeBetween(Long value1, Long value2) {
            addCriterion("completed_time between", value1, value2, "completedTime");
            return (Criteria) this;
        }

        public Criteria andCompletedTimeNotBetween(Long value1, Long value2) {
            addCriterion("completed_time not between", value1, value2, "completedTime");
            return (Criteria) this;
        }

        public Criteria andCloudServerIdIsNull() {
            addCriterion("cloud_server_id is null");
            return (Criteria) this;
        }

        public Criteria andCloudServerIdIsNotNull() {
            addCriterion("cloud_server_id is not null");
            return (Criteria) this;
        }

        public Criteria andCloudServerIdEqualTo(String value) {
            addCriterion("cloud_server_id =", value, "cloudServerId");
            return (Criteria) this;
        }

        public Criteria andCloudServerIdNotEqualTo(String value) {
            addCriterion("cloud_server_id <>", value, "cloudServerId");
            return (Criteria) this;
        }

        public Criteria andCloudServerIdGreaterThan(String value) {
            addCriterion("cloud_server_id >", value, "cloudServerId");
            return (Criteria) this;
        }

        public Criteria andCloudServerIdGreaterThanOrEqualTo(String value) {
            addCriterion("cloud_server_id >=", value, "cloudServerId");
            return (Criteria) this;
        }

        public Criteria andCloudServerIdLessThan(String value) {
            addCriterion("cloud_server_id <", value, "cloudServerId");
            return (Criteria) this;
        }

        public Criteria andCloudServerIdLessThanOrEqualTo(String value) {
            addCriterion("cloud_server_id <=", value, "cloudServerId");
            return (Criteria) this;
        }

        public Criteria andCloudServerIdLike(String value) {
            addCriterion("cloud_server_id like", value, "cloudServerId");
            return (Criteria) this;
        }

        public Criteria andCloudServerIdNotLike(String value) {
            addCriterion("cloud_server_id not like", value, "cloudServerId");
            return (Criteria) this;
        }

        public Criteria andCloudServerIdIn(List<String> values) {
            addCriterion("cloud_server_id in", values, "cloudServerId");
            return (Criteria) this;
        }

        public Criteria andCloudServerIdNotIn(List<String> values) {
            addCriterion("cloud_server_id not in", values, "cloudServerId");
            return (Criteria) this;
        }

        public Criteria andCloudServerIdBetween(String value1, String value2) {
            addCriterion("cloud_server_id between", value1, value2, "cloudServerId");
            return (Criteria) this;
        }

        public Criteria andCloudServerIdNotBetween(String value1, String value2) {
            addCriterion("cloud_server_id not between", value1, value2, "cloudServerId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdIsNull() {
            addCriterion("workspace_id is null");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdIsNotNull() {
            addCriterion("workspace_id is not null");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdEqualTo(String value) {
            addCriterion("workspace_id =", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdNotEqualTo(String value) {
            addCriterion("workspace_id <>", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdGreaterThan(String value) {
            addCriterion("workspace_id >", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdGreaterThanOrEqualTo(String value) {
            addCriterion("workspace_id >=", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdLessThan(String value) {
            addCriterion("workspace_id <", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdLessThanOrEqualTo(String value) {
            addCriterion("workspace_id <=", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdLike(String value) {
            addCriterion("workspace_id like", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdNotLike(String value) {
            addCriterion("workspace_id not like", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdIn(List<String> values) {
            addCriterion("workspace_id in", values, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdNotIn(List<String> values) {
            addCriterion("workspace_id not in", values, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdBetween(String value1, String value2) {
            addCriterion("workspace_id between", value1, value2, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdNotBetween(String value1, String value2) {
            addCriterion("workspace_id not between", value1, value2, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andAnsibleTaskIdIsNull() {
            addCriterion("ansible_task_id is null");
            return (Criteria) this;
        }

        public Criteria andAnsibleTaskIdIsNotNull() {
            addCriterion("ansible_task_id is not null");
            return (Criteria) this;
        }

        public Criteria andAnsibleTaskIdEqualTo(String value) {
            addCriterion("ansible_task_id =", value, "ansibleTaskId");
            return (Criteria) this;
        }

        public Criteria andAnsibleTaskIdNotEqualTo(String value) {
            addCriterion("ansible_task_id <>", value, "ansibleTaskId");
            return (Criteria) this;
        }

        public Criteria andAnsibleTaskIdGreaterThan(String value) {
            addCriterion("ansible_task_id >", value, "ansibleTaskId");
            return (Criteria) this;
        }

        public Criteria andAnsibleTaskIdGreaterThanOrEqualTo(String value) {
            addCriterion("ansible_task_id >=", value, "ansibleTaskId");
            return (Criteria) this;
        }

        public Criteria andAnsibleTaskIdLessThan(String value) {
            addCriterion("ansible_task_id <", value, "ansibleTaskId");
            return (Criteria) this;
        }

        public Criteria andAnsibleTaskIdLessThanOrEqualTo(String value) {
            addCriterion("ansible_task_id <=", value, "ansibleTaskId");
            return (Criteria) this;
        }

        public Criteria andAnsibleTaskIdLike(String value) {
            addCriterion("ansible_task_id like", value, "ansibleTaskId");
            return (Criteria) this;
        }

        public Criteria andAnsibleTaskIdNotLike(String value) {
            addCriterion("ansible_task_id not like", value, "ansibleTaskId");
            return (Criteria) this;
        }

        public Criteria andAnsibleTaskIdIn(List<String> values) {
            addCriterion("ansible_task_id in", values, "ansibleTaskId");
            return (Criteria) this;
        }

        public Criteria andAnsibleTaskIdNotIn(List<String> values) {
            addCriterion("ansible_task_id not in", values, "ansibleTaskId");
            return (Criteria) this;
        }

        public Criteria andAnsibleTaskIdBetween(String value1, String value2) {
            addCriterion("ansible_task_id between", value1, value2, "ansibleTaskId");
            return (Criteria) this;
        }

        public Criteria andAnsibleTaskIdNotBetween(String value1, String value2) {
            addCriterion("ansible_task_id not between", value1, value2, "ansibleTaskId");
            return (Criteria) this;
        }

        public Criteria andExecutePathIsNull() {
            addCriterion("execute_path is null");
            return (Criteria) this;
        }

        public Criteria andExecutePathIsNotNull() {
            addCriterion("execute_path is not null");
            return (Criteria) this;
        }

        public Criteria andExecutePathEqualTo(String value) {
            addCriterion("execute_path =", value, "executePath");
            return (Criteria) this;
        }

        public Criteria andExecutePathNotEqualTo(String value) {
            addCriterion("execute_path <>", value, "executePath");
            return (Criteria) this;
        }

        public Criteria andExecutePathGreaterThan(String value) {
            addCriterion("execute_path >", value, "executePath");
            return (Criteria) this;
        }

        public Criteria andExecutePathGreaterThanOrEqualTo(String value) {
            addCriterion("execute_path >=", value, "executePath");
            return (Criteria) this;
        }

        public Criteria andExecutePathLessThan(String value) {
            addCriterion("execute_path <", value, "executePath");
            return (Criteria) this;
        }

        public Criteria andExecutePathLessThanOrEqualTo(String value) {
            addCriterion("execute_path <=", value, "executePath");
            return (Criteria) this;
        }

        public Criteria andExecutePathLike(String value) {
            addCriterion("execute_path like", value, "executePath");
            return (Criteria) this;
        }

        public Criteria andExecutePathNotLike(String value) {
            addCriterion("execute_path not like", value, "executePath");
            return (Criteria) this;
        }

        public Criteria andExecutePathIn(List<String> values) {
            addCriterion("execute_path in", values, "executePath");
            return (Criteria) this;
        }

        public Criteria andExecutePathNotIn(List<String> values) {
            addCriterion("execute_path not in", values, "executePath");
            return (Criteria) this;
        }

        public Criteria andExecutePathBetween(String value1, String value2) {
            addCriterion("execute_path between", value1, value2, "executePath");
            return (Criteria) this;
        }

        public Criteria andExecutePathNotBetween(String value1, String value2) {
            addCriterion("execute_path not between", value1, value2, "executePath");
            return (Criteria) this;
        }

        public Criteria andSqlCriterion(String value) {
            addCriterion("(" + value + ")");
            return (Criteria) this;
        }
    }

    /**
     * This class was generated by MyBatis Generator.
     * This class corresponds to the database table devops_script_implement_log
     *
     * @mbg.generated do_not_delete_during_merge
     */
    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    /**
     * This class was generated by MyBatis Generator.
     * This class corresponds to the database table devops_script_implement_log
     *
     * @mbg.generated
     */
    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}