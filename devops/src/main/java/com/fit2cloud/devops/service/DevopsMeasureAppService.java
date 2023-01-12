package com.fit2cloud.devops.service;

import com.fit2cloud.commons.server.constants.RoleConstants;
import com.fit2cloud.commons.server.exception.F2CException;
import com.fit2cloud.commons.server.model.SessionUser;
import com.fit2cloud.commons.server.utils.SessionUtils;
import com.fit2cloud.commons.utils.LogUtil;
import com.fit2cloud.devops.dto.request.ApplicationRequest;
import com.fit2cloud.devops.base.domain.MeasureAppCustomize;
import com.fit2cloud.devops.base.mapper.ApplicationMapper;
import com.fit2cloud.devops.base.mapper.MeasureAppCustomizeMapper;
import com.fit2cloud.devops.base.domain.Application;
import com.fit2cloud.commons.utils.BeanUtils;
import com.fit2cloud.devops.vo.AppApiTestInfo;
import com.fit2cloud.devops.vo.AppBuildInfo;
import com.fit2cloud.devops.vo.AppCodeInfo;
import com.fit2cloud.devops.vo.AppDeployInfo;
import com.fit2cloud.devops.vo.AppJobsQualityMetricsVO;
import com.fit2cloud.devops.vo.AppJobsQualityVO;
import com.fit2cloud.devops.vo.AppMetricsVO;
import com.fit2cloud.devops.vo.CodeRepo;
import com.fit2cloud.devops.vo.JobBuildInfo;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;
import org.apache.commons.lang3.StringUtils;
import com.fit2cloud.devops.common.model.RetValues;
import com.fit2cloud.devops.common.util.AsyncTaskUtil;
import com.fit2cloud.devops.common.util.CommonUtils;
import com.fit2cloud.devops.common.util.RetUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

@Service
@Transactional(rollbackFor = Exception.class)
public class DevopsMeasureAppService {

    @Resource
    private MeasureAppCustomizeMapper measureAppCustomizeMapper;

    @Resource
    DevopsMeasureAppQualityService appQualityService;

    @Resource
    private ApplicationService appService;

    @Resource
    private ApplicationMapper applicationMapper;

    private final static long MAX_CHACHE_SIZE = 1024;

    private final static int MAX_TASK_RUN_SIZE = 16;

    private final static int MAX_QUERY_PAGE_SIZE = 15;

    private final static int CACHE_REFLASH_TIME_M = 3; // 缓存刷新时间

    private final static int CACHE_EXPIRED_TIME_D = 2; // 缓存过期时间

    private LoadingCache<String, AppMetricsVO> APP_METRICS_CACHE = CacheBuilder.newBuilder()
            .refreshAfterWrite(CACHE_REFLASH_TIME_M, TimeUnit.MINUTES)
            .expireAfterWrite(CACHE_EXPIRED_TIME_D, TimeUnit.DAYS)
            .initialCapacity(45)
            .maximumSize(MAX_CHACHE_SIZE)
            .build(new AppAsyncRefresh());

    public RetValues<List<AppMetricsVO>> getList(Integer goPages, Integer pageSize, ApplicationRequest req) {
        return RetUtils.<List<AppMetricsVO>>success(getAppMetricsList(goPages, pageSize, req, false));
    }

    public RetValues<List<AppMetricsVO>> getCustomizeList(Integer goPages, Integer pageSize, ApplicationRequest req) {
        return RetUtils.<List<AppMetricsVO>>success(getAppMetricsList(goPages, pageSize, req, true));
    }

    public RetValues<Integer> getTotal() {
        return RetUtils.<Integer>success(getAppMetricsListSize(null, false));
    }

    public RetValues<Integer> getCustomizeTotal() {
        return RetUtils.<Integer>success(getAppMetricsListSize(null, true));
    }

    public RetValues<?> AddCustomize(String appId) {
        SessionUser user = SessionUtils.getUser();
        if (user == null) {
            return RetUtils.error(String.format("获取用户信息异常."));
        }
        if (!isAppIdValid(appId)) {
            return RetUtils.error(String.format("无效的应用id=%s, 请检查.", appId));
        }
        Example customizeExample = new Example(MeasureAppCustomize.class);
        customizeExample.createCriteria().andEqualTo("appId", appId).andEqualTo("userId", user.getId());
        List<MeasureAppCustomize> customizeList = measureAppCustomizeMapper.selectByExample(customizeExample);
        if (customizeList.isEmpty()) {
            MeasureAppCustomize customize = new MeasureAppCustomize();
            customize.setId(0);
            customize.setAppId(appId);
            customize.setUserId(user.getId());
            customize.setCreateTime(System.currentTimeMillis());
            measureAppCustomizeMapper.insert(customize);
        }
        return RetUtils.success();
    }

