package com.fit2cloud.devops.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.commons.server.base.domain.SystemParameter;
import com.fit2cloud.commons.server.base.domain.TagMapping;
import com.fit2cloud.commons.server.base.domain.TagValue;
import com.fit2cloud.commons.server.base.mapper.SystemParameterMapper;
import com.fit2cloud.commons.server.base.mapper.TagValueMapper;
import com.fit2cloud.commons.server.constants.RoleConstants;
import com.fit2cloud.commons.server.exception.F2CException;
import com.fit2cloud.commons.server.model.ExcelExportRequest;
import com.fit2cloud.commons.server.service.TagMappingService;
import com.fit2cloud.commons.server.utils.SessionUtils;
import com.fit2cloud.commons.utils.ExcelExportUtils;
import com.fit2cloud.commons.utils.LogUtil;
import com.fit2cloud.commons.utils.UUIDUtil;
import com.fit2cloud.devops.base.domain.*;
import com.fit2cloud.devops.base.mapper.*;
import com.fit2cloud.devops.common.consts.JenkinsConstants;
import com.fit2cloud.devops.common.consts.ScopeConstants;
import com.fit2cloud.devops.common.consts.StatusConstants;
import com.fit2cloud.devops.common.util.CommonUtils;
import com.fit2cloud.devops.common.util.ScoreUtils;
import com.fit2cloud.devops.dao.ext.ExtApplicationMapper;
import com.fit2cloud.devops.dto.ApplicationDTO;
import com.fit2cloud.devops.dto.ApplicationRepositorySettingDTO;
import com.fit2cloud.devops.dto.request.ApplicationRequest;
import com.fit2cloud.devops.service.jenkins.DevopsJenkinsJobService;
import com.fit2cloud.devops.service.openapi.XOceanOpenApiService;
import com.fit2cloud.devops.service.openapi.model.Environment;
import com.fit2cloud.devops.service.openapi.model.Product;
import com.fit2cloud.devops.service.openapi.model.TestPlan;
import com.fit2cloud.devops.vo.*;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApplicationService {

    @Resource
    private ApplicationMapper applicationMapper;
    @Resource
    private TagMappingService tagMappingService;
    @Resource
    private ExtApplicationMapper extApplicationMapper;
    @Resource
    private ApplicationVersionMapper applicationVersionMapper;
    @Resource
    private ApplicationRepositorySettingService applicationRepositorySettingService;
    @Resource
    private XOceanOpenApiService xOceanOpenApiService;
    @Resource
    private SystemParameterMapper systemParameterMapper;
    @Resource
    private ApplicationRepositorySettingMapper applicationRepositorySettingMapper;
    @Resource
    private ApplicationRepositoryMapper applicationRepositoryMapper;
    @Resource
    private TagValueMapper tagValueMapper;
    @Resource
    private ApplicationDeploymentMapper applicationDeploymentMapper;
    @Resource
    private ApplicationSceneTestService applicationSceneTestService;
    @Resource
    private DevopsGitService devopsGitService;
    @Resource
    private DevopsApiTestMapper devopsApiTestMapper;
    @Resource
    private DevopsJenkinsJobHistoryMapper devopsJenkinsJobHistoryMapper;
    @Resource
    private DevopsJenkinsJobService jenkinsJobService;

    private static final Pattern GITLAB_PROJECT_PATTERN = Pattern.compile("<projectPath>(.*?)</projectPath>");

    private static final Pattern GITEA_REPOOWNER_PROJECT_PATTERN = Pattern.compile("<repoOwner>(.*?)</repoOwner>");

    private static final Pattern GITEA_REPOSITORY_PROJECT_PATTERN = Pattern.compile("<repository>(.*?)</repository>");

    private static final Pattern GIT_PROJECT_PATTERN = Pattern.compile("<url>(.*?)</url>");

    // 获取git信息
    private ExecutorService executorGitService = new ThreadPoolExecutor(15, 200, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(1024));

    // 执行job
    private ExecutorService executorJobService = new ThreadPoolExecutor(10, 200, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(1024));

    private static final String REPOID_ALL = "ALL";
    // 2022-11-04T03:07:24Z
    private static final String DATE_FORMATE_Z = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    // 2022-09-08T00:54:59.000Z
    private static final String DATE_FORMATE_SSS_Z = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    // "yyyy-MM-dd'T'HH:mm:ssXXX"
    private static final String DATE_FORMATE_XXX = "yyyy-MM-dd'T'HH:mm:ssXXX";
    // 2022-11-16T15:02:22.000+08:00
    private static final String DATE_FORMATE_SSSXXX = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    // 2022-11-01T21:09:24.091+0800
    private static final String DATE_FORMATE_SSSZ = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    private static final List<String> ALL_DATE_FORMATE = Lists.newArrayList(DATE_FORMATE_Z, DATE_FORMATE_SSS_Z,
            DATE_FORMATE_XXX, DATE_FORMATE_SSSXXX, DATE_FORMATE_SSSZ);
    // createTime
    private BiPredicate<Long, Long> datePredicate = (c, k) -> c >= k && c < (k + DateUtils.MILLIS_PER_DAY);
    // 自动化测试平台查询结果缓存
    private Cache<String, String> OPENAPI_RESULT_CACHE = CacheBuilder.newBuilder().expireAfterWrite(12, TimeUnit.HOURS)
            .maximumSize(1024).build();

    public Application saveApplication(ApplicationDTO application) {
        List<ApplicationRepositorySettingDTO> applicationRepositorySettings = application
                .getApplicationRepositorySettings();
        saveCheck(application);
        if (application.getId() != null) {
            applicationMapper.updateByPrimaryKeySelective(application);
        } else {
            application.setId(UUIDUtil.newUUID());
            application.setCreatedTime(System.currentTimeMillis());
            if (SessionUtils.getUser().getParentRoleId().equalsIgnoreCase(RoleConstants.Id.ORGADMIN.name())) {
                application.setOrganizationId(SessionUtils.getUser().getOrganizationId());
                application.setWorkspaceId("ROOT");
                application.setScope(ScopeConstants.ORG);
            } else if (StringUtils.equalsIgnoreCase(RoleConstants.Id.USER.name(),
                    SessionUtils.getUser().getParentRoleId())) {
                application.setOrganizationId(SessionUtils.getUser().getOrganizationId());
                application.setWorkspaceId(SessionUtils.getUser().getWorkspaceId());
                application.setScope(ScopeConstants.WORKSPACE);
            } else {
                application.setOrganizationId("ROOT");
                application.setWorkspaceId("ROOT");
                application.setScope(ScopeConstants.GLOBAL);
            }
            applicationMapper.insert(application);
        }
        applicationRepositorySettingService.save(applicationRepositorySettings, application.getId());

        return application;
    }

    private void saveCheck(Application application) {
        Example applicationExample = new Example(Application.class);
        String parentRoleId = SessionUtils.getUser().getParentRoleId();
        Example.Criteria criteria = applicationExample.createCriteria();
        switch (StringUtils.upperCase(parentRoleId)) {
            case "ADMIN": {
                criteria.andEqualTo("scope", "global");
                break;
            }
            case "ORGADMIN": {
                criteria.andEqualTo("scope", "org");
                break;
            }
            case "USER": {
                criteria.andEqualTo("scope", "workspace");
                break;
            }
            default: {
                break;
            }
        }
        if (application.getId() != null) {
            criteria.andEqualTo("name", application.getName()).andNotEqualTo("id", application.getId());
        } else {
            criteria.andEqualTo("name", application.getName());
        }
        List<Application> applications = applicationMapper.selectByExample(applicationExample);
        if (applications.size() != 0) {
            F2CException.throwException("名称：" + application.getName() + "已存在！");
        }
    }

    public List<ApplicationDTO> selectApplications(Map params) {
        CommonUtils.filterPermission(params);
        List<ApplicationDTO> applicationDTOS = extApplicationMapper.selectApplications(params);
        applicationDTOS.forEach(applicationDTO -> {
            applicationDTO.setApplicationRepositorySettings(
                    extApplicationMapper.selectRepositorySettings(applicationDTO.getId()));
            if (StringUtils.isNotBlank(applicationDTO.getTestProdId())) {
                try {
                    applicationDTO.setProdName(OPENAPI_RESULT_CACHE.get(applicationDTO.getTestProdId(), () -> {
                        xOceanOpenApiService.getProductList()
                                .forEach(e -> OPENAPI_RESULT_CACHE.put(e.getProdId(), e.getName()));
                        return OPENAPI_RESULT_CACHE.getIfPresent(applicationDTO.getTestProdId());
                    }));
                } catch (Exception e) {
                }
                if (StringUtils.isNotBlank(applicationDTO.getTestPlanId())) {
                    try {
                        applicationDTO.setPlanName(OPENAPI_RESULT_CACHE.get(applicationDTO.getTestPlanId(), () -> {
                            xOceanOpenApiService.getTestPlan(applicationDTO.getTestProdId())
                                    .forEach(e -> OPENAPI_RESULT_CACHE.put(e.getPlanId(), e.getName()));
                            return OPENAPI_RESULT_CACHE.getIfPresent(applicationDTO.getTestPlanId());
                        }));
                    } catch (Exception e) {
                    }
                }
            }
        });
        return applicationDTOS;
    }

    public void deleteApplication(String applicationId) throws Exception {
        applicationMapper.deleteByPrimaryKey(applicationId);
        Map<String, String> params = new HashMap<>();
        params.put("resourceId", applicationId);
        List<TagMapping> tagMappings = tagMappingService.selectTagMappings(params);
        tagMappingService.deleteTagMappings(tagMappings);
        ApplicationVersionExample applicationVersionExample = new ApplicationVersionExample();
        applicationVersionExample.createCriteria().andApplicationIdEqualTo(applicationId);
        applicationVersionMapper.deleteByExample(applicationVersionExample);
    }

    public byte[] exportCloudServers(ExcelExportRequest request) throws Exception {
        Map<String, Object> params = request.getParams();
        ApplicationRequest applicationRequest = new ApplicationRequest();
        if (MapUtils.isNotEmpty(params)) {
            BeanUtils.populate(applicationRequest, params);
        }
        CommonUtils.filterPermission(params);
        List<ExcelExportRequest.Column> columns = request.getColumns();
        List<ApplicationDTO> applicationDTOs = extApplicationMapper.selectApplications(params);
        List<List<Object>> data = applicationDTOs.stream().map(application -> new ArrayList<Object>() {
            {
                columns.forEach(column -> {
                    try {
                        add(MethodUtils.invokeMethod(application, "get" + StringUtils.capitalize(column.getKey())));
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                        LogUtil.error("导出 cloud_server excel error: ", ExceptionUtils.getStackTrace(e));
                    }
                });
            }
        }).collect(Collectors.toList());
        return ExcelExportUtils.exportExcelData("应用详情",
                request.getColumns().stream().map(ExcelExportRequest.Column::getValue).collect(Collectors.toList()),
                data);
    }

    public Application queryById(String id) {
        return applicationMapper.selectByPrimaryKey(id);
    }

    public JSONObject checkTestParam(String id) {
        JSONObject data = new JSONObject();
        // 校验host开关
        data.put("checkhost", true);
        SystemParameter systemParameter = systemParameterMapper.selectByPrimaryKey("xocean.openapi.checkhost");
        if (systemParameter != null) {
            data.put("checkhost", Boolean.valueOf(systemParameter.getParamValue()));
        }
        if (StringUtils.isBlank(id)) {
            data.put("flag", false);
            data.put("tip", "未选择应用：" + id);
            return data;
        }
        Application application = applicationMapper.selectByPrimaryKey(id);
        if (application == null) {
            data.put("flag", false);
            data.put("tip", "应用异常，id:" + id);
            return data;
        }
        if (StringUtils.isAnyBlank(application.getTestProdId(), application.getTestPlanId(),
                application.getTestEvn())) {
            data.put("flag", false);
            data.put("tip", "应用" + application.getName() + "未绑定API安全测试产品信息,应用管理 > 应用 > 编辑 完善应用信息");
            return data;
        }
        List<Product> productList = xOceanOpenApiService.getProductList();
        Optional<Product> optionalProduct = productList.stream()
                .filter(e -> StringUtils.equals(application.getTestProdId(), e.getProdId())).findAny();
        if (!optionalProduct.isPresent()) {
            data.put("flag", false);
            data.put("tip", "应用" + application.getName() + "绑定的测试平台产品" + application.getTestProdId() + "已失效");
            return data;
        }

        List<TestPlan> testPlanList = xOceanOpenApiService.getTestPlan(application.getTestProdId());
        Optional<TestPlan> optionalTestPlan = testPlanList.stream()
                .filter(e -> StringUtils.equals(application.getTestPlanId(), e.getPlanId())).findAny();
        if (!optionalTestPlan.isPresent()) {
            data.put("flag", false);
            data.put("tip", "应用" + application.getName() + "绑定的测试平台执行计划" + application.getTestPlanId() + "已失效");
            return data;
        }
        List<Environment> environment = xOceanOpenApiService.getEnvironment(application.getTestProdId());
        Optional<Environment> optionalEnvironment = environment.stream()
                .filter(e -> StringUtils.equals(application.getTestEvn(), e.getName())).findAny();
        if (!optionalEnvironment.isPresent()) {
            data.put("flag", false);
            data.put("tip", "应用" + application.getName() + "绑定的测试平台环境" + application.getTestEvn() + "已失效");
            return data;
        }
        data.put("testHost", optionalEnvironment.get().getProperty() == null ? "unknow host"
                : optionalEnvironment.get().getProperty().getHost());
        data.put("flag", true);
        return data;
    }

    public AppOverall getAppOverall(String id) {
        Application application = this.checkAppId(id);
        AppOverall appOverall = new AppOverall();
        appOverall.setName(application.getName());
        appOverall.setDescription(application.getDescription());
        ApplicationRepositorySettingExample example = new ApplicationRepositorySettingExample();
        example.createCriteria().andApplicationIdEqualTo(id);
        ScoreUtils scoreCal = new ScoreUtils();
        List<ApplicationRepositorySetting> applicationRepositorySettings = applicationRepositorySettingMapper
                .selectByExample(example);
        if (CollectionUtils.isNotEmpty(applicationRepositorySettings)) {
            ApplicationRepositorySetting applicationRepositorySetting = applicationRepositorySettings.get(0);
            ApplicationRepository applicationRepository = applicationRepositoryMapper
                    .selectByPrimaryKey(applicationRepositorySetting.getRepositoryId());
            TagValue tagValue = tagValueMapper.selectByPrimaryKey(applicationRepositorySetting.getEnvId());
            appOverall.setEnv(tagValue.getTagValueAlias());
            appOverall.setRepo(applicationRepository.getType());
        }
        ApplicationVersionExample applicationVersionExample = new ApplicationVersionExample();
        applicationVersionExample.createCriteria().andApplicationIdEqualTo(id);
        List<ApplicationVersion> applicationVersions = applicationVersionMapper
                .selectByExample(applicationVersionExample);
        appOverall.setVersions(0);
        if (CollectionUtils.isNotEmpty(applicationVersions)) {
            appOverall.setVersions(applicationVersions.size());
            Collections.sort(applicationVersions, Comparator.comparing(ApplicationVersion::getCreatedTime).reversed());
            appOverall.setLatestVersion(applicationVersions.get(0).getName());
            Map<String, String> collect = applicationVersions.stream()
                    .collect(Collectors.toMap(ApplicationVersion::getId, ApplicationVersion::getName, (k1, k2) -> k1));

            ApplicationDeploymentExample applicationDeploymentExample = new ApplicationDeploymentExample();
            applicationDeploymentExample.createCriteria().andIdIn(Lists.newArrayList(collect.keySet()))
                    .andStatusEqualTo("success");
            List<ApplicationDeployment> applicationDeployments = applicationDeploymentMapper
                    .selectByExample(applicationDeploymentExample);
            if (CollectionUtils.isNotEmpty(applicationDeployments)) {
                Collections.sort(applicationDeployments,
                        Comparator.comparing(ApplicationDeployment::getCreatedTime).reversed());
                appOverall.setDeployVersion(collect.get(applicationDeployments.get(0).getApplicationVersionId()));
            }
        }
        List<DevopsJenkinsJob> devopsJenkinsJobs = jenkinsJobService.getDevopsJenkinsJobByAppId(id);
        appOverall.setJobs(devopsJenkinsJobs.size());
        appOverall.setScore(scoreCal.getScoreDouble());
        return appOverall;
    }

    public AppCodeInfo getAppCodeInfo(String appId, Integer goPages, Integer pageSize) {
        AppCodeInfo appCodeInfo = new AppCodeInfo();
        List<GitProjectInfo> gitProjects = parseGitInfo(appId);
        LogUtil.info(String.format("通过应用id：%s,获得job代码信息：%s", appId, JSON.toJSONString(gitProjects)));
        List<CodeRepo> repos = new ArrayList<>();
        List<Future<CodeRepo>> reposFuture = new ArrayList<>();
        ScoreUtils scoreCal = new ScoreUtils();
        List<GitProjectInfo> available = gitProjects.stream().filter(e -> checkGitProject(e))
                .collect(Collectors.toList());
        for (GitProjectInfo gitProjectInfo : available) {
            reposFuture.add(executorJobService.submit(() -> getCodeRepo(gitProjectInfo)));
        }
        for (Future<CodeRepo> future : reposFuture) {
            try {
                repos.add(future.get());
            } catch (Exception e) {
                LogUtil.error("getAppCodeInfo error", e);
            }
        }
        Set<String> nameSet = Sets.newHashSet();
        List<CodeRepo> codeRepoList = repos.stream().filter(e -> nameSet.add(e.getName())).collect(Collectors.toList());
        // 其它不可用的代码仓
        // List<CodeRepo> otherCode = gitProjects.stream().filter(e ->
        // nameSet.add(e.getName())).map(e -> {
        // CodeRepo codeRepo = new CodeRepo();
        // codeRepo.setName(e.getName());
        // return codeRepo;
        // }).collect(Collectors.toList());
        // codeRepoList.addAll(otherCode);
        appCodeInfo.setRepoCount(codeRepoList.size());
        scoreCal.addExpactTargetCount(Long.valueOf(codeRepoList.size()), 1L, 100);
        appCodeInfo.setRepos(CommonUtils.page(codeRepoList, goPages, pageSize));
        appCodeInfo.setScore(scoreCal.getScoreDouble());
        return appCodeInfo;
    }

    private CodeRepo getCodeRepo(GitProjectInfo gitProjectInfo) {
        CodeRepo codeRepo = new CodeRepo();
        try {
            // codeRepo.setId("");
            codeRepo.setName(gitProjectInfo.getName());
            Future<Integer> tagFuture = executorGitService.submit(() -> devopsGitService
                    .getTagNum(gitProjectInfo.getHost(), gitProjectInfo.getName(), gitProjectInfo.getGitType()));
            Future<Integer> branchFuture = executorGitService.submit(() -> devopsGitService
                    .getBranchNum(gitProjectInfo.getHost(), gitProjectInfo.getName(), gitProjectInfo.getGitType()));
            Future<Integer> mrFuture = executorGitService.submit(() -> devopsGitService
                    .getMRNum(gitProjectInfo.getHost(), gitProjectInfo.getName(), gitProjectInfo.getGitType()));
            if (gitProjectInfo.getGitType() == DevopsGitService.GitType.GITEA) {
                Future<List<GiteaCommitInfo>> commitFuture = executorGitService.submit(
                        () -> devopsGitService.getGiteaCommits(gitProjectInfo.getHost(), gitProjectInfo.getName()));
                List<GiteaCommitInfo> giteaCommitInfos = commitFuture.get();
                codeRepo.setCommitCount(giteaCommitInfos.size());
                Set<String> committer = giteaCommitInfos.stream().map(GiteaCommitInfo::getAuthor)
                        .collect(Collectors.toSet());
                codeRepo.setCommitterCount(committer.size());
            }
            if (gitProjectInfo.getGitType() == DevopsGitService.GitType.GITLAB) {
                Future<List<GitlabCommitInfo>> commitFuture = executorGitService.submit(
                        () -> devopsGitService.getGitlabCommits(gitProjectInfo.getHost(), gitProjectInfo.getName()));
                List<GitlabCommitInfo> gitlabCommitInfoList = commitFuture.get();
                codeRepo.setCommitCount(gitlabCommitInfoList.size());
                Set<String> committer = gitlabCommitInfoList.stream().map(GitlabCommitInfo::getAuthorName)
                        .collect(Collectors.toSet());
                codeRepo.setCommitterCount(committer.size());
            }
            codeRepo.setBranchCount(branchFuture.get());
            codeRepo.setTagCount(tagFuture.get());
            codeRepo.setMrCount(mrFuture.get());
        } catch (Exception e) {
            LogUtil.error("getAppCodeInfo error", e);
        }
        return codeRepo;
    }

    private boolean checkGitProject(GitProjectInfo gitProject) {
        boolean flag = false;
        try {
            if (StringUtils.isNotBlank(gitProject.getHost()) && devopsGitService.checkGitProject(gitProject.getHost(),
                    gitProject.getName(), gitProject.getGitType())) {
                flag = true;
            }
        } catch (Exception e) {
            LogUtil.info("checkGitProject fail", e.getMessage());
        }
        if (flag) {
            LogUtil.info("GIT配置地址校验 通过，" + JSON.toJSONString(gitProject));
        } else {
            LogUtil.warn("GIT配置地址校验 失败，" + JSON.toJSONString(gitProject));
        }
        return flag;
    }

    public Map<Long, AtomicInteger> groupByCommit(String appId, String repoId, int time) {
        return this.groupByGitInfo(appId, repoId, time, datePredicate, null);
    }

    public Map<Long, AtomicInteger> groupByCommitter(String appId, String repoId, int time) {
        Map<Long, Set<String>> dupKey = Maps.newHashMap();
        return this.groupByGitInfo(appId, repoId, time, datePredicate,
                (k, n) -> dupKey.computeIfAbsent(k, k1 -> Sets.newHashSet()).add(n));
    }

    private Map<Long, AtomicInteger> groupByGitInfo(String appId, String repoId, int time,
            BiPredicate<Long, Long> datePredicate, BiPredicate<Long, String> dupPredicate) {
        List<GitProjectInfo> gitProjects = parseGitInfo(appId);
        Map<Long, AtomicInteger> group = CommonUtils.defaultGroupResult(time);
        try {
            for (GitProjectInfo gitProject : gitProjects) {
                if (!checkGitProject(gitProject)) {
                    continue;
                }
                if (StringUtils.equalsIgnoreCase(repoId, REPOID_ALL)
                        || StringUtils.equals(repoId, gitProject.getName())) {
                    if (gitProject.getGitType() == DevopsGitService.GitType.GITEA) {
                        List<GiteaCommitInfo> giteaCommitInfos = devopsGitService.getGiteaCommits(gitProject.getHost(),
                                gitProject.getName());
                        for (GiteaCommitInfo giteaCommitInfo : giteaCommitInfos) {
                            Long created = tryParseDate(giteaCommitInfo.getCreated()).getTime();
                            group.forEach((k, v) -> {
                                if (datePredicate == null || datePredicate.test(created, k) && (dupPredicate == null
                                        || dupPredicate.test(k, giteaCommitInfo.getAuthor()))) {
                                    v.incrementAndGet();
                                }
                            });
                        }
                    }
                    if (gitProject.getGitType() == DevopsGitService.GitType.GITLAB) {
                        List<GitlabCommitInfo> gitlabCommits = devopsGitService.getGitlabCommits(gitProject.getHost(),
                                gitProject.getName());
                        for (GitlabCommitInfo gitlabCommitInfo : gitlabCommits) {
                            Long created = tryParseDate(gitlabCommitInfo.getCreatedAt()).getTime();
                            group.forEach((k, v) -> {
                                if (datePredicate == null || datePredicate.test(created, k) && (dupPredicate == null
                                        || dupPredicate.test(k, gitlabCommitInfo.getAuthorName()))) {
                                    v.incrementAndGet();
                                }
                            });
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error("groupByGitInfo error", e);
        }
        return group;
    }

    private Date tryParseDate(String date) {
        for (String formate : ALL_DATE_FORMATE) {
            try {
                return DateUtils.parseDate(date, formate);
            } catch (ParseException e) {
            }
        }
        throw new RuntimeException("tryParseDate fail");
    }

    public AppBuildInfo getBuildInfo(String appId, Integer goPages, Integer pageSize) {
        AppBuildInfo appBuildInfo = new AppBuildInfo();
        this.checkAppId(appId);
        List<DevopsJenkinsJob> devopsJenkinsJobs = jenkinsJobService.getDevopsJenkinsJobByAppId(appId);
        appBuildInfo.setJobCount(devopsJenkinsJobs.size());
        ScoreUtils scoreCal = new ScoreUtils();
        List<JobBuildInfo> jobs = new ArrayList<>();
        for (DevopsJenkinsJob devopsJenkinsJob : devopsJenkinsJobs) {
            JobBuildInfo jobBuildInfo = new JobBuildInfo();
            jobs.add(jobBuildInfo);
            jobBuildInfo.setId(devopsJenkinsJob.getId());
            jobBuildInfo.setName(devopsJenkinsJob.getName());
            DevopsJenkinsJobHistoryExample example = new DevopsJenkinsJobHistoryExample();
            example.createCriteria().andJobIdEqualTo(devopsJenkinsJob.getId());
            List<DevopsJenkinsJobHistory> devopsJenkinsJobHistories = devopsJenkinsJobHistoryMapper
                    .selectByExample(example);
            if (CollectionUtils.isNotEmpty(devopsJenkinsJobHistories)) {
                devopsJenkinsJobHistories.sort(Comparator.comparing(DevopsJenkinsJobHistory::getBuildTime).reversed());
                DevopsJenkinsJobHistory devopsJenkinsJobHistory = devopsJenkinsJobHistories.get(0);
                jobBuildInfo.setLatesStatus(devopsJenkinsJobHistory.getBuildStatus());
                jobBuildInfo.setLatesJobSn(devopsJenkinsJobHistory.getName());
                jobBuildInfo.setLatesBuildTime(devopsJenkinsJobHistory.getBuildTime());
                jobBuildInfo.setBuildCount(devopsJenkinsJobHistories.size());
                long count = devopsJenkinsJobHistories.stream().filter(e -> e.getDurationTime() != null)
                        .mapToLong(DevopsJenkinsJobHistory::getDurationTime).sum();
                jobBuildInfo.setBuildTime(count / jobBuildInfo.getBuildCount());
                long failCount = devopsJenkinsJobHistories.stream()
                        .filter(e -> !StringUtils.equalsIgnoreCase(e.getBuildStatus(), "SUCCESS")).count();
                jobBuildInfo.setBuildFailRate((int) (failCount * 100) / jobBuildInfo.getBuildCount());
                scoreCal.addFailRate(jobBuildInfo.getBuildFailRate());
            }

        }
        appBuildInfo.setJobs(CommonUtils.page(jobs, goPages, pageSize));
        appBuildInfo.setScore(scoreCal.getScoreDouble());
        return appBuildInfo;
    }

    public Map<Long, AtomicInteger> groupByBuild(String appId, String repoId, int time, String status) {
        return groupByBuildInfo(appId, repoId, time, status, false);
    }

    public Map<Long, AtomicInteger> groupByBuildTime(String appId, String repoId, int time, String status) {
        return groupByBuildInfo(appId, repoId, time, status, true);
    }

    public Map<Long, AtomicInteger> groupByBuildInfo(String appId, String repoId, int time, String status,
            boolean avgFlag) {
        if (StringUtils.isNotBlank(status)
                && !StringUtils.equalsAnyIgnoreCase(status, StatusConstants.SUCCESS, StatusConstants.FAIL)) {
            F2CException.throwException("参数异常,status:" + status);
        }
        this.checkAppId(appId);
        List<DevopsJenkinsJob> devopsJenkinsJobs = jenkinsJobService.getDevopsJenkinsJobByAppId(appId);
        Map<Long, AtomicInteger> group = CommonUtils.defaultGroupResult(time);
        Map<Long, AtomicInteger> groupTime = CommonUtils.defaultGroupResult(time);
        try {
            for (DevopsJenkinsJob devopsJenkinsJob : devopsJenkinsJobs) {
                if (StringUtils.equalsIgnoreCase(repoId, REPOID_ALL)
                        || StringUtils.equals(repoId, devopsJenkinsJob.getId())) {
                    DevopsJenkinsJobHistoryExample example = new DevopsJenkinsJobHistoryExample();
                    example.createCriteria().andJobIdEqualTo(devopsJenkinsJob.getId());
                    List<DevopsJenkinsJobHistory> devopsJenkinsJobHistories = devopsJenkinsJobHistoryMapper
                            .selectByExample(example);
                    if (CollectionUtils.isNotEmpty(devopsJenkinsJobHistories)) {
                        for (DevopsJenkinsJobHistory devopsJenkinsJobHistory : devopsJenkinsJobHistories) {
                            if (StringUtils.isNotBlank(status)
                                    && StringUtils.equalsIgnoreCase(status, StatusConstants.SUCCESS)
                                    && !StringUtils.equalsIgnoreCase(devopsJenkinsJobHistory.getBuildStatus(),
                                            StatusConstants.SUCCESS)) {
                                continue;
                            }
                            // fail 包含其他非成功的状态
                            if (StringUtils.isNotBlank(status)
                                    && StringUtils.equalsIgnoreCase(status, StatusConstants.FAIL)
                                    && StringUtils.equalsIgnoreCase(devopsJenkinsJobHistory.getBuildStatus(),
                                            StatusConstants.SUCCESS)) {
                                continue;
                            }
                            group.forEach((k, v) -> {
                                Long buildTime = devopsJenkinsJobHistory.getBuildTime();
                                if (buildTime >= k && buildTime < (k + DateUtils.MILLIS_PER_DAY)) {
                                    v.incrementAndGet();
                                    if (devopsJenkinsJobHistory.getDurationTime() != null) {
                                        groupTime.get(k)
                                                .getAndAdd(devopsJenkinsJobHistory.getDurationTime().intValue());
                                    }
                                }
                            });
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error("groupByBuild error", e);
        }
        if (avgFlag) {
            groupTime.replaceAll((k, v) -> {
                int count = group.get(k).get();
                if (count > 0) {
                    v.set(v.get() / count);
                }
                return v;
            });
            return groupTime;
        }
        return group;
    }

    public AppDeployInfo getDeployInfo(String appId, Integer goPages, Integer pageSize) {
        this.checkAppId(appId);
        AppDeployInfo appDeployInfo = new AppDeployInfo();
        ApplicationVersionExample applicationVersionExample = new ApplicationVersionExample();
        applicationVersionExample.createCriteria().andApplicationIdEqualTo(appId);
        List<ApplicationVersion> applicationVersions = applicationVersionMapper
                .selectByExample(applicationVersionExample);
        Map<String, List<ApplicationDeployment>> applicationVersionDeployMap = new HashMap<>();
        List<DeployVersion> versions = new ArrayList<>();
        AtomicLong totalSuccess = new AtomicLong(0L);
        if (CollectionUtils.isNotEmpty(applicationVersions)) {
            Map<String, ApplicationVersion> applicationVersionMap = applicationVersions.stream()
                    .collect(Collectors.toMap(ApplicationVersion::getId, e -> e, (k1, k2) -> k1));
            ApplicationDeploymentExample applicationDeploymentExample = new ApplicationDeploymentExample();
            applicationDeploymentExample.createCriteria()
                    .andApplicationVersionIdIn(Lists.newArrayList(applicationVersionMap.keySet()));
            List<ApplicationDeployment> applicationDeployments = applicationDeploymentMapper
                    .selectByExample(applicationDeploymentExample);
            appDeployInfo.setDeployCount(applicationDeployments.size());
            if (CollectionUtils.isNotEmpty(applicationDeployments)) {
                applicationVersionDeployMap.putAll(applicationDeployments.stream()
                        .collect(Collectors.groupingBy(ApplicationDeployment::getApplicationVersionId)));
            }
            if (MapUtils.isNotEmpty(applicationVersionDeployMap)) {
                applicationVersionDeployMap.forEach((k, v) -> {
                    DeployVersion deployVersion = new DeployVersion();
                    deployVersion.setId(k);
                    deployVersion.setName(applicationVersionMap.get(k).getName());
                    deployVersion.setDeployCount(v.size());
                    long totalTime = 0L, deploySuccessCount = 0L, deployRollbackCount = 0L;
                    for (ApplicationDeployment applicationDeployment : v) {
                        if (StringUtils.equals(applicationDeployment.getStatus(), StatusConstants.SUCCESS)) {
                            deploySuccessCount++;
                        }
                        if (StringUtils.equals(applicationDeployment.getStatus(), StatusConstants.FAIL)) {
                            deployRollbackCount++;
                        }
                        if (applicationDeployment.getStartTime() != null
                                && applicationDeployment.getEndTime() != null) {
                            totalTime += (applicationDeployment.getEndTime() - applicationDeployment.getStartTime());
                        }
                    }
                    totalSuccess.addAndGet(deploySuccessCount);
                    deployVersion.setDeploySuccessCount(deploySuccessCount);
                    deployVersion.setDeployRollbackCount(deployRollbackCount);
                    deployVersion.setDeployAvgTime(totalTime / v.size());
                    versions.add(deployVersion);
                });
            }
        }
        Integer rate = (appDeployInfo.getDeployCount() > 0)
                ? ((int) (totalSuccess.get() * 100) / appDeployInfo.getDeployCount())
                : 0;
        appDeployInfo.setVersion(CommonUtils.page(versions, goPages, pageSize));
        ScoreUtils scoreCal = new ScoreUtils();
        appDeployInfo.setDeploySuccessRate(rate);
        scoreCal.addSuccessRate(rate);
        appDeployInfo.setScore(scoreCal.getScoreDouble());
        return appDeployInfo;
    }

    public AppApiTestInfo getAppTestInfo(String id, Integer goPages, Integer pageSize) {
        AppApiTestInfo appApiTestInfo = new AppApiTestInfo();
        ScoreUtils scoreCal = new ScoreUtils();
        ApplicationVersionExample applicationVersionExample = new ApplicationVersionExample();
        applicationVersionExample.createCriteria().andApplicationIdEqualTo(id);
        List<ApplicationVersion> applicationVersions = applicationVersionMapper
                .selectByExample(applicationVersionExample);
        if (CollectionUtils.isEmpty(applicationVersions)) {
            return appApiTestInfo;
        }

        Map<String, ApplicationVersion> applicationVersionMaps = applicationVersions.stream()
                .collect(Collectors.toMap(ApplicationVersion::getId, e -> e, (k1, k2) -> k1));
        ApplicationDeploymentExample applicationDeploymentExample = new ApplicationDeploymentExample();
        applicationDeploymentExample.createCriteria()
                .andApplicationVersionIdIn(Lists.newArrayList(applicationVersionMaps.keySet()));
        List<ApplicationDeployment> applicationDeployments = applicationDeploymentMapper
                .selectByExample(applicationDeploymentExample);
        if (CollectionUtils.isEmpty(applicationDeployments)) {
            return appApiTestInfo;
        }

        Map<String, ApplicationDeployment> applicationDeploymentMaps = applicationDeployments.stream()
                .collect(Collectors.toMap(ApplicationDeployment::getId, e -> e, (k1, k2) -> k1));
        Example exampleApiTest = new Example(DevopsApiTest.class);
        exampleApiTest.createCriteria().orIn("deployId", Lists.newArrayList(applicationDeploymentMaps.keySet()))
                .andEqualTo("result", "success");
        List<DevopsApiTest> apiTestLists = devopsApiTestMapper.selectByExample(exampleApiTest);
        if (CollectionUtils.isEmpty(apiTestLists)) {
            return appApiTestInfo;
        }
        Collections.sort(apiTestLists, Comparator.comparing(DevopsApiTest::getStartTime).reversed());
        appApiTestInfo.setTestCount(apiTestLists.size());
        DevopsApiTest latestApiTest = apiTestLists.get(0);
        Integer allRate = 0;
        if (latestApiTest.getTotalCount() > 0L) {
            Long tmp = (latestApiTest.getUntestedCount() + latestApiTest.getPassCount()) * 100
                    / latestApiTest.getTotalCount();
            allRate = tmp.intValue();
        }
        appApiTestInfo.setDeployAvgSuccessRate(allRate);
        List<AppVersionTest> versionTestList = new ArrayList<>();
        Integer avgRate = 0;
        for (DevopsApiTest test : apiTestLists) {
            if (!applicationDeploymentMaps.containsKey(test.getDeployId())) {
                continue;
            }
            ApplicationDeployment deploy = applicationDeploymentMaps.get(test.getDeployId());
            if (!applicationVersionMaps.containsKey(deploy.getApplicationVersionId())) {
                continue;
            }
            ApplicationVersion version = applicationVersionMaps.get(deploy.getApplicationVersionId());
            AppVersionTest appVersionTest = new AppVersionTest();
            appVersionTest.setId(version.getId());
            appVersionTest.setName(version.getName());
            appVersionTest.setTestCount(test.getTotalCount().intValue());
            try {
                Long spendTime = (test.getEndTime() > test.getStartTime()) ? (test.getEndTime() - test.getStartTime())
                        : 0L;
                appVersionTest.setTestAvgTime(spendTime);
            } catch (Exception e) {
                appVersionTest.setTestAvgTime(0);
            }
            appVersionTest.setTestSuccessRate(0);
            if (test.getTotalCount() > 0L) {
                Long rateTmp = (test.getUntestedCount() + test.getPassCount()) * 100 / test.getTotalCount();
                appVersionTest.setTestSuccessRate(rateTmp.intValue());
                avgRate += rateTmp.intValue();
            }
            versionTestList.add(appVersionTest);
        }
        avgRate = (apiTestLists.size() > 0) ? (avgRate / apiTestLists.size()) : 0;
        scoreCal.addSuccessRate(avgRate);
        appApiTestInfo.setVersion(CommonUtils.page(versionTestList, goPages, pageSize));
        appApiTestInfo.setScore(scoreCal.getScoreDouble());
        return appApiTestInfo;
    }

    private Application checkAppId(String id) {
        Application application = applicationMapper.selectByPrimaryKey(id);
        if (application == null) {
            F2CException.throwException("应用不存在，id: " + id);
        }
        return application;
    }

    private List<GitProjectInfo> parseGitInfo(String appId) {
        List<DevopsJenkinsJob> devopsJenkinsJobs = jenkinsJobService.getDevopsJenkinsJobByAppId(appId);
        List<GitProjectInfo> gitProject = new ArrayList<>();
        String giteaUrl = getNotNullValue("gitea.host");
        String gitlabUrl = getNotNullValue("gitlab.host");
        Set<String> nameSet = Sets.newHashSet();
        for (DevopsJenkinsJob devopsJenkinsJob : devopsJenkinsJobs) {
            try {
                if (StringUtils.isBlank(devopsJenkinsJob.getJobXml())
                        || StringUtils.isNotBlank(devopsJenkinsJob.getParentId())) {
                    continue;
                }
                if (StringUtils.equals(devopsJenkinsJob.getType(), JenkinsConstants.JOB_TYPE_MULTIBRANCH)) {
                    if (devopsJenkinsJob.getJobXml().contains("GitLabSCMSource")) {
                        Matcher matcher = GITLAB_PROJECT_PATTERN.matcher(devopsJenkinsJob.getJobXml());
                        String project = "";
                        if (matcher.find()) {
                            project = matcher.group(1);
                        }
                        if (nameSet.add(project)) {
                            gitProject.add(new GitProjectInfo(devopsJenkinsJob.getId(), gitlabUrl, project,
                                    DevopsGitService.GitType.GITLAB));
                        }
                    }
                    if (devopsJenkinsJob.getJobXml().contains("GiteaSCMSource")) {
                        Matcher matcher = GITEA_REPOOWNER_PROJECT_PATTERN.matcher(devopsJenkinsJob.getJobXml());
                        String project = "";
                        if (matcher.find()) {
                            String owner = matcher.group(1);
                            project = owner;
                        }
                        matcher = GITEA_REPOSITORY_PROJECT_PATTERN.matcher(devopsJenkinsJob.getJobXml());
                        if (matcher.find()) {
                            String repo = matcher.group(1);
                            project = project + "/" + repo;
                        }
                        if (nameSet.add(project)) {
                            gitProject.add(new GitProjectInfo(devopsJenkinsJob.getId(), giteaUrl, project,
                                    DevopsGitService.GitType.GITEA));
                        }
                    }
                } else {
                    // TODO
                    if (devopsJenkinsJob.getJobXml().contains("UserRemoteConfig")) {
                        Matcher matcher = GIT_PROJECT_PATTERN.matcher(devopsJenkinsJob.getJobXml());
                        String url = "";
                        if (matcher.find()) {
                            url = matcher.group(1);
                        }
                        if (url.startsWith("http")) {
                            String url0 = url.replace("http://", "").replace("https://", "");
                            String[] split = url0.split("/");
                            String project = "";
                            if (split.length > 1) {
                                for (int i = 1; i < split.length; i++) {
                                    project = project + "/" + split[i];
                                }
                            }
                            // 去重后续校验
                            if (nameSet.add(project)) {
                                gitProject.add(new GitProjectInfo(devopsJenkinsJob.getId(), url.replace(project, ""),
                                        project.substring(1), DevopsGitService.GitType.GITEA));
                                gitProject.add(new GitProjectInfo(devopsJenkinsJob.getId(), url.replace(project, ""),
                                        project.substring(1), DevopsGitService.GitType.GITLAB));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LogUtil.error("parseGitInfo error", e);
            }
        }
        return gitProject;
    }

    private String getNotNullValue(String key) {
        SystemParameter systemParameter = systemParameterMapper.selectByPrimaryKey(key);
        if (systemParameter == null) {
            return null;
            // F2CException.throwException(key + "未设置");
        }
        String value = systemParameter.getParamValue();
        if (StringUtils.isBlank(value)) {
            // F2CException.throwException(key + "未设置");
            return null;
        }
        return value;
    }

    public class GitProjectInfo {
        String jobId;
        String host;
        String name;
        DevopsGitService.GitType gitType;

        public GitProjectInfo(String jobId, String host, String name, DevopsGitService.GitType gitType) {
            this.jobId = jobId;
            this.host = host;
            this.name = name;
            this.gitType = gitType;
        }

        public String getJobId() {
            return jobId;
        }

        public String getHost() {
            return host;
        }

        public String getName() {
            return name;
        }

        public DevopsGitService.GitType getGitType() {
            return gitType;
        }
    }
}
