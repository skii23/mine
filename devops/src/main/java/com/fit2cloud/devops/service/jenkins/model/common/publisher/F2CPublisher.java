package com.fit2cloud.devops.service.jenkins.model.common.publisher;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * @author shaochuan.wu
 * @date 19.11.29
 */
@XStreamAlias("com.fit2cloud.codedeploy2.F2CCodeDeploySouthPublisher")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE)
public class F2CPublisher extends BasePublisherModel {

    {
        this.type = PublisherType.F2C_PUBLISHER.getJavaType();
    }

    @XStreamAsAttribute
    private String plugin = "fit2cloud2.0-jenkins-plugin@2.0";
    private String f2cEndpoint;
    private String f2cAccessKey;
    private String f2cSecretKey;
    private String workspaceId;
    private String applicationId;
    private String clusterId;
    private String clusterRoleId;
    private String cloudServerId;
    private String deployPolicy;
    private String deploymentLevel;
    private Integer backupQuantity;
    private String applicationVersionName;
    private Boolean autoDeploy;
    private String includes;
    private String excludes;
    private String appspecFilePath;
    private String description;
    private Boolean waitForCompletion;
    private Integer pollingTimeoutSec;
    private Integer pollingFreqSec;
    private String nexusGroupId;
    private String nexusArtifactId;
    private String nexusArtifactVersion;
    private String nexus3GroupId;
    private String nexus3ArtifactId;
    private String nexus3ArtifactVersion;
    private Boolean nexusChecked;
    private Boolean nexus3Checked;
    private Boolean ossChecked;
    private Boolean s3Checked;
    private Boolean artifactoryChecked;
    private String path;
    private String objectPrefixAliyun;
    private String objectPrefixAWS;
    private String repositorySettingId;
    private String artifactType;
    private boolean buildImage;
    private String imageRepoSettingId;
    private String dockerHost;
    private String imageName;
    private String imageTag;
    private String dockerfile;
    private String imageAppVersion;

    public boolean isBuildImage() {
        return buildImage;
    }

    public void setBuildImage(boolean buildImage) {
        this.buildImage = buildImage;
    }

    public String getImageRepoSettingId() {
        return imageRepoSettingId;
    }

    public void setImageRepoSettingId(String imageRepoSettingId) {
        this.imageRepoSettingId = imageRepoSettingId;
    }

    public String getDockerHost() {
        return dockerHost;
    }

    public void setDockerHost(String dockerHost) {
        this.dockerHost = dockerHost;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageTag() {
        return imageTag;
    }

    public void setImageTag(String imageTag) {
        this.imageTag = imageTag;
    }

    public String getDockerfile() {
        return dockerfile;
    }

    public void setDockerfile(String dockerfile) {
        this.dockerfile = dockerfile;
    }

    public String getImageAppVersion() {
        return imageAppVersion;
    }

    public void setImageAppVersion(String imageAppVersion) {
        this.imageAppVersion = imageAppVersion;
    }


    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        // 避免被旧插件名称覆盖 强制替换插件名称
        if (!this.plugin.equalsIgnoreCase(plugin)){
            return;
        }
        this.plugin = plugin;
    }

    public String getF2cEndpoint() {
        return f2cEndpoint;
    }

    public void setF2cEndpoint(String f2cEndpoint) {
        this.f2cEndpoint = f2cEndpoint;
    }

    public String getF2cAccessKey() {
        return f2cAccessKey;
    }

    public void setF2cAccessKey(String f2cAccessKey) {
        this.f2cAccessKey = f2cAccessKey;
    }

    public String getF2cSecretKey() {
        return f2cSecretKey;
    }