    public RetValues<?> RemoveCustomize(String appId) {
        SessionUser user = SessionUtils.getUser();
        if (user == null) {
            return RetUtils.error(String.format("获取用户信息异常."));
        }
        if (!isAppIdValid(appId)) {
            return RetUtils.error(String.format("无效的应用id=%s, 请检查.", appId));
        }
        Example customizeExample = new Example(MeasureAppCustomize.class);
        customizeExample.createCriteria().andEqualTo("appId", appId).andEqualTo("userId", user.getId());
        List<MeasureAppCustomize> customizeList = measureAppCustomizeMapper.selectByExample(customizeExample);
        if (!customizeList.isEmpty()) {
            customizeList.forEach(del -> {
                measureAppCustomizeMapper.deleteByPrimaryKey(del.getId());
            });
        }
        return RetUtils.success();
    }

    public static AppMetricsVO getAppInitMetrics(Application app) {
        AppMetricsVO metrics = new AppMetricsVO();
        metrics.setId(app.getId());
        metrics.setName(app.getName());
        Random rand = new Random(); //
        metrics.setCommitDaily(0L);
        metrics.setBuildAvgPeriod(0L);
        metrics.setBuildFailRate(rand.nextInt(100));
        metrics.setUnitTestCoverageRate(rand.nextInt(100));
        metrics.setUnitTestSuccessRate(rand.nextInt(50) + 50);
        metrics.setDeploySuccesRate(rand.nextInt(50) + 50);
        metrics.setApiTestSuccesRate(rand.nextInt(100));
        metrics.setIsCustomized(false);
        return metrics;
    }

    public AppMetricsVO getAppMetrics(Application app, boolean isCustomize) {

        AppMetricsVO metrics = getAppInitMetrics(app);
        ExecutorService executor = Executors.newCachedThreadPool();

        FutureTask<AppJobsQualityVO> qualityFuture = new FutureTask<>(() -> {
            return appQualityService.getJobsQualityMetricsSimple(app.getId());
        });
        executor.submit(qualityFuture);

        FutureTask<AppCodeInfo> appInfoFuture = new FutureTask<>(() -> {
            return appService.getAppCodeInfo(app.getId(), 1, MAX_QUERY_PAGE_SIZE);
        });
        executor.submit(appInfoFuture);

        FutureTask<AppBuildInfo> buildInfoFuture = new FutureTask<>(() -> {
            return appService.getBuildInfo(app.getId(), 1, MAX_QUERY_PAGE_SIZE);
        });
        executor.submit(buildInfoFuture);

        FutureTask<AppDeployInfo> deployInfoFuture = new FutureTask<>(() -> {
            return appService.getDeployInfo(app.getId(), 1, MAX_QUERY_PAGE_SIZE);
        });
        executor.submit(deployInfoFuture);

        FutureTask<AppApiTestInfo> apiTestInfoFuture = new FutureTask<>(() -> {
            return appService.getAppTestInfo(app.getId(), 1, MAX_QUERY_PAGE_SIZE);
        });
        executor.submit(apiTestInfoFuture);
        executor.shutdown();
        try {
            AppJobsQualityVO quality = qualityFuture.get();
            if (quality != null) {
                Integer testSuccessRate = 0;
                Integer testCoverageRate = 0;
                for (AppJobsQualityMetricsVO jobQuality : quality.getMetrics()) {
                    testCoverageRate += jobQuality.getUniTestCoverage().intValue();
                    testSuccessRate += jobQuality.getUniTestSuccessRate().intValue();
                }
                testCoverageRate = (quality.getMetrics().size() > 0) ? (testCoverageRate / quality.getMetrics().size())
                        : 0;
                testSuccessRate = (quality.getMetrics().size() > 0) ? (testSuccessRate / quality.getMetrics().size())
                        : 0;
                metrics.setUnitTestCoverageRate(testCoverageRate);
                metrics.setUnitTestSuccessRate(testSuccessRate);
            }
        } catch (Exception e) {
            LogUtil.error(
                    String.format("app[%s:%s] get quality Error:%s", app.getId(), app.getName(), e.getMessage()));
        }

        try {
            AppCodeInfo appInfo = appInfoFuture.get();
            Long commitCount = 0L;
            for (CodeRepo code : appInfo.getRepos()) {
                commitCount += code.getCommitCount();
            }
            commitCount = (appInfo.getRepos().size() > 0) ? (commitCount / appInfo.getRepos().size()) : 0;
            metrics.setCommitDaily(commitCount);
        } catch (Exception e) {
            LogUtil.error(
                    String.format("app[%s:%s] get AppCodeInfo Error:%s", app.getId(), app.getName(), e.getMessage()));
        }

        try {
            AppBuildInfo buildInfo = buildInfoFuture.get();
            Integer buildFailRate = 0;
            Long buildTime = 0L;
            for (JobBuildInfo build : buildInfo.getJobs()) {
                buildFailRate += build.getBuildFailRate();
                buildTime += build.getBuildTime();
            }
            buildFailRate = (buildInfo.getJobs().size() > 0) ? (buildFailRate / buildInfo.getJobs().size()) : 0;
            buildTime = (buildInfo.getJobs().size() > 0) ? (buildTime / buildInfo.getJobs().size()) : 0L;
            metrics.setBuildFailRate(buildFailRate);
            metrics.setBuildAvgPeriod(buildTime);
        } catch (Exception e) {
            LogUtil.error(
                    String.format("app[%s:%s] get BuildInfo Error:%s", app.getId(), app.getName(), e.getMessage()));
        }

        try {
            metrics.setDeploySuccesRate(deployInfoFuture.get().getDeploySuccessRate());
        } catch (Exception e) {
            LogUtil.error(
                    String.format("app[%s:%s] get deploy info Error:%s", app.getId(), app.getName(), e.getMessage()));
        }

        try {
            metrics.setApiTestSuccesRate(apiTestInfoFuture.get().getDeployAvgSuccessRate());
        } catch (Exception e) {
            LogUtil.error(
                    String.format("app[%s:%s] get Api test Error:%s", app.getId(), app.getName(), e.getMessage()));
        }

        return metrics;
    }

