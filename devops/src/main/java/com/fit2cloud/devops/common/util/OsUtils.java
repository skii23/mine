package com.fit2cloud.devops.common.util;

import com.fit2cloud.ansible.model.request.ScriptRunRequest;
import com.fit2cloud.commons.utils.UUIDUtil;
import com.fit2cloud.devops.base.domain.Proxy;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class OsUtils {

    public static void mixWinParams(ScriptRunRequest scriptRunRequest) {
        scriptRunRequest.withModule("win_shell");
        Map<String, String> winVars = new HashMap<>();
        winVars.put("ansible_connection", "winrm");
        winVars.put("ansible_winrm_server_cert_validation", "ignore");
        scriptRunRequest.addVars(winVars);
    }

    public static void mixWinExecCmd(ScriptRunRequest scriptRunRequest) {
        String content = scriptRunRequest.getContent();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("$session = $(tasklist /fo CSV | findstr /i explorer).Split(',')[3] ;")
                .append("\n")
                .append("$sid = $session.Split('\"')[1];\n")
                .append("if($sid){\n")
                .append("psexec -accepteula -nobanner -s -i $sid ")
                .append(content)
                .append("\n")
                .append("}\n")
                .append("Else{\n")
                .append("psexec -accepteula -nobanner -s ")
                .append(content)
                .append("\n}");
        scriptRunRequest.withContent(stringBuffer.toString());
    }

    public static String winPython(String pythonScriptContent) throws UnsupportedEncodingException {
        String fileName = "C:/Windows/Temp/" + "tmp-" + UUIDUtil.newUUID();
        String cmd = "echo \"" +
                pythonScriptContent.replace("\n", "`n") +
                "\" " +
                "| Out-File -Encoding utf8 " + fileName +
                "\n" +
                "python " + fileName;
        return new String(cmd.getBytes(), StandardCharsets.UTF_8);

    }

    public static void mixLinuxParams(ScriptRunRequest scriptRunRequest, Integer timeout,
                                      String runas, String username, String password) {

        StringBuffer buffer = new StringBuffer();
        if (timeout != null) {
            buffer.append("timeout ").append(timeout).append(" ");
        }
        if (password != null && !runas.equals(username)) {
            buffer.append("echo \'").append(password).append("\' | ");
        }
        if (runas != null && !runas.equals(username)) {
            buffer.append("sudo -S su - ").append(runas).append(" ");
        }

        buffer.append(scriptRunRequest.getContent());
        scriptRunRequest.withContent(buffer.toString());

    }

    public static void setUpWindowsProxy(ScriptRunRequest request, Proxy proxy) {
        request.addVar("ansible_winrm_path", String.format("/%s/%d/", request.getIp(), request.getPort()))
                .withIp(proxy.getIp())
                .withPort(proxy.getPort());
    }


}
