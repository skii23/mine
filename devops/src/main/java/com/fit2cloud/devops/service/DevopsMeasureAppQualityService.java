package com.fit2cloud.devops.service;

import com.fit2cloud.devops.base.mapper.DevopsJenkinsJobMapper;
import com.fit2cloud.devops.base.mapper.DevopsSonarqubeMapper;
import com.fit2cloud.devops.service.jenkins.DevopsJenkinsJobHistoryService;
import com.fit2cloud.devops.vo.AppJobsQualityMetricsVO;
import com.fit2cloud.devops.vo.AppJobsQualityVO;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.cache.CacheLoader;
import com.offbytwo.jenkins.model.TestReport;

import lombok.NonNull;
import tk.mybatis.mapper.entity.Example;

import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fit2cloud.devops.service.jenkins.DevopsJenkinsJobService;
import com.fit2cloud.devops.service.model.SonarqubeMetrics;
import com.fit2cloud.commons.utils.LogUtil;
import com.fit2cloud.devops.base.domain.DevopsJenkinsJob;
import com.fit2cloud.devops.base.domain.DevopsSonarqube;
import com.fit2cloud.devops.common.model.RetValues;
import com.fit2cloud.devops.common.util.AsyncTaskUtil;
import com.fit2cloud.devops.common.util.CommonUtils;
import com.fit2cloud.devops.common.util.RetUtils;
import com.fit2cloud.devops.common.util.ScoreUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Service
@Transactional(rollbackFor = Exception.class)
public class DevopsMeasureAppQualityService {
    @Resource
    private DevopsJenkinsJobService jenkinsJobService;

    @Resource
    private DevopsJenkinsJobHistoryService jenkinsJobHistoryService;

    @Resource
    private DevopsSonarqubeService sonarqubeService;

    @Resource
    DevopsJenkinsJobMapper devopsJenkinsJobMapper;

    @Resource
    private DevopsSonarqubeMapper sonarqubeMapper;

    private final static long MAX_CHACHE_SIZE = 1024;

    private final static int MAX_TASK_RUN_SIZE = 16;

    private final static int CACHE_REFLASH_TIME_M = 3; // 缓存刷新

    private final static int CACHE_EXPIRED_TIME_D = 2; // 缓存过期时间

    private final static int REFRESH_PROABILITY = 10;

    private LoadingCache<String, AppJobsQualityMetricsVO> JobsCache = CacheBuilder.newBuilder()
            .refreshAfterWrite(CACHE_REFLASH_TIME_M, TimeUnit.MINUTES)
            .expireAfterWrite(CACHE_EXPIRED_TIME_D, TimeUnit.DAYS)
            .maximumSize(MAX_CHACHE_SIZE)
            .initialCapacity(30)
            .build(new QualityRefresh());

    public RetValues<AppJobsQualityVO> getJobsQualityMetrics(String appId, Integer goPages, Integer pageSize) {
        AppJobsQualityVO quality = getJobsQualityMetricsSimple(appId);
        if (quality == null) {
            return RetUtils.error(String.format("获取jobs为空,appId= %s", appId), quality);
        }
        return RetUtils.success(quality);
    }

    public AppJobsQualityVO getJobsQualityMetricsSimple(String appId) {
        List<DevopsJenkinsJob> jobs = jenkinsJobService.getDevopsJenkinsJobByAppId(appId);
        if (jobs == null) {
            return null;
        }
        List<AppJobsQualityMetricsVO> qualityMetrics = getJobsQualityListAsyncTask(jobs);
        if (qualityMetrics == null || qualityMetrics.isEmpty()) {
            return null;
        }
        AppJobsQualityVO quality = new AppJobsQualityVO();
        quality.setJobCount(jobs.size());
        quality.setMetrics(qualityMetrics);
        quality.setScore(getScore(qualityMetrics));
        return quality;
    }

    public AppJobsQualityMetricsVO getQualityData(DevopsJenkinsJob job) {
        AppJobsQualityMetricsVO metrics = new AppJobsQualityMetricsVO();
        if (job == null) {
            return metrics;
        }
        metrics.setId(job.getId());
        metrics.setName(job.getName());
        try {
            TestReport testReport = jenkinsJobHistoryService.getJenkinsJobTestReport(job);
            if (testReport != null) {
                metrics.setUniTestCaseCount(testReport.getTotalCount());
                metrics.setUniTestFailCount(testReport.getFailCount());
                Integer failRate = (testReport.getFailCount() * 100) / (testReport.getTotalCount() + 1);
                failRate = (failRate > 100) ? 100 : failRate;
                metrics.setUniTestSuccessRate(100 - failRate);
                metrics.setUniTestFailRate(failRate);
            }
        } catch (Exception e) {
            LogUtil.warn(String.format("Job[%s] getJenkinsJobTestReport Error:%s", job.getId(), e.getMessage()));
        }
        try {
            Example sonarExample = new Example(DevopsSonarqube.class);
            sonarExample.createCriteria().andEqualTo("projectKey", job.getName());
            sonarExample.orderBy("createTime").desc();
            RowBounds rowBounds = new RowBounds(0, 1);
            List<DevopsSonarqube> sonarLists = sonarqubeMapper.selectByExampleAndRowBounds(sonarExample, rowBounds);
            if (sonarLists != null) {
                DevopsSonarqube sonar = sonarLists.get(0);
                metrics.setBugs(sonar.getBugs());
                metrics.setDebt(sonar.getDebt());
                metrics.setUniTestCoverage(Float.valueOf(sonar.getTestCoverage()*100).longValue());
                metrics.setVulnerabilities(sonar.getVulnerabilities());
                metrics.setCoverageCodeLine(sonar.getTestLine());
                metrics.setDuplicatedRate(Float.valueOf(sonar.getDuplicatedRate()*100).longValue());
                metrics.setIssue(sonar.getIssues());
                metrics.setOpenIssue(sonar.getOpenIssue());
                metrics.setConfiredIssue(sonar.getConfiredIssue());
                metrics.setFalsePositionIssue(sonar.getFalsePositionIssue());
                metrics.setNewCodeLines(sonar.getNewLines());
                metrics.setNewCoverageCodeLine(sonar.getNewTestLine());
                metrics.setNewUniTestCoverage(Float.valueOf(sonar.getNewTestCoverage()*100).longValue());
            } else {
                metrics = getMetricsByService(job, metrics);
            }
        } catch (Exception e) {
            LogUtil.warn(String.format("Job[%s] getSonarqubeMertics Error:%s", job.getId(), e.getMessage()));
        }

        return metrics;
    }