    public void setF2cSecretKey(String f2cSecretKey) {
        this.f2cSecretKey = f2cSecretKey;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getClusterRoleId() {
        return clusterRoleId;
    }

    public void setClusterRoleId(String clusterRoleId) {
        this.clusterRoleId = clusterRoleId;
    }

    public String getCloudServerId() {
        return cloudServerId;
    }

    public void setCloudServerId(String cloudServerId) {
        this.cloudServerId = cloudServerId;
    }

    public String getDeployPolicy() {
        return deployPolicy;
    }

    public void setDeployPolicy(String deployPolicy) {
        this.deployPolicy = deployPolicy;
    }

    public String getDeploymentLevel() {
        return deploymentLevel;
    }

    public void setDeploymentLevel(String deploymentLevel) {
        this.deploymentLevel = deploymentLevel;
    }

    public Integer getBackupQuantity() {
        return backupQuantity;
    }

    public void setBackupQuantity(Integer backupQuantity) {
        this.backupQuantity = backupQuantity;
    }

    public String getApplicationVersionName() {
        return applicationVersionName;
    }

    public void setApplicationVersionName(String applicationVersionName) {
        this.applicationVersionName = applicationVersionName;
    }

    public Boolean isAutoDeploy() {
        return autoDeploy;
    }

    public void setAutoDeploy(Boolean autoDeploy) {
        this.autoDeploy = autoDeploy;
    }

    public String getIncludes() {
        return includes;
    }

    public void setIncludes(String includes) {
        this.includes = includes;
    }

    public String getExcludes() {
        return excludes;
    }

    public void setExcludes(String excludes) {
        this.excludes = excludes;
    }

    public String getAppspecFilePath() {
        return appspecFilePath;
    }

    public void setAppspecFilePath(String appspecFilePath) {
        this.appspecFilePath = appspecFilePath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean isWaitForCompletion() {
        return waitForCompletion;
    }

    public void setWaitForCompletion(Boolean waitForCompletion) {
        this.waitForCompletion = waitForCompletion;
    }

    public Integer getPollingTimeoutSec() {
        return pollingTimeoutSec;
    }

    public void setPollingTimeoutSec(Integer pollingTimeoutSec) {
        this.pollingTimeoutSec = pollingTimeoutSec;
    }

    public Integer getPollingFreqSec() {
        return pollingFreqSec;
    }

    public void setPollingFreqSec(Integer pollingFreqSec) {
        this.pollingFreqSec = pollingFreqSec;
    }

    public String getNexusGroupId() {
        return nexusGroupId;
    }

    public void setNexusGroupId(String nexusGroupId) {
        this.nexusGroupId = nexusGroupId;
    }

    public String getNexusArtifactId() {
        return nexusArtifactId;
    }

    public void setNexusArtifactId(String nexusArtifactId) {
        this.nexusArtifactId = nexusArtifactId;
    }

    public String getNexusArtifactVersion() {
        return nexusArtifactVersion;
    }

    public void setNexusArtifactVersion(String nexusArtifactVersion) {
        this.nexusArtifactVersion = nexusArtifactVersion;
    }

    public String getNexus3GroupId() {
        return nexus3GroupId;
    }

    public void setNexus3GroupId(String nexus3GroupId) {
        this.nexus3GroupId = nexus3GroupId;
    }

    public String getNexus3ArtifactId() {
        return nexus3ArtifactId;
    }

    public void setNexus3ArtifactId(String nexus3ArtifactId) {
        this.nexus3ArtifactId = nexus3ArtifactId;
    }

    public String getNexus3ArtifactVersion() {
        return nexus3ArtifactVersion;
    }

    public void setNexus3ArtifactVersion(String nexus3ArtifactVersion) {
        this.nexus3ArtifactVersion = nexus3ArtifactVersion;
    }

    public Boolean isNexusChecked() {
        return nexusChecked;
    }

    public void setNexusChecked(Boolean nexusChecked) {
        this.nexusChecked = nexusChecked;
    }

    public Boolean isNexus3Checked() {
        return nexus3Checked;
    }

    public void setNexus3Checked(Boolean nexus3Checked) {
        this.nexus3Checked = nexus3Checked;
    }

    public Boolean isOssChecked() {
        return ossChecked;
    }

    public void setOssChecked(Boolean ossChecked) {
        this.ossChecked = ossChecked;
    }

    public Boolean isS3Checked() {
        return s3Checked;
    }

    public void setS3Checked(Boolean s3Checked) {
        this.s3Checked = s3Checked;
    }

    public Boolean isArtifactoryChecked() {
        return artifactoryChecked;
    }

    public void setArtifactoryChecked(Boolean artifactoryChecked) {
        this.artifactoryChecked = artifactoryChecked;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getObjectPrefixAliyun() {
        return objectPrefixAliyun;
    }

    public void setObjectPrefixAliyun(String objectPrefixAliyun) {
        this.objectPrefixAliyun = objectPrefixAliyun;
    }

    public String getObjectPrefixAWS() {
        return objectPrefixAWS;
    }

    public void setObjectPrefixAWS(String objectPrefixAWS) {
        this.objectPrefixAWS = objectPrefixAWS;
    }

    public String getRepositorySettingId() {
        return repositorySettingId;
    }

    public void setRepositorySettingId(String repositorySettingId) {
        this.repositorySettingId = repositorySettingId;
    }

    public String getArtifactType() {
        return artifactType;
    }

    public void setArtifactType(String artifactType) {
        this.artifactType = artifactType;
    }

}
