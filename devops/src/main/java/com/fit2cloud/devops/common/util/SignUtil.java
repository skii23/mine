package com.fit2cloud.devops.common.util;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author caiwzh
 * @date 2022/8/1
 */
public class SignUtil {


    public static Map<String, String> buildHeadMap(String appId, String appSecret) {
        Map<String, String> headMap = new HashMap<>();
        String timestamp = String.valueOf(System.currentTimeMillis());
        String sign = getMD5(appId + ":" + appSecret + ":" + timestamp);
        headMap.put("appId", appId);
        headMap.put("timestamp", timestamp);
        headMap.put("sign", sign);
        return headMap;
    }

    /**
     * 获取字符串的MD5
     *
     * @param str
     * @return String
     */
    public static String getMD5(String str) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        char[] charArray = str.toCharArray();
        byte[] byteArray = new byte[charArray.length];
        for (int i = 0; i < charArray.length; i++) {
            byteArray[i] = (byte) charArray[i];
        }
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }
}