    private AppJobsQualityMetricsVO getMetricsByService(DevopsJenkinsJob job, AppJobsQualityMetricsVO metrics) {
        SonarqubeMetrics sonarqube = sonarqubeService.getSonarqubeMertics(job, false);
        if (sonarqube != null) {
            metrics.setBugs(sonarqube.getBugs());
            metrics.setDebt(sonarqube.getDebt());
            metrics.setUniTestCoverage(sonarqube.getTestCoverage());
            metrics.setVulnerabilities(sonarqube.getVulnerabilities());
            metrics.setCoverageCodeLine(sonarqube.getCoverageCodeLine());
            metrics.setDuplicatedRate(sonarqube.getDuplicatedRate());
            metrics.setIssue(sonarqube.getIssue());
            metrics.setOpenIssue(sonarqube.getOpenIssue());
            metrics.setConfiredIssue(sonarqube.getConfiredIssue());
            metrics.setFalsePositionIssue(sonarqube.getFalsePositionIssue());
        }
        sonarqube = sonarqubeService.getSonarqubeMertics(job, true);
        if (sonarqube != null) {
            metrics.setNewUniTestCoverage(sonarqube.getTestCoverage());
            metrics.setNewCodeLines(sonarqube.getCodeLines());
            metrics.setNewCoverageCodeLine(sonarqube.getCoverageCodeLine());
        }

        return metrics;
    }
    private double getScore(List<AppJobsQualityMetricsVO> qualityMetrics) {
        ScoreUtils cal = new ScoreUtils();
        for (AppJobsQualityMetricsVO m : qualityMetrics) {
            try {
                cal.addSuccessRate(m.getUniTestSuccessRate());
                cal.addFailRate(m.getDuplicatedRate().intValue());
                cal.addFailRate(m.getUniTestFailRate());
                cal.addWarnTargetCount(m.getBugs(), 0L, 5);
                cal.addWarnTargetCount(m.getDebt(), 0L, 5);
                cal.addExpactTargetCount(m.getUniTestCaseCount().longValue(), 10L, 5);
            } catch (Exception e) {
                LogUtil.error(e.getMessage());
            }
        }
        return (double) cal.getScore();
    }

    private List<AppJobsQualityMetricsVO> getJobsQualityListAsyncTask(List<DevopsJenkinsJob> jobs) {
        List<AppJobsQualityMetricsVO> rspList = new ArrayList<>();
        List<Callable<AppJobsQualityMetricsVO>> tasks = new ArrayList<>();
        jobs.forEach(job -> {
            AppJobsQualityMetricsVO metrics = JobsCache.getIfPresent(job.getId());
            if (metrics != null) {
                rspList.add(metrics);
                // CommonUtils.refreshCacheProbability(JobsCache, job.getId(),
                // REFRESH_PROABILITY);// 避免缓存批量更新，概率性刷新，错开更新时间；
            } else {
                Callable<AppJobsQualityMetricsVO> call = new QualityAsyncTask(job);
                tasks.add(call);
            }
        });

        List<AppJobsQualityMetricsVO> newQualities = AsyncTaskUtil.MultiTaskRunSlice(tasks, MAX_TASK_RUN_SIZE);
        if (newQualities != null) {
            newQualities.forEach(metrics -> {
                rspList.add(metrics);
                JobsCache.put(metrics.getId(), metrics);
            });
        }
        return rspList;
    }

    private class QualityAsyncTask implements Callable<AppJobsQualityMetricsVO> {
        private final DevopsJenkinsJob job;

        public QualityAsyncTask(DevopsJenkinsJob job) {
            this.job = job;
        }

        @Override
        public AppJobsQualityMetricsVO call() throws Exception {
            try {
                return getQualityData(job);
            } catch (Exception e) {
                throw new Exception(
                        String.format("Job[%s:%s] get quality Error:%s", job.getId(), job.getName(), e.getMessage()));
            }
        }
    }

    private class QualityRefresh extends CacheLoader<String, AppJobsQualityMetricsVO> {
        @Override
        public AppJobsQualityMetricsVO load(String key) throws Exception {
            return getVlaue(key);
        }

        @Override
        public ListenableFuture<AppJobsQualityMetricsVO> reload(String key, AppJobsQualityMetricsVO oldValue)
                throws Exception {
            LogUtil.warn("Reload Quality Metrics: " + key);
            AppJobsQualityMetricsVO newValue = getVlaue(key);
            if (newValue == null) {
                return super.reload(key, oldValue);
            }
            return super.reload(key, getVlaue(key));
        }

        public AppJobsQualityMetricsVO getVlaue(String key) throws Exception {
            try {
                return getQualityData(devopsJenkinsJobMapper.selectByPrimaryKey(key));
            } catch (Exception e) {
                LogUtil.error(String.format("Job[%s] reload quality Error:%s", key, e.getMessage()));
                return null;
            }
        }
    }
}
