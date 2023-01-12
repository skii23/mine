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
import com.fit2cloud.devops.common.util.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class NexusService {
    private static final String LIST_REPOSITORIES = "service/local/all_repositories";
    private static final String BROWSER_REPOSITORIES = "service/local/repo_groups/";
    @Resource
    private RestTemplate restTemplate;

    public List<Repository> listRepository(String accessId, String accessPassword, String location) {
        if (!location.endsWith("/")) {
            location += "/";
        }
        List<Repository> repositories = new ArrayList<>();
        try {
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.add("Accept", "application/json");
            requestHeaders.add("Authorization", "Basic " + CommonUtils.getBasicAuth(accessId, accessPassword));
            HttpEntity<String> requestEntity = new HttpEntity<>(null, requestHeaders);
            String url = location + LIST_REPOSITORIES;
            ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
            JSONObject o = JSON.parseObject(result.getBody());
            JSONArray array = JSON.parseArray(o.getString("data"));
            for (int i = 0; i < array.size(); i++) {
                JSONObject obj = array.getJSONObject(i);
                repositories.add(new Repository(obj.getString("id"), obj.getString("contentResourceURI")));
            }
        } catch (Exception e) {
            F2CException.throwException("连接nexus服务器错误！");
        }
        return repositories;
    }

    public boolean check(ApplicationRepository applicationRepository) {
        String orgstr = applicationRepository.getRepository();
        String location = orgstr.substring(0, orgstr.indexOf("/content") + 1);
        try {
            listRepository(applicationRepository.getAccessId(), applicationRepository.getAccessPassword(), location);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public String genDownloadURL(ApplicationRepository applicationRepository, String key) {
        String auth = applicationRepository.getAccessId() + ":" + applicationRepository.getAccessPassword() + "@";
        return key.replace("http://", "http://" + auth)
                .replace("https://", "https://" + auth);
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
        List<FileTreeNode> childrens = new ArrayList<>();
        String url = (String) root.getObj();
        Document doc;
        String auth = CommonUtils.getBasicAuth(applicationRepository.getAccessId(), applicationRepository.getAccessPassword());
        if (check(applicationRepository)) {
            doc = Jsoup.connect(url).timeout(10000).header("Authorization", "Basic " + auth).get();
        } else {
            throw new Exception("制品库无效！");
        }
        Elements trs = doc.select("table tr");
        for (int i = 2; i < trs.size(); i++) {
            Element tr = trs.get(i);
            Elements tds = tr.getElementsByTag("td");
            Element aElmt = tds.get(0).child(0);
            String name = aElmt.text();
            if (name.endsWith(".xml") || name.endsWith(".md5") || name.endsWith(".sha1") || name.endsWith(".pom")) {
                continue;
            }
            FileTreeNode treeNode = new FileTreeNode();
            treeNode.setName(name);
            treeNode.setObj(aElmt.attr("href"));
            if (name.endsWith("/")) {
                findArtifact(treeNode, applicationRepository, artifacts);
            } else {
                Artifact artifact = new Artifact();
                artifact.setName(name);
                artifact.setUrl(aElmt.attr("href"));
                artifact.setRepositoryId(applicationRepository.getId());
                artifact.setId(UUIDUtil.newUUID());
                artifacts.add(artifact);
            }
            childrens.add(treeNode);
        }
        if (childrens.size() == 0) {
            childrens = null;
        }
        root.setChildren(childrens);
    }


    public FileTreeNode getTree(ApplicationRepository applicationRepository) {
        FileTreeNode root = new FileTreeNode("/");
        root.setObj("/");
        root.setFolder(true);
        root.setChildren(getSubNodes(applicationRepository,root.getObj().toString()));
        root.setHasChildren(!root.getChildren().isEmpty());
        return root;
    }
    public FileTreeNode getProxyTree(ApplicationRepository applicationRepository) {
        FileTreeNode root = new FileTreeNode("/");
        root.setObj("/");
        root.setFolder(true);
        root.setChildren(getProxySubNodes(applicationRepository,root.getObj().toString()));
        root.setHasChildren(!root.getChildren().isEmpty());
        return root;
    }

    private void genTree(FileTreeNode root, ApplicationRepository applicationRepository)  {
        List<FileTreeNode> childrens = new ArrayList<>();
        String url = (String) root.getObj();
        Document doc;
        if (check(applicationRepository)) {
            try {
                String auth = CommonUtils.getBasicAuth(applicationRepository.getAccessId(), applicationRepository.getAccessPassword());
                doc = Jsoup.connect(url).timeout(10000).header("Authorization", "Basic " + auth).get();
                Elements trs = doc.select("table tr");
                for (int i = 2; i < trs.size(); i++) {
                    Element tr = trs.get(i);
                    Elements tds = tr.getElementsByTag("td");
                    Element aElmt = tds.get(0).child(0);
                    String name = aElmt.text();
                    if (name.endsWith(".xml") || name.endsWith(".md5") || name.endsWith(".sha1") || name.endsWith(".pom")) {
                        continue;
                    }
                    FileTreeNode treeNode = new FileTreeNode();
                    treeNode.setName(name);
                    treeNode.setObj(aElmt.attr("href"));
                    if (name.endsWith("/")) {
                        genTree(treeNode, applicationRepository);
                    }
                    childrens.add(treeNode);
                }
                root.setChildren(childrens);
            } catch (IOException e) {
                F2CException.throwException("制品库无效");
            }
        } else {
            F2CException.throwException("制品库无效");
        }
    }

    public List<FileTreeNode> getSubNodes(ApplicationRepository applicationRepository, String path) {
        String repo = applicationRepository.getRepository();
        String url = repo.replace("content/repositories/", BROWSER_REPOSITORIES) + "/index_content" + path;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/json");
        headers.add("Authorization", "Basic " + CommonUtils.getBasicAuth(applicationRepository.getAccessId()
                , applicationRepository.getAccessPassword()));
        HttpEntity<Object> httpEntity = new HttpEntity<>(null, headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
        JSONArray children = JSONObject.parseObject(responseEntity.getBody()).getJSONObject("data").getJSONArray("children");
        List<FileTreeNode> nodeList = new ArrayList<>();
        Optional.ofNullable(children).ifPresent(array -> array.forEach(jsonObject -> {
            JSONObject tmpNode;
            if (jsonObject instanceof Map) {
                tmpNode = new JSONObject(((Map) jsonObject));
            }else {
                tmpNode = (JSONObject) jsonObject;
            }
            FileTreeNode node = new FileTreeNode(tmpNode.getString("nodeName"));
            node.setFolder(!tmpNode.getBoolean("leaf"));
            node.setHasChildren(node.isFolder());
            node.setObj(tmpNode.getString("path"));
            if (!"G".equals(tmpNode.getString("type"))){
                JSONArray vNodes = tmpNode.getJSONArray("children");
                List<FileTreeNode> vNodeList = new ArrayList<>();
                vNodes.forEach(vNodeItem -> {
                    JSONObject vNodeObj;
                    if (vNodeItem instanceof Map) {
                        vNodeObj = new JSONObject(((Map) vNodeItem));
                    }else {
                        vNodeObj = (JSONObject) vNodeItem;
                    }
                    FileTreeNode vNode = new FileTreeNode(vNodeObj.getString("nodeName"));
                    vNode.setObj(vNodeObj.getString("path"));
                    vNode.setFolder(!vNodeObj.getBoolean("leaf"));
                    vNode.setHasChildren(vNode.isFolder());
                    JSONArray fileNodes = vNodeObj.getJSONArray("children");
                    List<FileTreeNode> fileNodeList = new ArrayList<>();
                    fileNodes.forEach(fileNodeItem -> {
                        JSONObject fileNodeObj;
                        if (fileNodeItem instanceof Map) {
                            fileNodeObj = new JSONObject(((Map) fileNodeItem));
                        }else {
                            fileNodeObj = (JSONObject) fileNodeItem;
                        }
                        if (StringUtils.endsWithAny(fileNodeObj.getString("extension"),"pom","md5","sha1","xml")){
                            return;
                        }
                        FileTreeNode fileNode = new FileTreeNode(fileNodeObj.getString("nodeName"));
                        fileNode.setFolder(!fileNodeObj.getBoolean("leaf"));
                        fileNode.setObj(fileNodeObj.getString("path"));
                        fileNode.setHasChildren(fileNode.isFolder());
                        fileNodeList.add(fileNode);
                    });
                    vNode.setChildren(fileNodeList);
                    vNodeList.add(vNode);
                });
                node.setChildren(vNodeList);
            }
            nodeList.add(node);
        }));
        return nodeList;
    }
    public List<FileTreeNode> getProxySubNodes(ApplicationRepository applicationRepository, String path) {
        String repo = applicationRepository.getRepository();
        String url = repo.replace("content/repositories/", "service/local/repositories/") + "/remotebrowser" + path;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/json");
        headers.add("Authorization", "Basic " + CommonUtils.getBasicAuth(applicationRepository.getAccessId()
                , applicationRepository.getAccessPassword()));
        HttpEntity<Object> httpEntity = new HttpEntity<>(null, headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
        JSONArray children = JSONObject.parseObject(responseEntity.getBody()).getJSONArray("data");
        List<FileTreeNode> nodeList = new ArrayList<>();
        Optional.ofNullable(children).ifPresent(array -> array.forEach(jsonObject -> {
            JSONObject tmpNode = (JSONObject) jsonObject;
            FileTreeNode node = new FileTreeNode(tmpNode.getString("text"));
            node.setFolder(!tmpNode.getBoolean("leaf"));
            node.setHasChildren(node.isFolder());
            node.setObj(tmpNode.getString("relativePath"));
            nodeList.add(node);
        }));
        return nodeList;
    }
}
