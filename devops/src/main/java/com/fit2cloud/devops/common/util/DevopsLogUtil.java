package com.fit2cloud.devops.common.util;

import java.text.SimpleDateFormat;

public class DevopsLogUtil {
    public static String info(String eventName, String content) {
        return genStr(eventName, LEVEL.INFO.name(), content);
    }

    public static String error(String eventName, String content) {
        return genStr(eventName, LEVEL.ERROR.name(), content);
    }


    private static String genStr(String eventName, String level, String content) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String logFormat = "$timestamp [$eventName]  $level $content";
        String timestamp = sdf.format(System.currentTimeMillis());

        return logFormat.replace("$timestamp", timestamp)
                .replace("$eventName", eventName)
                .replace("$level", level)
                .replace("$content", content) + "\n";
    }

    enum LEVEL {
        ERROR, INFO
    }
}


