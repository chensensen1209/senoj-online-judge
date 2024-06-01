package com.sen.senojbackendjudgeservice.judge.codesandbox;


import com.sen.senojbackendmodel.codesandbox.ExecuteCodeRequest;
import com.sen.senojbackendmodel.codesandbox.ExecuteCodeResponse;

public interface CodeSandbox {
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRespect);
}
