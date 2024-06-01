package com.sen.senoj.judge.codesandbox;

import com.sen.senoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.sen.senoj.judge.codesandbox.model.ExecuteCodeResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassDescription:
 * @JdkVersion: 11
 * @Author: chensen
 * @Created: 2024/5/13 16:00
 */
@Slf4j
public class CodeSandboxProxy implements CodeSandbox{
    private final CodeSandbox codeSandbox;

    public CodeSandboxProxy(CodeSandbox codeSandbox) {
        this.codeSandbox = codeSandbox;
    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRespect) {
        log.info("代码沙箱请求信息",executeCodeRespect.toString());
        System.out.println("进入了方法 代码沙箱请求信息"+executeCodeRespect.toString());
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRespect);
        log.info("代码沙箱响应信息",executeCodeResponse.toString());
        System.out.println("代码沙箱响应信息"+executeCodeResponse.toString());
        return executeCodeResponse;
    }
}
