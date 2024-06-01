package com.sen.senojbackendjudgeservice.judge.codesandbox.impl;

import com.sen.senojbackendjudgeservice.judge.codesandbox.CodeSandbox;
import com.sen.senojbackendmodel.codesandbox.ExecuteCodeRequest;
import com.sen.senojbackendmodel.codesandbox.ExecuteCodeResponse;

/*
* 调用第三方 网络现成的
* */
public class ThirdPartyCodeSandboxImpl implements CodeSandbox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRespect) {
        System.out.println("ThirdPartyCodeSandboxImpl");
        return null;
    }
}
