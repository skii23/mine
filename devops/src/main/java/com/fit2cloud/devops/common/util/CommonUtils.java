package com.fit2cloud.devops.common.util;

import com.fit2cloud.commons.server.base.domain.SystemParameter;
import com.fit2cloud.commons.server.constants.RoleConstants;
import com.fit2cloud.commons.server.model.SessionUser;
import com.fit2cloud.commons.server.utils.SessionUtils;
import com.fit2cloud.commons.utils.LogUtil;
import com.fit2cloud.commons.utils.Pager;
import com.google.common.collect.Lists;

import lombok.NonNull;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.LoadingCache;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author shaochuan.wu
 * @date 2019.9.5
 */
public class CommonUtils {
    private CommonUtils() {

    }

    public static String getBasicAuth(String username, String password) {
        return Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
    }


    public static void filterPermission(Map<String, Object> params) {
        SessionUser user = SessionUtils.getUser();
        if (null != user) {
            if (StringUtils.equalsIgnoreCase(user.getParentRoleId(), RoleConstants.Id.ORGADMIN.name())) {
                params.put("organizationId", user.getOrganizationId());
            } else if (StringUtils.equalsIgnoreCase(user.getParentRoleId(), RoleConstants.Id.USER.name())) {
                params.put("workspaceId", user.getWorkspaceId());
            }
        }else {
            LogUtil.debug("当前 Session 没有用户");
        }
    }

    public static Object castValue(String valueStr, Class<?> clazz) {

        if (clazz == boolean.class) {
            return Boolean.valueOf(valueStr);
        }

        if (clazz == int.class) {
            return Integer.valueOf(valueStr);
        }

        return valueStr;
    }

    public static <T> Pager<List<T>> setPage(List<T> list, int pageNum, int pageSize) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }

        int count = list.size();
        int pageCount;

        if (count % pageSize == 0) {
            pageCount = count / pageSize;
        } else {
            pageCount = count / pageSize + 1;
        }

        int fromIndex;
        int toIndex;

        if (pageNum != pageCount) {
            fromIndex = (pageNum - 1) * pageSize;
            toIndex = fromIndex + pageSize;
        } else {
            fromIndex = (pageNum - 1) * pageSize;
            toIndex = count;
        }

        Pager<List<T>> pager = new Pager<>();
        pager.setItemCount(count);
        pager.setListObject(list.subList(fromIndex, toIndex));
        pager.setPageCount(pageCount);

        return pager;
    }

    public static boolean validSysParam(SystemParameter systemParameter) {
        return systemParameter != null && StringUtils.isNotBlank(systemParameter.getParamValue());
    }

    // 获得当天0点 2022-02-17 00:00:00
    public static Date getStartOfDay(Date date) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault());
        LocalDateTime startOfDay = localDateTime.with(LocalTime.MIN);
        return Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Map<Long, AtomicInteger> defaultGroupResult(int time) {
        Date now = getStartOfDay(new Date());
        Map<Long, AtomicInteger> group = new LinkedHashMap<>();
        for (int i = time - 1; i >= 0; i--) {
            group.put(DateUtils.addDays(now, -i).getTime(), new AtomicInteger(0));
        }
        return group;
    }

    public static <T> List<T> page(List<T> list, int goPages, int pageSize) {
        int startIndex = (goPages - 1) * pageSize;
        int endIndex = goPages * pageSize;
        int size = list.size();
        if (size < startIndex) {
            return Lists.newArrayList();
        }
        endIndex = endIndex > size ? size : endIndex;
        return list.subList(startIndex, endIndex);
    }

    public static <T, I> void refreshCacheProbability(@NonNull LoadingCache<T, I> caches, T key, int probability) {
        Random rand = new Random();
        if (rand.nextInt(100) > probability) {
            return;
        }
        caches.refresh(key);
    }
}
