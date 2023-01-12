package com.fit2cloud.devops.common.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.commons.utils.LogUtil;
import com.fit2cloud.devops.base.domain.DevopsCloudServer;
import com.fit2cloud.devops.base.domain.InstanceType;
import com.fit2cloud.devops.dto.ClusterDTO;
import com.fit2cloud.devops.dto.ClusterRoleDTO;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yankaijun
 * @date 2019-07-30 17:11
 */
public class ExcelUtils {

    private final static List<String> cloudServerRowNameList = new ArrayList() {{
        add("集群名称*");
        add("主机组名称*");
        add("主机名称*");
        add("管理IP*");
        add("管理端口*");
        add("用户名*");
        add("密码*");
        add("主机配置");
        add("操作系统*");
        add("系统版本*");
        add("主机状态");
    }};

    /**
     * 判断行标签
     *
     * @param row
     * @return
     */
    public static String getJudgementTag(Row row) {
        Iterator<Cell> iterator = row.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            String rowName = cloudServerRowNameList.get(count);
            if (!(rowName != null && rowName.equals(row.getCell(count).toString()))) {
                return "第2行第" + (count + 1) + "列不符合规范";
            }
            ++count;
            if (count > 10) {
                break;
            }
        }
        return null;
    }

    /**
     * 数据校验
     *
     * @param row
     * @param num
     * @return
     */
    public static String parseRowAndFillData(JSONArray rowArray, Row row, int num, List<ClusterDTO> clusterList, List<ClusterRoleDTO> clusterRoleList,
                                             List<InstanceType> instanceTypeList, List<Map> devopsScriptList, List<DevopsCloudServer> serverList, List<String> ipList) {
        JSONObject rowJson = new JSONObject();
        Iterator<Cell> iterator = row.iterator();
        DataFormatter dataFormatter = new DataFormatter();
        int count = 0;
        while (iterator.hasNext()) {
            boolean b = false;
            boolean clusterRoleFalg = false;
            Cell cell = row.getCell(count);
            String rowName = cloudServerRowNameList.get(count);
            if (cell == null && rowName.contains("*")) {
                return "第" + num + "行第" + (count + 1) + "列" + rowName + "不能为空";
            }
            String cellValue = cell.toString();
            switch (count) {
                case 0:
                    for (ClusterDTO cluster : clusterList) {
                        if (cluster.getName().equals(cellValue)) {
                            b = true;
                            rowJson.put("clusterId", cluster.getId());
                            break;
                        }
                    }
                    if (!b) {
                        return "第" + num + "行第" + (count + 1) + "列无" + cellValue + "集群名称";
                    }
                    break;
                case 1:
                    JSONArray array = new JSONArray();
                    String[] clusterRoleNames = cellValue.split(",");
                    for (String clusterRoleName : clusterRoleNames) {
                        for (ClusterRoleDTO clusterRole : clusterRoleList) {
                            if (clusterRole.getName().equals(clusterRoleName)) {
                                clusterRoleFalg = true;
                                if (clusterRole.getClusterName().equals(row.getCell(count - 1).toString())) {
                                    b = true;
                                    array.add(clusterRole.getId());
                                    //break;
                                }
                            }
                        }
                    }
                    rowJson.put("clusterRoleId", array);
                    if (!clusterRoleFalg) {
                        return "第" + num + "行第" + (count + 1) + "列无" + cellValue + "主机组";
                    }
                    if (!b) {
                        return "第" + num + "行第" + (count + 1) + "列主机组不在" + row.getCell(count - 1).toString() + "集群中";
                    }
                    break;
                case 2:
                    rowJson.put("instanceName", cellValue);
                    break;
                case 3:
                    if (!isIP(cellValue)) {
                        return "第" + num + "行第" + (count + 1) + "列" + cellValue + "不符合管理IP格式";
                    }
                    rowJson.put("managementIp", cellValue);
                    break;
                case 4:
                    if (!isNumeric(dataFormatter.formatCellValue(cell))) {
                        return "第" + num + "行第" + (count + 1) + "列" + cellValue + "不符合管理端口格式";
                    }
                    rowJson.put("managementPort", Integer.valueOf(dataFormatter.formatCellValue(cell)));
                    break;
                case 5:
                    rowJson.put("username", cellValue);
                    break;
                case 6:
                    rowJson.put("password", cellValue);
                    break;
                case 7:
                    long sum = instanceTypeList.stream().filter(instanceType -> {
                        if (instanceType.getName().equals(cellValue)) {
                            return true;
                        } else {
                            return false;
                        }
                    }).count();
                    if (sum <= 0) {
                        return "第" + num + "行第" + (count + 1) + "列主机配置不符合规范";
                    }
                    rowJson.put("instanceType", cellValue);
                    break;
                case 8:
                    for (Map map : devopsScriptList) {
                        LogUtil.info(map.get("value"));
                        LogUtil.info(cellValue);
                        if (map.get("value").toString().replaceAll(" ", "").equals(cellValue.replaceAll(" ", ""))) {
                            rowJson.put("os", map.get("key"));
                            break;
                        }
                    }
                    break;
                case 9:
                    cell.setCellType(CellType.STRING);
                    rowJson.put("osVersion", cell.getStringCellValue());
                    break;
                case 10:
                    if (cell == null) {
                        rowJson.put("instanceStatus", "Running");
                    } else {
                        rowJson.put("instanceStatus", cellValue);
                    }
                    break;
                default:
                    break;
            }
            ++count;
            if (count > 10) {
                break;
            }
        }
        rowArray.add(rowJson);
        return null;
    }

    public static boolean isExcel2003(String filePath) {
        return filePath.matches("^.+\\\\.(?i)(xls)$");
    }

    public static boolean isExcel2007(String filePath) {
        return filePath.matches("^.+\\\\.(?i)(xlsx)$");
    }

    private static boolean isIP(String addr) {
        if (addr.length() < 7 || addr.length() > 15 || "".equals(addr)) {
            return false;
        }
        /**
         * 判断IP格式和范围
         */
        String rexp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        Pattern pat = Pattern.compile(rexp);
        Matcher mat = pat.matcher(addr);
        boolean ipAddress = mat.find();
        return ipAddress;
    }

    private static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

}
