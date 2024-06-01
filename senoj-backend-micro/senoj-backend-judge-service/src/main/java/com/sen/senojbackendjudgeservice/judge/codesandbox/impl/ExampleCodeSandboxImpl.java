package com.sen.senojbackendjudgeservice.judge.codesandbox.impl;


import com.sen.senojbackendjudgeservice.judge.codesandbox.CodeSandbox;
import com.sen.senojbackendmodel.codesandbox.ExecuteCodeRequest;
import com.sen.senojbackendmodel.codesandbox.ExecuteCodeResponse;
import com.sen.senojbackendmodel.codesandbox.JudgeInfo;
import com.sen.senojbackendmodel.enums.JudgeInfoMessageEnum;
import com.sen.senojbackendmodel.enums.QuestionSubmitStatusEnum;

import java.util.ArrayList;
import java.util.List;

/*
* 例子
* */
public class ExampleCodeSandboxImpl implements CodeSandbox {


    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRespect) {
        List<String> inputList = executeCodeRespect.getInputList();
        String code = executeCodeRespect.getCode();
        String language = executeCodeRespect.getLanguage();

        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMessage(JudgeInfoMessageEnum.ACCEPTED.getText());
        judgeInfo.setMemory(100L);
        judgeInfo.setTime(100L);


        executeCodeResponse.setOutputList(new ArrayList<>());
        executeCodeResponse.setMessage("测试执行成功");
        executeCodeResponse.setJudgeInfo(judgeInfo);
        executeCodeResponse.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        return executeCodeResponse;
    }
}
