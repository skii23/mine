package com.fit2cloud.devops.service.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.ListObjectsRequest;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import com.fit2cloud.commons.utils.UUIDUtil;
import com.fit2cloud.devops.base.domain.ApplicationRepository;
import com.fit2cloud.devops.common.model.Artifact;
import com.fit2cloud.devops.common.model.FileTreeNode;
import com.fit2cloud.devops.common.model.Bucket;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AliyunOSSService {
    private final static String DEFAULE_ENDPOINT = "http://oss-cn-beijing.aliyuncs.com";
    private final static String ALIYUN_ENDPOINT_SUFFIX = ".aliyuncs.com";


    public List<Bucket> getBuckets(String accessKey, String accessKeySecret) {


        OSS ossClient = new OSSClient(DEFAULE_ENDPOINT, accessKey, accessKeySecret);
        List<Bucket> buckets = new ArrayList<>();

        for (com.aliyun.oss.model.Bucket bucket : ossClient.listBuckets()) {
            buckets.add(new Bucket(bucket.getName(), "bucket:" + bucket.getName()));
        }
        return buckets;
    }


    public boolean check(ApplicationRepository applicationRepository) {
        final String accesskey = applicationRepository.getAccessId();
        final String accesskeySecret = applicationRepository.getAccessPassword();
        try {
            OSS ossClient = new OSSClient(DEFAULE_ENDPOINT, accesskey, accesskeySecret);
            ossClient.listBuckets();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public String genDownloadURL(ApplicationRepository applicationRepository, String key) {


        OSS ossClient = new OSSClient(DEFAULE_ENDPOINT, applicationRepository.getAccessId(), applicationRepository.getAccessPassword());
        String bucketName = getBucketName(applicationRepository.getRepository());
        String endpoint = "http://" + ossClient.getBucketLocation(bucketName) + ALIYUN_ENDPOINT_SUFFIX;
        ossClient = new OSSClient(endpoint, applicationRepository.getAccessId(), applicationRepository.getAccessPassword());
        final long late = (long) 60 * 60 * 1000;
        long expiredTime = System.currentTimeMillis() + late;
        return ossClient.generatePresignedUrl(bucketName, key, new Date(expiredTime)).toString();
    }

    public List<Artifact> findArtifactByKey(ApplicationRepository applicationRepository, String key) {
        final String accesskey = applicationRepository.getAccessId();
        final String accesskeySecret = applicationRepository.getAccessPassword();
        OSS ossClient = new OSSClient(DEFAULE_ENDPOINT, accesskey, accesskeySecret);
        String str = applicationRepository.getRepository();
        String bucketName = str.substring(str.indexOf(":") + 1);
        String endpoint = "http://" + ossClient.getBucketLocation(bucketName) + ALIYUN_ENDPOINT_SUFFIX;
        ossClient = new OSSClient(endpoint, accesskey, accesskeySecret);
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest(bucketName).withMarker(key).withMaxKeys(1000);
        ObjectListing objects = ossClient.listObjects(listObjectsRequest);
        List<OSSObjectSummary> objlist = objects.getObjectSummaries();
        List<Artifact> artifacts = new ArrayList<>();

        for (OSSObjectSummary obj : objlist) {
            if (!obj.getKey().endsWith("/")) {
                Artifact artifact = new Artifact();
                artifact.setId(UUIDUtil.newUUID());
                String objKey = obj.getKey();
                artifact.setUrl(objKey);
                artifact.setName(objKey.substring(objKey.lastIndexOf("/") + 1));
                artifact.setRepositoryId(applicationRepository.getId());
                artifacts.add(artifact);
            }
        }
        return artifacts;

    }


    public FileTreeNode genFolderTree(ApplicationRepository applicationRepository) {
        FileTreeNode root = genFileTree(applicationRepository);
        traverseTree(root);
        return root;
    }

    private void traverseTree(FileTreeNode root) {
        if (root == null) {
            return;
        }

        if (CollectionUtils.isNotEmpty(root.getChildren())) {
            Iterator<FileTreeNode> iterator = root.getChildren().iterator();
            while (iterator.hasNext()) {
                FileTreeNode node = iterator.next();
                if (!node.isFolder()) {
                    iterator.remove();
                }
            }
        }
        if (CollectionUtils.isNotEmpty(root.getChildren())) {
            for (FileTreeNode node : root.getChildren()) {
                traverseTree(node);
            }
        }

    }

    public FileTreeNode genFileTree(ApplicationRepository applicationRepository) {

        OSS ossClient = new OSSClient(DEFAULE_ENDPOINT, applicationRepository.getAccessId(), applicationRepository.getAccessPassword());
        FileTreeNode root = new FileTreeNode("/");
        root.setObj("/");
        root.setFolder(true);
        List<FileTreeNode> list = new ArrayList<>();
        List<FileTreeNode> cList = new ArrayList<>();
        String str = applicationRepository.getRepository();
        str = str.substring(str.indexOf(":") + 1);
        String endpoint = "http://" + ossClient.getBucketLocation(str) + ALIYUN_ENDPOINT_SUFFIX;
        ossClient = new OSSClient(endpoint, applicationRepository.getAccessId(), applicationRepository.getAccessPassword());
        String nextMarker = null;
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest(str);
        listObjectsRequest.setMaxKeys(1000);
        ObjectListing objects;

        Map<String, FileTreeNode> folderMap = new HashMap<>();

        do {
            objects = ossClient.listObjects(listObjectsRequest);
            if (objects != null) {
                List<OSSObjectSummary> objlist = objects.getObjectSummaries();
                for (OSSObjectSummary oo : objlist) {
                    String objKey = oo.getKey();
                    String uri = oo.getKey();
                    String[] pathSegs = objKey.split("/");

                    FileTreeNode node = new FileTreeNode();
                    String nodeText = pathSegs[pathSegs.length - 1];
                    node.setName(nodeText);
                    node.setObj(uri);
                    // is folder
                    if (oo.getSize() == 0 && objKey.endsWith("/")) {
                        node.setFolder(true);
                        node.setChildren(new ArrayList<>());
                        folderMap.put(objKey, node);
                    }
                    if (objKey.equals(nodeText)) {
                        list.add(node);
                    } else {
                        String parentKey = objKey.substring(0, objKey.length() - nodeText.length() - 1);
                        if (!parentKey.endsWith("/")) {
                            parentKey += "/";
                        }
                        if (parentKey.equals("/")) {
                            list.add(node);
                            continue;
                        } else {
                            FileTreeNode parentObj = folderMap.get(parentKey);
                            if (parentObj == null) {
                                filledParentNode(parentKey, folderMap, list, node, cList);
                            } else {
                                parentObj.addChild(node);
                            }
                        }
                    }
                }
                nextMarker = objects.getNextMarker();
                listObjectsRequest.setMarker(nextMarker);
            }
        } while (nextMarker != null);
        root.setChildren(list);
        return root;
    }

    private void filledParentNode(String parentKey, Map<String, FileTreeNode> folderMap, List<FileTreeNode> list, FileTreeNode node, List<FileTreeNode> cList) {
        if (parentKey == null || parentKey.trim().length() == 0 || parentKey.equals("/")) {
            list.add(node);
            return;
        }
        FileTreeNode parentObj = new FileTreeNode();
        cList.add(parentObj);
        String[] pathSegs = parentKey.split("/");
        String pNodeText = pathSegs[pathSegs.length - 1];
        parentObj.setName(pNodeText);
        parentObj.setObj(pNodeText + "/");
        parentObj.setFolder(true);
        parentObj.setChildren(new ArrayList<>());
        folderMap.put(parentKey, parentObj);
        parentObj.addChild(node);
        String superParentKey = parentKey.substring(0, parentKey.length() - pNodeText.length() - 1);
        FileTreeNode superParentObj = folderMap.get(superParentKey);
        if (superParentObj != null) {
            superParentObj.addChild(parentObj);
        } else {
            filledParentNode(superParentKey, folderMap, list, parentObj, cList);
        }
    }


    private String getBucketName(String str) {
        return str.substring(str.indexOf(":") + 1);
    }


}
