package com.sen.senoj.judge.codesandbox.impl;

import com.sen.senoj.judge.codesandbox.CodeSandbox;
import com.sen.senoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.sen.senoj.judge.codesandbox.model.ExecuteCodeResponse;
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
