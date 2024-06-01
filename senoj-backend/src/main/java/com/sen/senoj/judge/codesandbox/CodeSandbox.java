package com.sen.senoj.judge.codesandbox;

import com.sen.senoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.sen.senoj.judge.codesandbox.model.ExecuteCodeResponse;

public interface CodeSandbox {
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRespect);
}
