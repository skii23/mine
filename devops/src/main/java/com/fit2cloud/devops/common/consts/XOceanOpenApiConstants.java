package com.fit2cloud.devops.common.consts;

/**
 * @author caiwzh
 * @date 2022/9/8
 */
public interface XOceanOpenApiConstants {
    /**
     * 自动化测试API平台查询产品列表
     */
    String QUERY_PRODUCT_URL= "/api/v1/xocean/openapi/product/query/list";
    /**
     *自动化测试API平台查询产品下的测试计划列表
     */
    String QUERY_TESTPLAN_URL= "/api/v1/xocean/openapi/product/query/testplan";
    /**
     *自动化测试API平台查询产品下的环境列表
     */
    String QUERY_ENVIRONMENT_URL= "/api/v1/xocean/openapi/environment/query/envlist";

    /**
     *自动化测试API平台发起执行计划的请求
     */
    String SCENE_RUN_URL= "/api/v1/xocean/openapi/authorization/scene/run";

    /**
     *自动化测试API平台根据执行Id查询执行结果
     */
    String RUN_RESULT_URL= "/api/v1/xocean/openapi/authorization/scene/run/result";

    /**
     *自动化测试API平台查询执?计划最后?次执?报告
     */
    String RUN_LASTREPORT_URL= "/api/v1/xocean/openapi/authorization/scene/run/lastReport";

    String STOP_RUN_URL= "/api/v1/xocean/openapi/authorization/scene/stop";
}