    private class AppMultiTask implements Callable<AppMetricsVO> {
        private final Application app;
        private final boolean isCustomize;

        public AppMultiTask(Application app, boolean isCustomize) {
            this.app = app;
            this.isCustomize = isCustomize;
        }

        @Override
        public AppMetricsVO call() throws Exception {
            try {
                return getAppMetrics(app, isCustomize);
            } catch (Exception e) {
                throw new Exception(
                        String.format("app[%s:%s] get metrics Error:%s", app.getId(), app.getName(), e.getMessage()));
            }
        }
    }

    private class AppAsyncRefresh extends CacheLoader<String, AppMetricsVO> {
        @Override
        public AppMetricsVO load(String key) throws Exception {
            LogUtil.warn("load app Metrics: " + key);
            AppMetricsVO newValue = getVlaue(key);
            if (newValue == null) {
                LogUtil.error(String.format("app[%s] can't find on database. ", key));
                return new AppMetricsVO();
            }
            return newValue;
        }

        @Override
        public ListenableFuture<AppMetricsVO> reload(String key, AppMetricsVO oldValue) throws Exception {
            LogUtil.warn("Reload app Metrics: " + key);
            AppMetricsVO newValue = getVlaue(key);
            if (newValue == null) {
                super.reload(key, oldValue);
            }
            return super.reload(key, newValue);
        }

        public AppMetricsVO getVlaue(String key) throws Exception {
            try {
                Application app = applicationMapper.selectByPrimaryKey(key);
                if (app == null) {
                    LogUtil.error(String.format("app[%s] can't find on database. ", key));
                    return null;
                }
                return getAppMetrics(app, false);
            } catch (Exception e) {
                LogUtil.error(String.format("app[%s] reload Error:%s", key, e.getMessage()));
                return  null;
            }
        }
    }

    private List<AppMetricsVO> getAppMetricsList(Integer goPages, Integer pageSize, ApplicationRequest req,
            boolean isCustomized) {
        List<AppMetricsVO> rspList = new ArrayList<>();
        List<AppMetricsVO> tmpList = new ArrayList<>();
        try {
            List<Application> appList = getAppListWithWorkspace();
            List<Application> appTmpList = new ArrayList<>();
            if (isCustomized) {
                appList.forEach(app -> {
                    if (getUserCustomizeStatus(app.getId())) {
                        appTmpList.add(app);
                    }
                });
                appList = appTmpList;
            }
            appList = getPageInList(goPages, pageSize, appList);
            List<Callable<AppMetricsVO>> tasks = new ArrayList<>();

            appList.forEach(app -> {
                boolean userCustomize = isCustomized;
                if (!userCustomize) {
                    userCustomize = getUserCustomizeStatus(app.getId());
                }
                AppMetricsVO metrics = APP_METRICS_CACHE.getIfPresent(app.getId());
                if (metrics != null) {
                    metrics.setIsCustomized(userCustomize);
                    rspList.add(metrics);
                } else {
                    Callable<AppMetricsVO> call = new AppMultiTask(app, userCustomize);
                    tasks.add(call);
                    metrics = getAppInitMetrics(app); // 暂时用初始数据，避免阻塞
                    metrics.setIsCustomized(userCustomize);
                    tmpList.add(metrics);
                }
            });
            if (!tasks.isEmpty()) {
                List<AppMetricsVO> newMetrics = asyncGetmetrics(tasks);
                if (newMetrics != null) {
                    rspList.addAll(newMetrics);
                } else {
                    rspList.addAll(tmpList);
                }
            }
        } catch (Exception e) {
            LogUtil.error(String.format("获取应用指标列表异常:%s", e.getMessage()));
        }

        return rspList;
    }

