package com.fit2cloud.devops.service.oss;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.devops.base.domain.ApplicationRepository;
import com.fit2cloud.devops.common.model.FileTreeNode;
import com.fit2cloud.devops.common.model.Repository;
import com.fit2cloud.devops.common.util.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author wisonic
 * @date 2019.9.5
 */

@Service
public class Nexus3Service {
    private static final String NEXUS3_REST_REPO_LIST = "service/rest/v1/repositories";
    private static final String NEXUS3_REST_REPO_CONTENT_LIST = "service/extdirect";
    @Resource
    private RestTemplate restTemplate;

    /**
     * 获取nexus3仓库列表
     *
     * @param accessId       账号
     * @param accessPassword 密码
     * @param location       nexus3服务器地址，只要根地址，例如 http://localhost:8081/
     * @return 仓库列列表
     */
    public List<Repository> listRepository(String accessId, String accessPassword, String location) {
        if (!location.endsWith("/")) {
            location += "/";
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/json");
        headers.add("Authorization", "Basic " + CommonUtils.getBasicAuth(accessId, accessPassword));
        HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
        String url = location + NEXUS3_REST_REPO_LIST;
        ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
        JSONArray jsonArray = JSON.parseArray(result.getBody());
        List<Repository> repoList = new ArrayList<>();
        Optional.ofNullable(jsonArray).ifPresent(array -> array.forEach(jsonObject -> {
            JSONObject tmp;
            if (jsonObject instanceof Map) {
                tmp = new JSONObject(((Map) jsonObject));
            }else {
                tmp = (JSONObject) jsonObject;
            }
            repoList.add(new Repository(tmp.getString("name"), tmp.getString("url")));
        }));
        return repoList;
    }

    /**
     * @param applicationRepository 应用仓库
     * @return 应用仓库一级子目录
     */
    public FileTreeNode genFileTree(ApplicationRepository applicationRepository) {
        FileTreeNode root = new FileTreeNode("/");
        root.setFolder(true);
        root.setObj("/");
        root.setChildren(getSubNodes(applicationRepository, (String) root.getObj()));
        root.setHasChildren(!root.getChildren().isEmpty());
        return root;
    }

    /**
     * 获取指定仓库节点下的子节点
     *
     * @param applicationRepository 目标仓库
     * @param node                  目标节点
     * @return 目标节点的子节点
     */
    public List<FileTreeNode> getSubNodes(ApplicationRepository applicationRepository, String node) {
        String repo = applicationRepository.getRepository();
        if (repo.endsWith("/")) {
            repo = repo.substring(0, repo.length() - 1);
        }
        String url = StringUtils.substringBefore(repo, "repository/");
        String repoName = repo.substring(repo.lastIndexOf("/") + 1);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/json");
        headers.add("Authorization", "Basic " + CommonUtils.getBasicAuth(applicationRepository.getAccessId()
                , applicationRepository.getAccessPassword()));

        JSONObject body = new JSONObject();
        body.put("action", "coreui_Browse");
        body.put("method", "read");
        body.put("type", "rpc");
        body.put("tid", 666);

        JSONArray dataArray = new JSONArray();
        JSONObject dataArrayObj = new JSONObject();
        dataArrayObj.put("repositoryName", repoName);
        dataArrayObj.put("node", node);
        dataArray.add(dataArrayObj);
        body.put("data", dataArray);

        HttpEntity<Object> requestEntity = new HttpEntity<>(body, headers);
        url += NEXUS3_REST_REPO_CONTENT_LIST;
        ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        JSONArray resultData = JSONObject.parseObject(result.getBody()).getJSONObject("result").getJSONArray("data");
        List<FileTreeNode> nodeList = new ArrayList<>();
        Optional.ofNullable(resultData).ifPresent(array -> array.forEach(jsonObject -> {
            JSONObject tmpNode = (JSONObject) jsonObject;
            if (StringUtils.endsWithAny(tmpNode.getString("id"), ".pom", ".md5", ".sha1", ".xml")) {
                return;
            }
            String type = tmpNode.getString("type");
            FileTreeNode treeNode = new FileTreeNode(tmpNode.getString("text"));
            treeNode.setFolder(StringUtils.equalsAny(type, "folder", "component"));
            treeNode.setObj(tmpNode.getString("id"));
            treeNode.setHasChildren(!tmpNode.getBoolean("leaf") || treeNode.isFolder());
            nodeList.add(treeNode);
        }));
        return nodeList;
    }

    public boolean check(ApplicationRepository applicationRepository) {
        try {
            String location = applicationRepository.getRepository();
            location = location.substring(0, location.indexOf("/repository") + 1);
            listRepository(applicationRepository.getAccessId(), applicationRepository.getAccessPassword(), location);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public String genDownloadURL(ApplicationRepository applicationRepository, String location) {
        String auth = applicationRepository.getAccessId() + ":" + applicationRepository.getAccessPassword() + "@";
        return location.replace("http://", "http://" + auth)
                .replace("https://", "https://" + auth);
    }
}
