package com.fit2cloud.devops.common.consts;

import com.fit2cloud.devops.service.jenkins.model.common.GlobalSettings;
import com.fit2cloud.devops.service.jenkins.model.common.RunPostStepsIfResult;
import com.fit2cloud.devops.service.jenkins.model.common.Settings;
import com.fit2cloud.devops.service.jenkins.model.maven.LocalRepository;
import com.fit2cloud.devops.service.jenkins.model.maven.RootModule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * jenkins静态常量
 *
 * @author makai
 */
public class JenkinsConstants {

    public static final String F2C_ENDPOINT_PARAM = "f2c_endpoint";

    public static final String JENKINS_SYNC_TRIGGER_KEY = "jenkins_sync";
    public static final String JENKINS_ADDRESS_PARAM = "jenkins.address";
    public static final String JENKINS_USERNAME_PARAM = "jenkins.username";
    public static final String JENKINS_PASSWORD_PARAM = "jenkins.password";
    public static final String JENKINS_CRON_FLAG_PARAM = "jenkins.enableCronSync";
    public static final String JENKINS_CRON_SPEC_PARAM = "jenkins.cronSyncSpec";
    public static final String JENKINS_SYNC_STATUS_PARAM = "jenkins.syncStatus";
    public static final String JENKINS_DIRECTORY_SELECTOR = "jenkins.directorySelector";
    public static final String JENKINS_FILE_SELECTOR = "jenkins.fileSelector";

    public static final String NORMAL_CREDENTIAL_STAPLER_CLASS = "com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl";
    public static final String SSH_CREDENTIAL_STAPLER_CLASS = "com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey";
    public static final String PRIVATE_KEY_CREDENTIAL_STAPLER_CLASS = "com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey$DirectEntryPrivateKeySource";


    public static final Set<String> SUPPORTED_NODE_SET = new HashSet<>();
    public static final Set<Class<?>> BASE_TYPE_SET = new HashSet<>();

    public static final String JOB_TYPE_MAVEN = "MAVEN";
    public static final String JOB_TYPE_FREESTYLE = "FREE_STYLE";
    public static final String JOB_TYPE_FLOW = "FLOW";
    public static final String JOB_TYPE_MULTIBRANCH = "WORKFLOW_MULTI_BRANCH";

    static {
//        用于标记可解析的一级xml节点
        SUPPORTED_NODE_SET.addAll(FieldMapHolder.BASE_JOB_MODEL_FIELDS_MAP.keySet());
        SUPPORTED_NODE_SET.addAll(FieldMapHolder.MAVEN_JOB_MODEL_FIELDS_MAP.keySet());
        SUPPORTED_NODE_SET.add("scm");
        SUPPORTED_NODE_SET.add("settings");
        SUPPORTED_NODE_SET.add("globalSettings");
        SUPPORTED_NODE_SET.add("runPostStepsIfResult");
        SUPPORTED_NODE_SET.add("properties");
        SUPPORTED_NODE_SET.add("triggers");
        SUPPORTED_NODE_SET.add("prebuilders");
        SUPPORTED_NODE_SET.add("builders");
        SUPPORTED_NODE_SET.add("postbuilders");
        SUPPORTED_NODE_SET.add("publishers");
        SUPPORTED_NODE_SET.add("buildWrappers");
//        基础数据类型
        BASE_TYPE_SET.add(String.class);
        BASE_TYPE_SET.add(Integer.class);
        BASE_TYPE_SET.add(Boolean.class);
    }


    /**
     * 同步状态
     */
    public enum SyncStatus {
        /**
         * 正在同步
         */
        IN_SYNC,
        /**
         * 未同步过
         */
        NO_SYNC,
        /**
         * 同步完成
         */
        END_SYNC,
        /**
         * 同步错误
         */
        ERROR_SYNC
    }

    /**
     * job类型
     */

    public enum JobType {
        /**
         * 自由风格
         */
        FREE_STYLE,
        /**
         * maven项目
         */
        MAVEN,
        /**
         * 未知
         */
        UNKNOWN
    }