    private List<AppMetricsVO> asyncGetmetrics(List<Callable<AppMetricsVO>> tasks) {
        List<AppMetricsVO> rsp = null;
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        FutureTask<List<AppMetricsVO>> getFuture = new FutureTask<>(() -> {
            LogUtil.warn(String.format("异步获取指标数据,size:" + tasks.size()));
            try {
                List<AppMetricsVO> newAppDatas = AsyncTaskUtil.MultiTaskRunSlice(tasks, MAX_TASK_RUN_SIZE);
                if (newAppDatas != null) {
                    newAppDatas.forEach(metrics -> {
                        APP_METRICS_CACHE.put(metrics.getId(), metrics);
                    });
                }
                LogUtil.warn(String.format("异步后台获取指标结束:" + newAppDatas.size()));
                return newAppDatas;
            } catch (Exception e) {
                LogUtil.error(String.format("异步获取应用指标列表异常:%s", e.getMessage()));
                return null;
            }
        });
        executorService.submit(getFuture);
        try {
            rsp = getFuture.get(1000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            LogUtil.warn(String.format("异步获取应用指标列表超时:%s", e.getMessage()));
        }
        executorService.shutdown(); // 让其自动回收线程池
        return rsp;
    }

    private Integer getAppMetricsListSize(ApplicationRequest req, boolean isCustomized) {
        List<Application> appRawList = getAppListWithWorkspace();
        Integer total = 0;
        if (!isCustomized) {
            return appRawList.size();
        }
        for (Application app : appRawList) {
            if (!getUserCustomizeStatus(app.getId())) {
                continue;
            }
            total++;
        }
        return total;
    }

    private List<Application> getAppListWithWorkspace() {
        List<Application> appList = new ArrayList<>();
        Map<String, Object> params = getUserWorkspace();
        try {
            Example appExample = new Example(Application.class);
            params.forEach((key, value) -> {
                appExample.createCriteria().andEqualTo(key, value);
            });
            return applicationMapper.selectByExample(appExample);
        } catch (Exception e) {
            LogUtil.error("查询应用列表异常:" + mapToString(params) + ", error:" + e.getMessage());
        }
        return appList;
    }

    private boolean isAppIdValid(String appId) {
        return true;
    }

    private boolean getUserCustomizeStatus(String appId) {
        SessionUser user = SessionUtils.getUser();
        if (user == null) {
            LogUtil.error("获取用户信息异常:");
            return false;
        }
        Example customizeExample = new Example(MeasureAppCustomize.class);
        customizeExample.createCriteria().andEqualTo("appId", appId).andEqualTo("userId", user.getId());
        List<MeasureAppCustomize> customizeList = measureAppCustomizeMapper.selectByExample(customizeExample);
        if (customizeList.isEmpty()) {
            return false;
        }
        return true;
    }

    private Map<String, Object> getUserWorkspace() {
        SessionUser user = SessionUtils.getUser();
        Map<String, Object> params = new HashMap<>();
        if (null != user) {
            if (StringUtils.equalsIgnoreCase(user.getParentRoleId(), RoleConstants.Id.ORGADMIN.name())) {
                params.put("organizationId", user.getOrganizationId());
            } else if (StringUtils.equalsIgnoreCase(user.getParentRoleId(), RoleConstants.Id.USER.name())) {
                params.put("workspaceId", user.getWorkspaceId());
            }
        } else {
            LogUtil.error("当前 Session 没有用户");
            return params;
        }
        return params;
    }

    private <T> List<T> getPageInList(Integer goPages, Integer pageSize, List<T> list) {
        int startIndex = (goPages - 1) * pageSize;
        int endIndex = goPages * pageSize;
        int size = list.size();
        if (size < startIndex) {
            return null;
        }
        endIndex = endIndex > size ? size : endIndex;
        return list.subList(startIndex, endIndex);
    }

    private String mapToString(Map<String, Object> map) {
        if (map == null) {
            return "";
        }
        String out = "";
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            out = entry.getKey() + ":" + entry.getValue().toString() + ";";
        }
        return out;
    }
}
