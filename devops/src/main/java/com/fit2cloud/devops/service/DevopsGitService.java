package com.fit2cloud.devops.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.commons.server.base.domain.SystemParameter;
import com.fit2cloud.commons.server.base.mapper.SystemParameterMapper;
import com.fit2cloud.commons.server.exception.F2CException;
import com.fit2cloud.commons.utils.HttpClientConfig;
import com.fit2cloud.commons.utils.HttpClientUtil;
import com.fit2cloud.commons.utils.LogUtil;
import com.fit2cloud.devops.vo.GiteaCommitInfo;
import com.fit2cloud.devops.vo.GitlabCommitInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author caiwzh
 * @date 2022/11/3
 */
@Service
public class DevopsGitService {
    /***************************gitlab************************************/
    public static final String BASE_GITLAB_URL = "/api/v4/projects";
    public static final String GITLAB_BRANCHS_URL = BASE_GITLAB_URL + "/%s/repository/branches";
    public static final String GITLAB_MRS_URL = BASE_GITLAB_URL + "/%s/merge_requests";
    public static final String GITLAB_COMMITS_URL = BASE_GITLAB_URL + "/%s/repository/commits";
    public static final String GITLAB_TAGS_URL = BASE_GITLAB_URL + "/%s/repository/tags";

    /***************************gitea************************************/
    public static final String BASE_GITEA_URL = "/api/v1";
    public static final String GITEA_BRANCHS_URL = BASE_GITEA_URL + "/repos/%s/branches";
    public static final String GITEA_COMMITS_URL = BASE_GITEA_URL + "/repos/%s/commits";
    public static final String GITEA_TAGS_URL = BASE_GITEA_URL + "/repos/%s/tags";
    public static final String GITLAB_PULLS_URL = BASE_GITEA_URL + "/repos/%s/pulls";
    @Resource
    private SystemParameterMapper systemParameterMapper;

    public List<GitlabCommitInfo> getGitlabCommits(String host, String project) {
        try {
            String url = String.format(GITLAB_COMMITS_URL, param(project));
            String response = HttpClientUtil.get(host + url, buildGitlabConfig());
            List<GitlabCommitInfo> gitlabCommitInfos = JSON.parseArray(response, GitlabCommitInfo.class);
            return gitlabCommitInfos;
        } catch (Exception e) {
            LogUtil.error("getGitlabCommits error", e);
        }
        return Collections.EMPTY_LIST;
    }

    public List<GiteaCommitInfo> getGiteaCommits(String host, String project) {
        List<GiteaCommitInfo> result = new ArrayList<>();
        try {
            String url = String.format(GITEA_COMMITS_URL, project);
            String response = HttpClientUtil.get(host + url, buildGitlabConfig());
            JSONArray objects = JSON.parseArray(response);
            for (int i = 0; i < objects.size(); i++) {
                JSONObject commitInfo = objects.getJSONObject(i);
                GiteaCommitInfo giteaCommitInfo = new GiteaCommitInfo();
                giteaCommitInfo.setUrl(commitInfo.getString("url"));
                giteaCommitInfo.setCreated(commitInfo.getString("created"));
                giteaCommitInfo.setAuthor(commitInfo.getJSONObject("author").getString("email"));
                result.add(giteaCommitInfo);
            }
        } catch (Exception e) {
            LogUtil.error("getGiteaCommits error", e);
        }
        return result;
    }


    public int getBranchNum(String host, String project, GitType gitType) {
        try {
            switch (gitType) {
                case GITEA:
                    return doGiteaRequest(String.format(host + GITEA_BRANCHS_URL, project)).size();
                case GITLAB:
                    return doGitlabRequest(String.format(host + GITLAB_BRANCHS_URL, param(project))).size();
                default:
                    return 0;
            }
        } catch (Exception e) {
            LogUtil.error("DevopsGitService error",e);
        }
        return 0;
    }

    public int getTagNum(String host, String project, GitType gitType) {
        try {
            switch (gitType) {
                case GITEA:
                    return doGiteaRequest(String.format(host + GITEA_TAGS_URL, project)).size();
                case GITLAB:
                    return doGitlabRequest(String.format(host + GITLAB_TAGS_URL, param(project))).size();
                default:
                    return 0;
            }
        } catch (Exception e) {
            LogUtil.error("DevopsGitService error",e);
        }
        return 0;
    }

    public int getMRNum(String host, String project, GitType gitType) {
        try {
            switch (gitType) {
                case GITEA:
                    return doGiteaRequest(String.format(host + GITLAB_PULLS_URL, project)).size();
                case GITLAB:
                    return doGitlabRequest(String.format(host + GITLAB_MRS_URL, param(project))).size();
                default:
                    return 0;
            }
        } catch (Exception e) {
            LogUtil.error("DevopsGitService error",e);
        }
        return 0;
    }

    private String param(String project) {
        try {
            project = URLEncoder.encode(project, "UTF-8");
        } catch (UnsupportedEncodingException e) {
        }
        if (!project.startsWith("/")) {
            project = "/" + project;
        }
        return project;
    }

    private JSONArray doGitlabRequest(String url) {
        return JSON.parseArray(HttpClientUtil.get(url, buildGitlabConfig()));
    }

    private JSONArray doGiteaRequest(String url) {
        return JSON.parseArray(HttpClientUtil.get(url, buildGiteaConfig()));
    }

    public boolean checkGitProject(String host,String project, GitType gitType){
        try {
            switch (gitType) {
                case GITEA:
                    String response = HttpClientUtil.get(String.format(host + GITEA_BRANCHS_URL, project), this.buildGiteaConfig());
                    return !response.contains("errors");
                case GITLAB:
                    return HttpClientUtil.isResourceExists((String.format(host + GITLAB_BRANCHS_URL, param(project))),this.buildGitlabConfig());
                default:
                    return false;
            }
        } catch (Exception e) {
            LogUtil.warn("checkGitProject fail:" + e.getMessage());
        }
        return false;
    }

    public HttpClientConfig buildGitlabConfig() {
        String token = getNotNullValue("gitlab.token");
        HttpClientConfig config = new HttpClientConfig();
        config.addHeader("Content-Type", "application/json");
        config.addHeader("PRIVATE-TOKEN", token);
        return config;
    }

    public HttpClientConfig buildGiteaConfig() {
        String token = getNotNullValue("gitea.token");
        HttpClientConfig config = new HttpClientConfig();
        config.addHeader("Content-Type", "application/json");
        config.addHeader("Authorization", "token " + token);
        return config;
    }

    private String getNotNullValue(String key) {
        SystemParameter systemParameter = systemParameterMapper.selectByPrimaryKey(key);
        if (systemParameter == null) {
            F2CException.throwException(key + "未设置");
        }
        String value = systemParameter.getParamValue();
        if (StringUtils.isBlank(value)) {
            F2CException.throwException(key + "未设置");
        }
        return value;
    }

    public enum GitType {
        GITLAB, GITEA;
    }
}
