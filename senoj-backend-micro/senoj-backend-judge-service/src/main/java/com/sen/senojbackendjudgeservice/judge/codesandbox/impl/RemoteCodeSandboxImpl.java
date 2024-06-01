package com.sen.senojbackendjudgeservice.judge.codesandbox.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.sen.senojbackendcommon.common.ErrorCode;
import com.sen.senojbackendcommon.exception.BusinessException;
import com.sen.senojbackendjudgeservice.judge.codesandbox.CodeSandbox;
import com.sen.senojbackendmodel.codesandbox.ExecuteCodeRequest;
import com.sen.senojbackendmodel.codesandbox.ExecuteCodeResponse;
import org.apache.commons.lang3.StringUtils;

/*
* 调用自己写的
* */
public class RemoteCodeSandboxImpl implements CodeSandbox {

    private static final String AUTH_REQUEST_HEADER = "auth";
    private static final String AUTH_REQUEST_SECRET = "secretKey";

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRespect) {
        System.out.println("RemoteCodeSandboxImpl");
        String url  = "http://192.168.3.123:8080/executeCode";
        String json = JSONUtil.toJsonStr(executeCodeRespect);
        String responseStr = HttpUtil.createPost(url)
                .header(AUTH_REQUEST_HEADER,AUTH_REQUEST_SECRET)
                .body(json)
                .execute()
                .body();
        if (StringUtils.isBlank(responseStr)){
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR,"executeCode remoteSandbox error,message = "+responseStr);
        }
        System.out.println("返回的沙箱信息："+responseStr);
        return JSONUtil.toBean(responseStr, ExecuteCodeResponse.class);
    }
}
