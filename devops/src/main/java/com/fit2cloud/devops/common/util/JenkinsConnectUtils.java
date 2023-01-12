package com.fit2cloud.devops.common.util;

import com.fit2cloud.commons.utils.LogUtil;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.client.JenkinsHttpClient;
import com.offbytwo.jenkins.helper.JenkinsVersion;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JenkinsConnectUtils {
    /**
     * Http 客户端工具
     * <p>
     * 如果有些 API 该Jar工具包未提供，可以用此Http客户端操作远程接口，执行命令
     *
     * @return
     */
    public static JenkinsHttpClient getClient(String jenkinsUrl, String username, String password) {
        JenkinsHttpClient jenkinsHttpClient = null;
        try {
            jenkinsHttpClient = new JenkinsHttpClient(new URI(jenkinsUrl), username, password);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return jenkinsHttpClient;
    }

    /**
     * 连接 Jenkins
     *
     * @return
     */
    public static JenkinsServer getJenkinsServer(String jenkinsUrl, String username, String password) {
        JenkinsServer jenkinsServer = null;
        try {
            jenkinsServer = new JenkinsServer(new URI(jenkinsUrl), username, password);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return jenkinsServer;
    }

    public static boolean validateJenkins(String address, String username, String password) {
        try {
            JenkinsServer jenkinsServer = new JenkinsServer(new URI(address), username, password);
            return jenkinsServer.isRunning();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
    }

}