    public static class FieldMapHolder {
        public static final Map<String, Class<?>> BASE_JOB_MODEL_FIELDS_MAP = new HashMap<>();
        public static final Map<String, Class<?>> MAVEN_JOB_MODEL_FIELDS_MAP = new HashMap<>();

        static {
            BASE_JOB_MODEL_FIELDS_MAP.put("description", String.class);
            BASE_JOB_MODEL_FIELDS_MAP.put("jdk", String.class);
            BASE_JOB_MODEL_FIELDS_MAP.put("canRoam", boolean.class);
            BASE_JOB_MODEL_FIELDS_MAP.put("keepDependencies", boolean.class);
            BASE_JOB_MODEL_FIELDS_MAP.put("disabled", boolean.class);
            BASE_JOB_MODEL_FIELDS_MAP.put("blockBuildWhenDownstreamBuilding", boolean.class);
            BASE_JOB_MODEL_FIELDS_MAP.put("blockBuildWhenUpstreamBuilding", boolean.class);
            BASE_JOB_MODEL_FIELDS_MAP.put("concurrentBuild", boolean.class);
            BASE_JOB_MODEL_FIELDS_MAP.put("authToken", String.class);
            BASE_JOB_MODEL_FIELDS_MAP.put("quietPeriod", int.class);
            BASE_JOB_MODEL_FIELDS_MAP.put("scmCheckoutRetryCount", int.class);
            BASE_JOB_MODEL_FIELDS_MAP.put("assignedNode", String.class);

            MAVEN_JOB_MODEL_FIELDS_MAP.put("aggregatorStyleBuild", boolean.class);
            MAVEN_JOB_MODEL_FIELDS_MAP.put("ignoreUpstremChanges", boolean.class);
            MAVEN_JOB_MODEL_FIELDS_MAP.put("ignoreUnsuccessfulUpstreams", boolean.class);
            MAVEN_JOB_MODEL_FIELDS_MAP.put("incrementalBuild", boolean.class);
            MAVEN_JOB_MODEL_FIELDS_MAP.put("archivingDisabled", boolean.class);
            MAVEN_JOB_MODEL_FIELDS_MAP.put("siteArchivingDisabled", boolean.class);
            MAVEN_JOB_MODEL_FIELDS_MAP.put("fingerprintingDisabled", boolean.class);
            MAVEN_JOB_MODEL_FIELDS_MAP.put("disableTriggerDownstreamProjects", boolean.class);
            MAVEN_JOB_MODEL_FIELDS_MAP.put("blockTriggerWhenBuilding", boolean.class);
            MAVEN_JOB_MODEL_FIELDS_MAP.put("perModuleBuild", boolean.class);
            MAVEN_JOB_MODEL_FIELDS_MAP.put("resolveDependencies", boolean.class);
            MAVEN_JOB_MODEL_FIELDS_MAP.put("runHeadless", boolean.class);
            MAVEN_JOB_MODEL_FIELDS_MAP.put("processPlugins", boolean.class);
            MAVEN_JOB_MODEL_FIELDS_MAP.put("reporters", String.class);
            MAVEN_JOB_MODEL_FIELDS_MAP.put("mavenName", String.class);
            MAVEN_JOB_MODEL_FIELDS_MAP.put("rootPOM", String.class);
            MAVEN_JOB_MODEL_FIELDS_MAP.put("goals", String.class);
            MAVEN_JOB_MODEL_FIELDS_MAP.put("mavenOpts", String.class);
            MAVEN_JOB_MODEL_FIELDS_MAP.put("customWorkspace", String.class);
            MAVEN_JOB_MODEL_FIELDS_MAP.put("mavenValidationLevel", int.class);
            MAVEN_JOB_MODEL_FIELDS_MAP.put("rootModule", RootModule.class);
            MAVEN_JOB_MODEL_FIELDS_MAP.put("runPostStepsIfResult", RunPostStepsIfResult.class);
            MAVEN_JOB_MODEL_FIELDS_MAP.put("localRepository", LocalRepository.class);
            MAVEN_JOB_MODEL_FIELDS_MAP.put("settings", Settings.class);
            MAVEN_JOB_MODEL_FIELDS_MAP.put("globalSettings", GlobalSettings.class);
        }

        private FieldMapHolder() {

        }
    }

}
