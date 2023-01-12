package com.fit2cloud.devops.service.openapi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.commons.server.base.domain.SystemParameter;
import com.fit2cloud.commons.server.base.mapper.SystemParameterMapper;
import com.fit2cloud.commons.server.exception.F2CException;
import com.fit2cloud.commons.utils.HttpClientConfig;
import com.fit2cloud.commons.utils.HttpClientUtil;
import com.fit2cloud.commons.utils.LogUtil;
import com.fit2cloud.devops.common.consts.XOceanOpenApiConstants;
import com.fit2cloud.devops.common.util.RetryWhenErrorUtil;
import com.fit2cloud.devops.common.util.SignUtil;
import com.fit2cloud.devops.service.openapi.model.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author caiwzh
 * @date 2022/9/8
 */
@Service
public class XOceanOpenApiService {

    public static final String PAGE_SIZE = "500";

    @Resource
    private SystemParameterMapper systemParameterMapper;

    private String baseUrl;

    private String appId;

    private String appSecret;


    //@PostConstruct
    public void init() {
        baseUrl = getNotNullValue("xocean.openapi.host");
        appId = getNotNullValue("xocean.openapi.appId");
        appSecret = getNotNullValue("xocean.openapi.appSecret");
    }

    /***
     * 自动化测试API平台查询产品列表
     * @return
     */
    public List<Product> getProductList() {
        List<Product> products = Lists.newArrayList();
        this.doPageRequest(XOceanOpenApiConstants.QUERY_PRODUCT_URL, null, e ->
                products.add(e.toJavaObject(Product.class))
        );
        return products;
    }

    /***
     * 自动化测试API平台查询产品下的测试计划列表
     * @return
     */
    public List<TestPlan> getTestPlan(String prodId) {
        List<TestPlan> testPlans = Lists.newArrayList();
        this.doPageRequest(XOceanOpenApiConstants.QUERY_TESTPLAN_URL, ImmutableMap.of("prod_id", prodId), e ->
                testPlans.add(e.toJavaObject(TestPlan.class))
        );
        return testPlans;
    }

    /***
     * 自动化测试API平台查询产品下的环境列表
     * @return
     */
    public List<Environment> getEnvironment(String prodId) {
        List<Environment> environments = Lists.newArrayList();
        this.doPageRequest(XOceanOpenApiConstants.QUERY_ENVIRONMENT_URL, ImmutableMap.of("prod_id", prodId), e ->
                environments.add(e.toJavaObject(Environment.class))
        );
        return environments;
    }

    /**
     * 自动化测试API平台发起执行计划的请求
     *
     * @param runRequest
     * @return
     */
    public JSONObject sceneRun(SceneRunRequest runRequest) {
        return this.doReqeust(XOceanOpenApiConstants.SCENE_RUN_URL, ImmutableMap.of("input", JSON.toJSONString(runRequest)), e -> e);
    }

    /**
     * 自动化测试API平台根据执行Id查询执行结果
     *
     * @param runId
     * @return
     */
    public SceneRunResult getSceneRunResult(String runId) {
        return this.doReqeust(XOceanOpenApiConstants.RUN_RESULT_URL, ImmutableMap.of("runId", runId), e -> e.toJavaObject(SceneRunResult.class));
    }

    public void stop(JSONObject param) {
        try {
            this.doReqeust(XOceanOpenApiConstants.STOP_RUN_URL, ImmutableMap.of("input", param.toString()), e -> e);
        } catch (Exception e) {
            LogUtil.warn("XOceanOpenApiService stop error", e);
        }
    }

    /**
     * 自动化测试API平台查询执?计划最后?次执?报告
     *
     * @param productId
     * @param planId
     * @return
     */
    public LastReport getLastReport(String productId, String planId) {
        JSONObject param = new JSONObject(ImmutableMap.of("productId", productId, "planId", planId));
        return this.doReqeust(XOceanOpenApiConstants.RUN_LASTREPORT_URL, ImmutableMap.of("input", param.toJSONString()), e -> e.toJavaObject(LastReport.class));
    }

    private <T> T doReqeust(String url, Map<String, String> param, Function<JSONObject, T> function) {
        init();
        String response = null;
        try {
            response = RetryWhenErrorUtil.execute(3, 1, () -> {
                String res = "";
                try {
                    res = HttpClientUtil.post(UriComponentsBuilder.fromHttpUrl(baseUrl).path(url).toUriString(), param, this.buildConfig());
                } catch (Exception e) {
                    Throwable cause = e.getCause();
                    if (cause != null && cause instanceof SocketTimeoutException) {
                        throw new RuntimeException(cause.getMessage());
                    }
                }
                if (StringUtils.isNotBlank(res) && res.contains("504 Gateway Time-out")) {
                    throw new RuntimeException("504 Gateway Time-out");
                }
                return res;
            });
        } catch (Exception e) {
            F2CException.throwException("xocean.openapi接口失败：" + e.getMessage() + ",url: " + url + ",param: " + param);
        }
        JSONObject responseData = JSON.parseObject(response);
        String resCode = responseData.getJSONObject("resphead").getString("respcode");
        if (!StringUtils.equals(resCode, "0000")) {
            F2CException.throwException("xocean.openapi接口状态异常：" + resCode + ",url: " + url + ",param: " + param);
        }
        JSONObject respbody = responseData.getJSONObject("respbody");
        if (!respbody.getBooleanValue("success")) {
            F2CException.throwException("xocean.openapi接口失败：" + respbody.getString("message") + ",url: " + url + ",param: " + param);
        }
        return function.apply(respbody.getJSONObject("object"));
    }

    private void doPageRequest(String url, Map<String, String> param, Consumer<JSONObject> consumer) {
        init();
        int page = 1;
        for (; ; page++) {
            try {
                Map<String, String> body = Maps.newHashMap();
                body.put("page", String.valueOf(page));
                body.put("size", PAGE_SIZE);
                if (MapUtils.isNotEmpty(param)) {
                    body.putAll(param);
                }
                String response = HttpClientUtil.post(UriComponentsBuilder.fromHttpUrl(baseUrl).path(url).toUriString(), body, this.buildConfig());
                JSONObject responseData = JSON.parseObject(response);
                String resCode = responseData.getJSONObject("resphead").getString("respcode");
                if (!StringUtils.equals(resCode, "0000")) {
                    F2CException.throwException("xocean.openapi接口状态异常：" + resCode + ",url: " + url + ",param: " + param);
                }
                JSONArray array = responseData.getJSONObject("respbody").getJSONArray("list");
                if (CollectionUtils.isEmpty(array)) {
                    break;
                }
                for (int i = 0; i < array.size(); i++) {
                    consumer.accept(array.getJSONObject(i));
                }
            } catch (Exception e) {
                F2CException.throwException(e);
            }
        }
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

    private HttpClientConfig buildConfig() {
        HttpClientConfig httpClientConfig = new HttpClientConfig();
        SignUtil.buildHeadMap(appId, appSecret).forEach(httpClientConfig::addHeader);
        return httpClientConfig;
    }
}
