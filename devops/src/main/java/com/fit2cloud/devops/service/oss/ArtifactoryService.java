package com.fit2cloud.devops.service.oss;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.commons.server.exception.F2CException;
import com.fit2cloud.commons.utils.UUIDUtil;
import com.fit2cloud.devops.base.domain.ApplicationRepository;
import com.fit2cloud.devops.common.model.Artifact;
import com.fit2cloud.devops.common.model.FileTreeNode;
import com.fit2cloud.devops.common.model.Repository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class ArtifactoryService {

    public List<Repository> listRepository(String accessId, String password, String location) {
        if (!location.endsWith("/")) {
            location += "/";
        }


        List<Repository> repositories = new ArrayList<>();
        try {

            RestTemplate restTemplate = getClient();
            HttpHeaders requestHeaders = new HttpHeaders();
            String auth = Base64.getEncoder().encodeToString((accessId + ":" + password).getBytes("utf-8"));
            requestHeaders.add("Authorization", "Basic " + auth);
            requestHeaders.add("Accept", "application/json");

            String url = location + "artifactory/api/repositories";
            HttpEntity<String> requestEntity = new HttpEntity<>(null, requestHeaders);
            ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
            JSONArray array = JSON.parseArray(result.getBody());
            for (int i = 0; i < array.size(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String repKey = obj.getString("key");
                repositories.add(new Repository(obj.getString("key"), obj.getString("url")));
            }
        } catch (Exception e) {
            F2CException.throwException("连接Artifactory服务器错误！");
        }
        return repositories;
    }

    public boolean check(ApplicationRepository applicationRepository) {
        String orgstr = applicationRepository.getRepository();
        String location = orgstr.substring(0, orgstr.indexOf("/artifactory"));
        boolean flag = true;
        try {
            listRepository(applicationRepository.getAccessId(), applicationRepository.getAccessPassword(), location);
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }


    public FileTreeNode getTree(ApplicationRepository applicationRepository) {
        FileTreeNode root = new FileTreeNode();
        root.setName("/");
        String repName = applicationRepository.getRepository().trim().substring(applicationRepository.getRepository().lastIndexOf("/") + 1);
        root.setObj(applicationRepository.getRepository().replace(repName, "api/storage/" + repName));
        genTree(applicationRepository.getAccessId(), applicationRepository.getAccessPassword(), root);
        return root;
    }

    public void genTree(String accessId, String password, FileTreeNode root) {
        RestTemplate client = getClient();
        try {
            String auth = Base64.getEncoder().encodeToString((accessId + ":" + password).getBytes("utf-8"));
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.add("Authorization", "Basic " + auth);
            requestHeaders.add("Accept", "application/json");
            HttpEntity<String> requestEntity = new HttpEntity<>(null, requestHeaders);
            ResponseEntity<String> result = client.exchange((String) root.getObj(), HttpMethod.GET, requestEntity, String.class);
            JSONObject o = JSON.parseObject(result.getBody());
            JSONArray childs = o.getJSONArray("children");
            for (int i = 0; i < childs.size(); i++) {
                JSONObject child = childs.getJSONObject(i);
                boolean isFolder = child.getBoolean("folder");
                String key = child.getString("uri");
                if (isFolder) {
                    FileTreeNode node = new FileTreeNode();
                    node.setName(key.replace("/", ""));
                    node.setObj(root.getObj() + key);
                    node.setChildren(new ArrayList<>());
                    root.addChild(node);
                    genTree(accessId, password, node);
                } else {
                    ResponseEntity<String> fileResult = client.exchange(root.getObj() + key, HttpMethod.GET, requestEntity, String.class);
                    JSONObject file = JSON.parseObject(fileResult.getBody());
                    String uri = file.getString("downloadUri");
                    String name = uri.substring(uri.lastIndexOf("/") + 1);
                    FileTreeNode node = new FileTreeNode();
                    node.setName(name);
                    node.setObj(uri);
                    root.addChild(node);
                }
            }

        } catch (Exception e) {
            F2CException.throwException("用户名或密码错误！");
        }
    }

    public String genDownloadURL(ApplicationRepository applicationRepository, String key) {
        String auth = applicationRepository.getAccessId() + ":" + applicationRepository.getAccessPassword() + "@";
        return key.replace("http://", "http://" + auth);
    }


    public List<Artifact> findArtifactByKey(ApplicationRepository applicationRepository, String key) throws Exception {
        List<Artifact> artifacts = new ArrayList<>();
        FileTreeNode root = new FileTreeNode();
        root.setName("/");
        root.setObj(key);
        findArtifact(root, applicationRepository, artifacts);
        return artifacts;
    }

    public void findArtifact(FileTreeNode root, ApplicationRepository applicationRepository, List<Artifact> artifacts) throws Exception {
        RestTemplate client = getClient();
        try {
            String auth = Base64.getEncoder().encodeToString((applicationRepository.getAccessId() + ":" + applicationRepository.getAccessPassword()).getBytes("utf-8"));
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.add("Authorization", "Basic " + auth);
            requestHeaders.add("Accept", "application/json");
            HttpEntity<String> requestEntity = new HttpEntity<>(null, requestHeaders);
            ResponseEntity<String> result = client.exchange((String) root.getObj(), HttpMethod.GET, requestEntity, String.class);
            JSONObject o = JSON.parseObject(result.getBody());
            JSONArray childs = o.getJSONArray("children");
            for (int i = 0; i < childs.size(); i++) {
                JSONObject child = childs.getJSONObject(i);
                boolean isFolder = child.getBoolean("folder");
                String key = child.getString("uri");
                if (isFolder) {
                    FileTreeNode node = new FileTreeNode();
                    node.setName(key.replace("/", ""));
                    node.setObj(root.getObj() + key);
                    node.setChildren(new ArrayList<>());
                    root.addChild(node);
                    findArtifact(node, applicationRepository, artifacts);
                } else {
                    ResponseEntity<String> fileResult = client.exchange(root.getObj() + key, HttpMethod.GET, requestEntity, String.class);
                    JSONObject file = JSON.parseObject(fileResult.getBody());
                    String uri = file.getString("downloadUri");
                    String name = uri.substring(uri.lastIndexOf("/") + 1);
                    System.out.println(name);
                    Artifact artifact = new Artifact();
                    artifact.setName(name);
                    artifact.setUrl(uri);
                    artifact.setRepositoryId(applicationRepository.getId());
                    artifact.setId(UUIDUtil.newUUID());
                    artifacts.add(artifact);
                }
            }

        } catch (Exception e) {
            F2CException.throwException("用户名或密码错误！");
        }
    }


    private RestTemplate getClient() {
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectionRequestTimeout(3000);
        httpRequestFactory.setConnectTimeout(3000);
        httpRequestFactory.setReadTimeout(3000);
        return new RestTemplate(httpRequestFactory);

    }


}
