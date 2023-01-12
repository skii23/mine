package com.fit2cloud.devops.common.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import com.fit2cloud.commons.utils.LogUtil;
import com.netflix.config.validation.ValidationException;

import lombok.NonNull;


public class AsyncTaskUtil{
    
    public static int MIN_AYSNC_TASK_NUM = 2;

    public static <T> List<T> singleTaskRun(@NonNull List<Callable<T>> callbacks) {
        List<T> results = new ArrayList<T>();
        if (callbacks.size() <= MIN_AYSNC_TASK_NUM) {
            for(Callable<T> callback :callbacks) {
                try {
                    results.add(callback.call());
                } catch (Exception e) {
                    LogUtil.error(String.format("同步执行call异常, error:%s", e.getMessage()));
                }
            }
        }
        return results;
    }

    public static <T> void MultiTaskAysncRun(@NonNull List<Callable<T>> callbacks) {
        try {
            List<ExecutorService> executors = new ArrayList<ExecutorService>();
            List<FutureTask<T>> futureTasks = new ArrayList<FutureTask<T>>();
            for(Callable<T> callback :callbacks) {
                ExecutorService executor = Executors.newCachedThreadPool();
                executors.add(executor);
                FutureTask<T> futureTask = new FutureTask<T>(callback);
                futureTasks.add(futureTask);
                executor.submit(futureTask);
            }
            for (ExecutorService executor : executors) {
                if (!executor.isShutdown()) {
                    executor.shutdown();
                }
            }
        } catch (Exception e) {
            LogUtil.error(String.format("异步处理批量任务异常, error:%s", e.getMessage()));
        }
        return;
    }

    public static <T> List<T> MultiTaskRun(@NonNull List<Callable<T>> callbacks) {
        List<T> results = new ArrayList<T>();
        if (callbacks.size() <= MIN_AYSNC_TASK_NUM) {
            for(Callable<T> callback :callbacks) {
                try {
                    results.add(callback.call());
                } catch (Exception e) {
                    LogUtil.error(String.format("同步执行call异常, error:%s", e.getMessage()));
                }
            }
            return results;
        }
        try {
            List<ExecutorService> executors = new ArrayList<ExecutorService>();
            List<FutureTask<T>> futureTasks = new ArrayList<FutureTask<T>>();
            for(Callable<T> callback :callbacks) {
                ExecutorService executor = Executors.newCachedThreadPool();
                executors.add(executor);
                FutureTask<T> futureTask = new FutureTask<T>(callback);
                futureTasks.add(futureTask);
                executor.submit(futureTask);
            }
            for (FutureTask<T> task : futureTasks) {
                T ret = task.get();
                if (ret != null ) {
                    results.add(ret);
                }
            }
            for (ExecutorService executor : executors) {
                if (!executor.isShutdown()) {
                    executor.shutdown();
                }
            }
        } catch (Exception e) {
            LogUtil.error(String.format("同步多任务处理批量任务异常, error:%s", e.getMessage()));
        }
        return results;
    }
    public static <T> List<T> MultiTaskRunSlice(@NonNull List<Callable<T>> callbacks, int sliceSize) {
        if (sliceSize <= MIN_AYSNC_TASK_NUM) {
            throw new ValidationException("slice size must over 8.");
        }
        List<T> totalList = new ArrayList<>();
        int slice = ((callbacks.size() % sliceSize) > 0)?(callbacks.size()/sliceSize + 1):(callbacks.size()/sliceSize);
        for (int i = 0; i < slice; i++) {
            int start = i * sliceSize;
            int end = ((start + sliceSize) < callbacks.size())?(start + sliceSize):callbacks.size();
            totalList.addAll(MultiTaskRun(callbacks.subList(start, end)));
        }
        return totalList;
    } 
}


