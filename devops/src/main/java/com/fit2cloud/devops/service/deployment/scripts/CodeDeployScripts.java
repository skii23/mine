package com.fit2cloud.devops.service.deployment.scripts;

import com.fit2cloud.commons.utils.LogUtil;
import com.fit2cloud.devops.common.model.Appspec;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.*;

@Component
public class CodeDeployScripts {

    private String baseInitScript = null;
    private String baseInstallScript = null;
    private String baseCleanScript = null;

    @PostConstruct
    public void initBase() {
        InputStream is1 = null;
        InputStream is2 = null;
        InputStream is3 = null;
        try {
            PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
            if (!patternResolver.getResource("script/initEnv.py").exists()) {
                LogUtil.info("script/initEnv.py not found");
            } else {
                is1 = patternResolver.getResource("script/initEnv.py").getInputStream();
                is2 = patternResolver.getResource("script/installApplication.py").getInputStream();
                is3 = patternResolver.getResource("script/cleanEnv.py").getInputStream();
                baseInitScript = IOUtils.toString(is1);
                baseInstallScript = IOUtils.toString(is2);
                baseCleanScript = IOUtils.toString(is3);
            }
        } catch (Exception e) {
            LogUtil.error("获取内置脚本失败:" + e.getMessage());
        } finally {
            if (is1 != null) {
                IOUtils.closeQuietly(is1);
            }
            if (is2 != null) {
                IOUtils.closeQuietly(is2);
            }
            if (is3 != null) {
                IOUtils.closeQuietly(is3);
            }
        }

    }

    public String getInitScript(String artifactName, String downloadUrl, String os) {
        String path = StringUtils.containsIgnoreCase(os, "windows") ? "C:/Windows/Temp/fit2cloud" : "/tmp/fit2cloud";
        return this.baseInitScript
                .replace("${artifact_name}", artifactName)
                .replace("${download_url}", downloadUrl)
                .replace("${default_path}", path);
    }

    public String getCleanScript(String artifactName, String os) {
        String path = StringUtils.containsIgnoreCase(os, "windows") ? "C:/Windows/Temp/fit2cloud" : "/tmp/fit2cloud";
        return this.baseCleanScript
                .replace("${artifact_name}", artifactName)
                .replace("${default_path}", path);
    }

    public String getInstallScript(String rootPath, Appspec appspec, String os) {
        Map<String, String> pathMap = parseFiles(rootPath,appspec.getFiles());
        String permissions = parsePermissions(appspec.getPermissions());
        return this.baseInstallScript
                .replace("${windows}", StringUtils.containsIgnoreCase(os, "windows") ? "True" : "False")
                .replace("${rootPath}", rootPath)
                .replace("${sources}", pathMap.get("sources"))
                .replace("${destinations}", pathMap.get("destinations"))
                .replace("${permissions}",permissions);
    }


    public String getCheckPythonScript() {
        return "python -V";
    }

    public String getLoadAppspec(String path) {
        return "cat " + path + "/appspec.yml";
    }

    /**
     * 解析appspec.yml的files字段,转换为python的列表
     * @param rootPath 目标主机上面zip包解压后的全路径
     * @param files appspec的files字段
     * @return
     */
    private Map<String,String> parseFiles(String rootPath,List<Map<String,String>> files){
        HashMap<String, String> pathMap = new HashMap<>(2);
        StringBuilder sources = new StringBuilder();
        StringBuilder destinations = new StringBuilder();
        files.forEach(map ->{
            String source = map.get("source");
            String destination = map.get("destination");
            if (!source.startsWith("/")) {
                source = "/" + source;
            }
//            解决window上因文件夹路径最后的\和python转义字符的冲突
            if (source.endsWith("\\")){
                source =  source.substring(0,source.length()-1);
            }
            if (destination.endsWith("\\")){
                destination =  destination.substring(0,destination.length()-1);
            }
//            转换为未转义的字符串
            sources.append("r'")
                    .append(rootPath)
                    .append(source)
                    .append("',");
            destinations.append("r'")
                    .append(destination)
                    .append("',");
        });
//        去除后面的逗号
        sources.deleteCharAt(sources.length() - 1);
        destinations.deleteCharAt(destinations.length() - 1);
        pathMap.put("sources", sources.toString());
        pathMap.put("destinations", destinations.toString());
        return pathMap;
    }

    private String parsePermissions(List<Map<String,String>> permissions) {
        StringJoiner stringJoiner = new StringJoiner(",\n", "[", "]");
        Optional.ofNullable(permissions).ifPresent(items -> items.forEach(map -> {
            stringJoiner.add(new Gson().toJson(map));
        }));
        return stringJoiner.toString();
    }

}
