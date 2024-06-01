package com.sen.senoj.judge.codesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExecuteCodeResponse {
    private List<String> outputList;
    /*
    * 接口信息
    * */
    private String message;
    private JudgeInfo judgeInfo;
    /*
    * 判题信息
    * */
    private Integer status;

}
