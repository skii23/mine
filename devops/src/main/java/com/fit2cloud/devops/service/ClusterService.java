package com.fit2cloud.devops.service;


import com.fit2cloud.commons.server.base.domain.TagMapping;
import com.fit2cloud.commons.server.exception.F2CException;
import com.fit2cloud.commons.server.service.TagMappingService;
import com.fit2cloud.commons.server.utils.SessionUtils;
import com.fit2cloud.commons.utils.UUIDUtil;
import com.fit2cloud.devops.base.domain.ApplicationVersion;
import com.fit2cloud.devops.base.domain.Cluster;
import com.fit2cloud.devops.base.domain.ClusterExample;
import com.fit2cloud.devops.base.mapper.ClusterMapper;
import com.fit2cloud.devops.common.consts.TagResourceType;
import com.fit2cloud.devops.common.util.CommonUtils;
import com.fit2cloud.devops.dao.ext.ExtClusterMapper;
import com.fit2cloud.devops.dto.ClusterDTO;
import com.fit2cloud.devops.dto.ClusterTagDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class ClusterService {

    @Resource
    private ClusterMapper clusterMapper;
    @Resource
    private ExtClusterMapper extClusterMapper;
    @Resource
    private TagMappingService tagMappingService;


    public Cluster saveCluster(ClusterTagDTO cluster) {
        saveCheck(cluster);
        if (StringUtils.isEmpty(cluster.getId())) {
            if (cluster.getWorkspaceId() == null) {
                cluster.setWorkspaceId(SessionUtils.getWorkspaceId());
            }
            cluster.setId(UUIDUtil.newUUID());
            cluster.setCreatedTime(System.currentTimeMillis());
            clusterMapper.insert(cluster);
            if (CollectionUtils.isNotEmpty(cluster.getTagMappings())) {
                cluster.getTagMappings().forEach(tagMapping -> {
                    tagMapping.setResourceId(cluster.getId());
                    tagMapping.setResourceType("DEVOPS_CLUSTER");
                });
                try {
                    tagMappingService.saveTagMappings(cluster.getTagMappings());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            clusterMapper.updateByPrimaryKeySelective(cluster);
        }
        return cluster;
    }

    public void deleteCluster(String clusterId) {
        deleteCheck(clusterId);
        clusterMapper.deleteByPrimaryKey(clusterId);
    }

    private void saveCheck(Cluster cluster) {
        ClusterExample clusterExample = new ClusterExample();
        String workspaceId = SessionUtils.getWorkspaceId();
        if (cluster.getId() != null) {
            clusterExample.createCriteria().andNameEqualTo(cluster.getName()).andWorkspaceIdEqualTo(workspaceId).andIdNotEqualTo(cluster.getId());
        } else {
            clusterExample.createCriteria().andNameEqualTo(cluster.getName()).andWorkspaceIdEqualTo(workspaceId);
        }
        List<Cluster> clusters = clusterMapper.selectByExample(clusterExample);
        if (clusters.size() != 0) {
            F2CException.throwException("名称：" + cluster.getName() + "已存在！");
        }
    }

    private void deleteCheck(String clusterId) throws F2CException {
        Map<Object, Object> params = new HashMap<>();
        params.put("id", clusterId);
        List<ClusterDTO> clusterDTOS = extClusterMapper.selectCluster(params);
        if (CollectionUtils.isNotEmpty(clusterDTOS)) {
            ClusterDTO clusterDTO = clusterDTOS.get(0);
            if (clusterDTO.getCountClusterRole() != 0 || clusterDTO.getCountServer() != 0) {
                F2CException.throwException("删除失败，" + clusterDTO.getName() + "集群下资源不为空！");

            }
        } else {
            F2CException.throwException("集群不存在！");
        }
    }

    public List<ClusterDTO> getClustersByAppversion(ApplicationVersion applicationVersion) {
        String applicationId = applicationVersion.getApplicationId();
        String applicationVersionId = applicationVersion.getId();
        //查询对应标签
        Map<String, String> params = new HashMap<>();
        params.put("resourceType", TagResourceType.APPLICATION);
        params.put("resourceId", applicationId);
        params.put("tagKey", "business");
        List<TagMapping> appTagMappings = tagMappingService.selectTagMappings(params);

        params = new HashMap<>();
        params.put("resourceType", TagResourceType.VERSION);
        params.put("resourceId", applicationVersionId);
        params.put("tagKey", "environment");
        List<TagMapping> appVersionTagMappings = tagMappingService.selectTagMappings(params);

        Map<String, Object> clusterParams = new HashMap<String, Object>();
        //没有标签返回所有可见集群
        if (CollectionUtils.isNotEmpty(appTagMappings) && CollectionUtils.isEmpty(appVersionTagMappings)) {
            TagMapping appTag = appTagMappings.get(0);
            clusterParams.put("systemValueId", appTag.getTagValueId());
            return selectCluster(clusterParams);
        } else if (CollectionUtils.isEmpty(appTagMappings) && CollectionUtils.isNotEmpty(appVersionTagMappings)) {
            TagMapping appVersion = appVersionTagMappings.get(0);
            clusterParams.put("envValueId", appVersion.getTagValueId());
            return selectCluster(clusterParams);
        } else if (CollectionUtils.isNotEmpty(appTagMappings) && CollectionUtils.isNotEmpty(appVersionTagMappings)) {
            TagMapping appTag = appTagMappings.get(0);
            TagMapping appVersion = appVersionTagMappings.get(0);
            clusterParams.put("systemValueId", appTag.getTagValueId());
            clusterParams.put("envValueId", appVersion.getTagValueId());
        }
        return selectCluster(clusterParams);
    }


    public List<ClusterDTO> selectCluster(Map<String, Object> params) {
        CommonUtils.filterPermission(params);
        return extClusterMapper.selectCluster(params);
    }

    public List<ClusterDTO> listAll(Map<String, Object> params) {
        if (params == null) {
            params = new HashMap<>();
        }
        CommonUtils.filterPermission(params);
        return extClusterMapper.selectCluster(params);
    }

    public Cluster selectClusterById(String clusterId) {
        return clusterMapper.selectByPrimaryKey(clusterId);
    }
}
