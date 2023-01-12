package com.fit2cloud.devops.service.oss;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.devops.base.domain.ApplicationRepository;
import com.fit2cloud.devops.common.model.Repository;
import com.fit2cloud.devops.common.util.CommonUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class HarborService {
    private static final String HARBOR_PROJECTS_URL = "api/v2.0/projects/";
    private static SSLContext sslContext;

    static {
        try {
            sslContext = SSLContexts.custom().loadTrustMaterial(null, (x509Certificates, s) -> true).build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取harbor的projects
     *
     * @param accessId       账号
     * @param accessPassword 密码
     * @param location       harbor服务器地址，只要根地址，例如 http://localhost:8081/
     * @return 项目列表
     */
    public List<Repository> listProjects(String accessId, String accessPassword, String location) {
        List<Repository> repoList = new ArrayList<>();
        try (CloseableHttpClient client = HttpClientBuilder.create()
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build()) {
            if (!location.endsWith("/")) {
                location += "/";
            }
            final String finalLocation = location;
            int page = 1;
//            harbor最多一次取100个project
            int pageSize = 100;
            while (true) {
                URI uri = new URIBuilder(finalLocation + HARBOR_PROJECTS_URL)
                        .addParameter("page", String.valueOf(page))
                        .addParameter("page_size", String.valueOf(pageSize))
                        .build();
                HttpGet req = new HttpGet(uri);
                req.addHeader(HttpHeaders.ACCEPT, "application/json");
                req.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + CommonUtils.getBasicAuth(accessId, accessPassword));
                CloseableHttpResponse res = client.execute(req);
                JSONArray projectsArr = JSONObject.parseArray(EntityUtils.toString(res.getEntity()));
                if (CollectionUtils.isEmpty(projectsArr)) {
                    break;
                }
                projectsArr.forEach(project -> {
                    JSONObject projectObject;
                    if (project instanceof Map) {
                        projectObject = new JSONObject(((Map) project));
                    }else {
                        projectObject = (JSONObject) project;
                    }
                    String name = projectObject.getString("name");
                    repoList.add(new Repository(name, finalLocation + HARBOR_PROJECTS_URL + name));
                });
                page++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return repoList;
    }

    public boolean check(ApplicationRepository appRepo) {
        try (CloseableHttpClient client = HttpClientBuilder.create()
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build()) {
            HttpGet req = new HttpGet(appRepo.getRepository() + "/repositories");
            req.addHeader(HttpHeaders.ACCEPT, "application/json");
            req.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + CommonUtils.getBasicAuth(appRepo.getAccessId(), appRepo.getAccessPassword()));
            CloseableHttpResponse res = client.execute(req);
            if (HttpStatus.SC_OK != res.getStatusLine().getStatusCode()) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
