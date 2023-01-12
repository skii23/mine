package com.fit2cloud.devops.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.commons.server.base.domain.CloudServerCredential;
import com.fit2cloud.commons.server.base.mapper.CloudServerCredentialMapper;
import com.fit2cloud.commons.server.exception.F2CException;
import com.fit2cloud.commons.server.utils.SessionUtils;
import com.fit2cloud.commons.utils.LogUtil;
import com.fit2cloud.commons.utils.UUIDUtil;
import com.fit2cloud.devops.base.domain.*;
import com.fit2cloud.devops.base.mapper.*;
import com.fit2cloud.devops.common.util.CommonUtils;
import com.fit2cloud.devops.common.util.ExcelUtils;
import com.fit2cloud.devops.dao.ext.ExtAccountMapper;
import com.fit2cloud.devops.dao.ext.ExtDevopsServerMapper;
import com.fit2cloud.devops.dto.CloudAccountDTO;
import com.fit2cloud.devops.dto.ClusterDTO;
import com.fit2cloud.devops.dto.ClusterRoleDTO;
import com.fit2cloud.devops.dto.ServerDTO;
import com.fit2cloud.devops.vo.ConnectTestResultVO;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class ServerService {

    @Resource
    private ExtDevopsServerMapper extDevopsServerMapper;
    @Resource
    private CloudServerDevopsMapper cloudServerDevopsMapper;
    @Resource
    private ExtAccountMapper extAccountMapper;
    @Resource
    private ClusterRoleService clusterRoleService;
    @Resource
    private ClusterService clusterService;
    @Resource
    private ProxyService proxyService;
    @Resource
    private DevopsCloudServerMapper devopsCloudServerMapper;
    @Resource
    private CloudServerCredentialMapper serverCredentialMapper;
    @Resource
    private InstanceTypeMapper instanceTypeMapper;
    @Resource
    private DevopsScriptService devopsScriptService;
    @Resource
    private CredentialService credentialService;

    public List<ServerDTO> selectServerDto(Map<String, Object> params) {
        CommonUtils.filterPermission(params);
        if (StringUtils.equalsIgnoreCase(MapUtils.getString(params, "clusterRoleId"), "ALL")) {
            params.remove("clusterRoleId");
        }

        if (params.get("osVersions") != null) {
            List<String> osVersions = new ArrayList<>();
            String versions = MapUtils.getString(params, "osVersions");
            if (versions.contains(",")) {
                osVersions.addAll(Arrays.asList(versions.split(",")));
            } else {
                osVersions.add(versions);
            }
            params.put("osVersions", osVersions);
        }
        return extDevopsServerMapper.selectDevopsServer(params);
    }

    public CloudServerDevops getCloudServerDevops(String cloudServerId) {
        return cloudServerDevopsMapper.selectByPrimaryKey(cloudServerId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void proxyServers(List<String> cloudServerIds, String proxyId) {
        try {
            Proxy proxy = proxyService.getProxy(proxyId);
            if (proxy != null) {
                cloudServerIds.forEach(cloudServerId -> {
                    CloudServerDevops cloudServerDevops = new CloudServerDevops();
                    cloudServerDevops.setId(cloudServerId);
                    cloudServerDevops.setProxyId(proxyId);
                    if (cloudServerDevopsMapper.updateByPrimaryKeySelective(cloudServerDevops) == 0) {
                        cloudServerDevopsMapper.insert(cloudServerDevops);
                    }
                });
            }
        } catch (Exception e) {
            F2CException.throwException("请先将需要设置代理的主机加入主机组！");
        }
    }

    public List<CloudAccountDTO> getAccountList(Map<String, Object> param) {
        return extAccountMapper.getAccountList(param);
    }

    /**
     * 加入到主机组
     *
     * @param cloudServerIds
     * @param clusterRoleId
     */
    public void groupServer(List<String> cloudServerIds, String clusterRoleId) {
        ClusterRole clusterRole = clusterRoleService.getClusterRole(clusterRoleId);
        cloudServerIds.forEach(cloudServerId -> {
            CloudServerDevops cloudServerDevops = new CloudServerDevops();
            cloudServerDevops.setId(cloudServerId);
            cloudServerDevops.setClusterId(clusterRole.getClusterId());
            cloudServerDevops.setClusterRoleId(clusterRole.getId());
            if (cloudServerDevopsMapper.updateByPrimaryKeySelective(cloudServerDevops) == 0) {
                cloudServerDevopsMapper.insert(cloudServerDevops);
            }
        });
    }

    /***
     * 移出主机组
     * @param serverIds
     */
    public void unProxyServer(List<String> serverIds) {
        for (String id : serverIds) {
            CloudServerDevops cloudServerDevops = getCloudServerDevops(id);
            cloudServerDevops.setProxyId(null);
            cloudServerDevopsMapper.updateByPrimaryKey(cloudServerDevops);
        }
    }

    /**
     * 将选择的主机加入到主机组
     * 更改为主机和主机组n->n的关系
     *
     * @param cloudServerIds
     * @param clusterRoleId
     */
    public void groupServerMulti(List<String> cloudServerIds, String clusterRoleId) {
        String[] clusterRoleIds = clusterRoleId.split(",");
        ClusterRole clusterRole = clusterRoleService.getClusterRole(clusterRoleIds[0]);
        cloudServerIds.forEach(cloudServerId -> {
            CloudServerDevops cloudServerDevops = new CloudServerDevops();
            cloudServerDevops.setId(cloudServerId);
            cloudServerDevops.setClusterId(clusterRole.getClusterId());
            //获得已有的主机组ID字符串
            StringBuffer roleStr = new StringBuffer();
            CloudServerDevops cloudServerDevopsTemp = cloudServerDevopsMapper.selectByPrimaryKey(cloudServerId);
            if (cloudServerDevopsTemp != null) {
                String clusterRoleStr = cloudServerDevopsTemp.getClusterRoleId();
                roleStr.append(clusterRoleStr);
                for (String roleId : clusterRoleIds) {
                    if (!clusterRoleStr.contains(roleId)) {
                        roleStr.append("," + roleId);
                    }
                }
                cloudServerDevops.setClusterRoleId(roleStr.toString());
            } else {
                cloudServerDevops.setClusterRoleId(clusterRoleId);
            }
            if (cloudServerDevopsMapper.updateByPrimaryKeySelective(cloudServerDevops) == 0) {
                cloudServerDevopsMapper.insert(cloudServerDevops);
            }
        });
    }

    public void unGroupServer(List<String> serverIds) {
        for (String id : serverIds) {
            cloudServerDevopsMapper.deleteByPrimaryKey(id);
        }
    }

    /***
     *  为选择的主机做连接性测试
     *  如果没有做选择就全部测试
     * @param serverDTOs 待测试的主机
     * @return
     */
    public ConnectTestResultVO connectTest(List<ServerDTO> serverDTOs) {
        //如果为选主机，则测试所有主机
        if (serverDTOs.isEmpty()) {
            serverDTOs = selectServerDto(new HashMap<>());
        }
        ConnectTestResultVO result = new ConnectTestResultVO();
        result.setStartTime(System.currentTimeMillis());
        result.setTotal(serverDTOs.size());
        result.setResults(serverDTOs);
        int successNum = 0, failedNum = 0;
        boolean flag;
        for (ServerDTO server : serverDTOs) {
            try {
                credentialService.findDevOpsCloudServerCredential(server.getId(), 2);
                flag = true;
                successNum++;
            } catch (Exception e) {//连接失败
                flag = false;
                server.setConnectMsg(e.getMessage());
                failedNum++;
            }
//            手动检测也更新连通性状态
            DevopsCloudServer devopsCloudServer = new DevopsCloudServer();
            devopsCloudServer.setId(server.getId());
            if (server.getConnectable() == null || flag != server.getConnectable()) {
                devopsCloudServer.setConnectable(flag);
                devopsCloudServerMapper.updateByPrimaryKeySelective(devopsCloudServer);
            }
//            最后再把状态设置进去
            server.setConnectable(flag);
        }
        result.setSuccess(successNum);
        result.setFailed(failedNum);
        result.setEndTime(System.currentTimeMillis());
        return result;
    }

    /**
     * 删除主机：
     * 逻辑删除，将Server的instance_status字段改为Deleted
     */
    public void deleteServers(List<String> serverIds) {
        for (String serverId : serverIds) {
            DevopsCloudServer server = devopsCloudServerMapper.selectByPrimaryKey(serverId);
            server.setInstanceStatus("Deleted");
            devopsCloudServerMapper.updateByPrimaryKeySelective(server);
        }

    }

    public String save(String server) {
        saveCheck(server);
        try {
            JSONObject serverJson = JSON.parseObject(server);
            //添加主机、集群、主机组的关系
            CloudServerDevops cloudServerDevops = new CloudServerDevops();
            cloudServerDevops.setId(UUIDUtil.newUUID());
            cloudServerDevops.setClusterId(serverJson.getString("clusterId"));
            String buffer = toClusterRoleIdStr(serverJson.getString("clusterRoleId"));
            cloudServerDevops.setClusterRoleId(buffer);
            cloudServerDevops.setProxyId(serverJson.getString("proxyId"));
            cloudServerDevopsMapper.insert(cloudServerDevops);

            //导入主机
            DevopsCloudServer cloudServerImported = JSON.parseObject(server, DevopsCloudServer.class);
            cloudServerImported.setId(cloudServerDevops.getId());
//           这边设置为null是为了将手动导入以及excel导入和云主机导入的区分开
            cloudServerImported.setInstanceUuid(null);
            cloudServerImported.setInstanceId(cloudServerImported.getInstanceName());
            if (cloudServerImported.getWorkspaceId() == null) {
                cloudServerImported.setWorkspaceId(SessionUtils.getWorkspaceId());
            }
            cloudServerImported.setCreateTime(System.currentTimeMillis());
            //这里导入的云账号设置默认值
            cloudServerImported.setAccountId("N/A");
            cloudServerImported.setSource("LOCAL");
            setServer(cloudServerImported);
            devopsCloudServerMapper.insert(cloudServerImported);

            //添加服务器账号密码
            CloudServerCredential serverCredential = new CloudServerCredential();
            serverCredential.setId(UUIDUtil.newUUID());
            serverCredential.setUsername(serverJson.getString("username"));
            serverCredential.setPassword(serverJson.getString("password"));
            serverCredential.setSecretKey(serverJson.getString("secretKey"));
            serverCredential.setCloudServerId(cloudServerImported.getId());
            serverCredential.setCreateTime(System.currentTimeMillis());
            serverCredentialMapper.insert(serverCredential);
        } catch (Exception e) {
            LogUtil.error(e);
            return "添加失败";
        }
        return "添加成功";
    }

    public String update(String server) {
        saveCheck(server);
        //修改主机
        DevopsCloudServer cloudServer = JSON.parseObject(server, DevopsCloudServer.class);
        setServer(cloudServer);
        JSONObject serverJson = JSON.parseObject(server);
        devopsCloudServerMapper.updateByPrimaryKeySelective(cloudServer);
        String buffer = toClusterRoleIdStr(serverJson.getString("clusterRoleId"));
        CloudServerDevops cloudServerDevops = cloudServerDevopsMapper.selectByPrimaryKey(cloudServer.getId());
        if (cloudServerDevops == null) {
            cloudServerDevops = new CloudServerDevops();
            cloudServerDevops.setId(cloudServer.getId());
            cloudServerDevops.setClusterId(serverJson.getString("clusterId"));
            cloudServerDevops.setClusterRoleId(buffer);
            cloudServerDevops.setProxyId(cloudServer.getProxyId());
            cloudServerDevopsMapper.insert(cloudServerDevops);
        } else {
            cloudServerDevops.setClusterId(serverJson.getString("clusterId"));
            cloudServerDevops.setClusterRoleId(buffer);
            cloudServerDevops.setProxyId(cloudServer.getProxyId());
            cloudServerDevopsMapper.updateByPrimaryKeySelective(cloudServerDevops);
        }
        //修改服务器账号密码密码
        CloudServerCredential serverCredential = credentialService.selectByServerId(cloudServer.getId());
        //导入的虚拟机可能没有密码信息
        if (serverCredential == null) {
            serverCredential = new CloudServerCredential();
            serverCredential.setId(UUIDUtil.newUUID());
            serverCredential.setCloudServerId(cloudServer.getId());
            serverCredential.setCreateTime(System.currentTimeMillis());
            serverCredential.setUsername(serverJson.getString("username"));
            serverCredential.setPassword(serverJson.getString("password"));
            serverCredential.setSecretKey(serverJson.getString("secretKey"));
            serverCredentialMapper.insert(serverCredential);
        } else {
            serverCredential.setUsername(serverJson.getString("username"));
            serverCredential.setPassword(serverJson.getString("password"));
            serverCredential.setSecretKey(serverJson.getString("secretKey"));
            serverCredentialMapper.updateByPrimaryKeyWithBLOBs(serverCredential);
        }
        return "修改成功";
    }

    public String toClusterRoleIdStr(String clusterRoleIds) {
        StringBuffer buffer = new StringBuffer();
        try {
            JSONArray jsonArray = JSONArray.parseArray(clusterRoleIds);
            Object[] arr = jsonArray.toArray();
            //多主机组
            for (int i = 0; i < arr.length; i++) {
                if (i == arr.length - 1) {
                    buffer.append(arr[i].toString());
                } else {
                    buffer.append(arr[i].toString() + ",");
                }
            }
        } catch (Exception e) {
            buffer.append(clusterRoleIds);
        }
        return buffer.toString();
    }

    private DevopsCloudServer setServer(DevopsCloudServer server) {
        server.setUpdateTime(System.currentTimeMillis());
        server.setLastSyncTimestamp(System.currentTimeMillis());
        JSONArray array = new JSONArray();
        array.add(server.getManagementIp());
        server.setIpArray(array.toJSONString());
        server.setHost(server.getManagementIp());
        if (server.getInstanceType() != null) {
            server.setInstanceTypeDescription(server.getInstanceType().replace("vm.", "").replace("c", "核").replace("g", "G"));
        }
        return server;
    }

    public List<InstanceType> getAllInstance() {
        return instanceTypeMapper.selectByExample(new InstanceTypeExample());
    }

    /**
     * 解析xlsx
     *
     * @param excelFile
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = {RuntimeException.class, Exception.class})
    public String getImportXlsx(MultipartFile excelFile) throws Exception {
        if (excelFile == null) {
            F2CException.throwException("files不允许空");
        }
        if (excelFile.isEmpty()) {
            F2CException.throwException("导入失败：文件为空");
        }
        //获取文件名
        String fileName = excelFile.getOriginalFilename();

        String osName = System.getProperty("os.name").toLowerCase();
        String uploadPath = "";
        String separator = File.separator;
        if (osName.contains("windows")) {
            //"C:/Windows/Temp/fit2cloud":"/tmp/fit2cloud"
            uploadPath = "C:" + separator + "Windows" + separator + "Temp" + separator + "fit2cloud" + separator + "fileupload" + separator;
        } else if (osName.contains("linux")) {
            uploadPath = separator + "tmp" + separator + "fit2cloud" + separator + "fileupload" + separator;
        } else if (osName.contains("mac")) {
            uploadPath = separator + "tmp" + separator + "fit2cloud" + separator + "fileupload" + separator;
        }

        File file = new File(uploadPath);//D:\fileupload
        //创建一个目录 （它的路径名由当前 File 对象指定，包括任一必须的父路径。）
        if (!file.exists()) {
            boolean b = file.mkdirs();
            LogUtil.info(b);
        }
        //新建一个文件
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss");
        String l = sdf.format(new Date());
        File file1 = new File(uploadPath + l + ".xlsx");

        //将上传的文件写入新建的文件中
        try {
            fileName = excelFile.getOriginalFilename();
            byte[] bytes = excelFile.getBytes();

            BufferedOutputStream buffStream =
                    new BufferedOutputStream(new FileOutputStream(new File(uploadPath + l + "." + fileName.split("\\.")[1])));
            buffStream.write(bytes);
            buffStream.close();
        } catch (Exception e) {
            LogUtil.error(e);
        }
        InputStream is = new FileInputStream(file1);
        boolean isExcel2007 = true;
        if (ExcelUtils.isExcel2003(fileName)) {
            isExcel2007 = false;
        }
        Workbook workbook;
        //当excel是2007时
        if (isExcel2007) {
            workbook = new XSSFWorkbook(is);
        } else {//当excel是2003时
            workbook = new HSSFWorkbook(is);
        }
        Sheet sheet = workbook.getSheetAt(0);
        int totalRows = sheet.getPhysicalNumberOfRows();
        Iterator<Row> iterator = sheet.iterator();
        Row row;
        int count = 0;
        List<ClusterDTO> clusterList = clusterService.listAll(null);
        List<ClusterRoleDTO> clusterRoleList = clusterRoleService.selectClusterRole(new HashMap<>());
        List<InstanceType> instanceTypeList = instanceTypeMapper.selectByExample(new InstanceTypeExample());
        List<Map> devopsScriptList = devopsScriptService.getOsList();
        List<DevopsCloudServer> serverList = devopsCloudServerMapper.selectByExample(null);
        JSONArray rowArray = new JSONArray();
        List<String> ipList = new ArrayList<String>();
        while (iterator.hasNext()) {
            row = iterator.next();
            //由于第一行是标题，因此这里单独处理
            if (count == 0) {
                if (!"主机导入模版".equals(row.getCell(0).toString())) {
                    return "第1行" + row.getCell(0) + "不等于主机导入模版";
                }
                row.getCell(0);
            } else if (count == 1) {
                //第二行标签
                String JudgementTag = ExcelUtils.getJudgementTag(row);
                if (JudgementTag != null) {
                    return JudgementTag;
                }
            } else {
                //其它行都在这里处理
                LogUtil.info(row.getCell(3).toString());
                String retStr = ExcelUtils.parseRowAndFillData(rowArray, row, count + 1, clusterList, clusterRoleList, instanceTypeList, devopsScriptList, serverList, ipList);
                ipList.add(row.getCell(3).toString());
                if (retStr != null) {
                    return retStr;
                }
            }
            ++count;
            if (totalRows == count) {
                break;
            }
        }
        if (rowArray.size() == 0) {
            return "文件数据为空,请重新选择!";
        }
        for (int i = 0; i < rowArray.size(); i++) {
            JSONObject rowJson = rowArray.getJSONObject(i);
            String result = save(rowJson.toString());
            if (!"添加成功".equals(result)) {
                F2CException.throwException(result);
                return result;
            }
        }
        return rowArray.size() + "";
    }

    /**
     * 导入云账号虚拟机
     *
     * @param params
     */
    public void importCloudServer(Map<String, Object> params) {
        List<DevopsCloudServer> serverList = transfer(params);

        if (serverList.isEmpty()) {
            F2CException.throwException("要导入的数据为空！");
        }
        String clusterId = (String) params.get("clusterId");
        List<String> clusterRoleId = (List<String>) params.get("currentClusterRole");
        for (int i = 0; i < serverList.size(); i++) {
            DevopsCloudServer server = serverList.get(i);
            if (server != null) {
                LogUtil.info(i + ". 导入的主机信息为：" + JSONObject.toJSONString(server));
                importCheck(server, clusterId, clusterRoleId);
            }
        }
        for (int i = 0; i < serverList.size(); i++) {
            DevopsCloudServer server = serverList.get(i);
            if (server != null) {
                insertNewServer(server, server.getId(), clusterId, clusterRoleId);
            }
        }
    }

    private void insertNewServer(DevopsCloudServer devopsCloudServer, String oldId, String clusterId, List<String> clusterRoleIds) {
        devopsCloudServer.setId(UUIDUtil.newUUID());
        if (StringUtils.isNotBlank(devopsCloudServer.getInstanceName())) {
            devopsCloudServer.setInstanceId(devopsCloudServer.getInstanceName());
        }
        devopsCloudServer.setUpdateTime(System.currentTimeMillis());
        if (StringUtils.isBlank(devopsCloudServer.getInstanceUuid())) {
            devopsCloudServer.setInstanceUuid(devopsCloudServer.getId());
        }
        devopsCloudServer.setSource("CLOUD_IMPORT");
        devopsCloudServerMapper.insert(devopsCloudServer);

        CloudServerCredential credential = credentialService.selectByServerId(oldId);
        credential.setId(UUIDUtil.newUUID());
        credential.setCloudServerId(devopsCloudServer.getId());
        credential.setCreateTime(System.currentTimeMillis());
        serverCredentialMapper.insert(credential);

        CloudServerDevops cloudServerDevops = new CloudServerDevops();
        cloudServerDevops.setId(devopsCloudServer.getId());
        cloudServerDevops.setClusterId(clusterId);
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < clusterRoleIds.size(); i++) {
            if (i == clusterRoleIds.size() - 1) {
                buffer.append(clusterRoleIds.get(i));
            } else {
                buffer.append(clusterRoleIds.get(i) + ",");
            }
        }
        cloudServerDevops.setClusterRoleId(buffer.toString());
        cloudServerDevopsMapper.insert(cloudServerDevops);
    }

    private void importCheck(DevopsCloudServer devopsCloudServer, String clusterId, List<String> clusterRoleIds) {
        if (StringUtils.isBlank(clusterId) || clusterRoleIds == null) {
            F2CException.throwException("导入参数错误，请查看日志！");
        }
        Cluster cluster = clusterService.selectClusterById(clusterId);
        if (cluster == null) {
            F2CException.throwException("所选集群不存在！");
        }
        CloudServerCredential credential = credentialService.selectByServerId(devopsCloudServer.getId());
        if (credential == null) {
            F2CException.throwException("导入的主机：" + devopsCloudServer.getInstanceName() + " 未找到有效的用户密码信息！");
        }
        Map map = new HashMap();
        map.put("clusterId", clusterId);
        for (String roleId : clusterRoleIds) {
            ClusterRole clusterRole = clusterRoleService.getClusterRole(roleId);
            map.put("clusterRoleId", roleId);
            List<ServerDTO> serverDevops = extDevopsServerMapper.checkServerInCluster(map);
            if (!serverDevops.isEmpty()) {
                for (ServerDTO serverDTO : serverDevops) {
                    DevopsCloudServer server = devopsCloudServerMapper.selectByPrimaryKey(serverDTO.getId());
                    if (server == null || !StringUtils.equalsIgnoreCase(server.getInstanceStatus(), "Running")) {
                        continue;
                    }
                    if (StringUtils.equalsIgnoreCase(devopsCloudServer.getInstanceName(), server.getInstanceId())) {
                        F2CException.throwException("在集群：" + cluster.getName() + " 下的 " + clusterRole.getName() + " 主机组中已存在名称为：" + devopsCloudServer.getInstanceName() + " 的主机，请勿重复添加！");
                    }
                }
            }
        }
    }

    private void saveCheck(String serverStr) {
        JSONObject serverJson = JSON.parseObject(serverStr);
        String serverId = serverJson.getString("id");
        String clusterId = serverJson.getString("clusterId");
        String instanceName = serverJson.getString("instanceName");
        Cluster cluster = clusterService.selectClusterById(clusterId);
        if (cluster == null) {
            F2CException.throwException("所选集群不存在！");
        }
        Map map = new HashMap() {{
            put("clusterId", clusterId);
        }};
        String clusterRoleIdStr = toClusterRoleIdStr(serverJson.getString("clusterRoleId"));
        String[] roleIdArr = clusterRoleIdStr.split(",");
        for (Object roleId : roleIdArr) {
            ClusterRole clusterRole = clusterRoleService.getClusterRole(roleId.toString());
            map.put("clusterRoleId", roleId.toString());
            List<ServerDTO> serverDevops = extDevopsServerMapper.checkServerInCluster(map);
            if (!serverDevops.isEmpty()) {
                for (ServerDTO serverDTO : serverDevops) {
                    DevopsCloudServer server = devopsCloudServerMapper.selectByPrimaryKey(serverDTO.getId());
                    if (server == null || !StringUtils.equalsIgnoreCase(server.getInstanceStatus(), "Running")) {
                        continue;
                    }
                    if (StringUtils.isNotBlank(serverId) && StringUtils.equalsIgnoreCase(serverId, server.getId())) {
                        continue;
                    }
                    if (StringUtils.equalsIgnoreCase(instanceName, server.getInstanceId())) {
                        F2CException.throwException("在集群：" + cluster.getName() + " 下的 " + clusterRole.getName() + " 主机组中已存在名称为：" + instanceName + " 的主机，请勿重复添加！");
                    }
                }
            }
        }
    }

    private List<DevopsCloudServer> transfer(Map<String, Object> params) {
        String serversStr = JSONObject.toJSONString(params.get("servers"));
        List<DevopsCloudServer> serverList = JSONObject.parseArray(serversStr, DevopsCloudServer.class);
        return serverList;
    }

}
