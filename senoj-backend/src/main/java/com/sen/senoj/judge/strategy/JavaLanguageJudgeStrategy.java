package com.sen.senoj.judge.strategy;

import cn.hutool.json.JSONUtil;
import com.sen.senoj.exception.BusinessException;
import com.sen.senoj.model.dto.question.JudgeCase;
import com.sen.senoj.model.dto.question.JudgeConfig;
import com.sen.senoj.judge.codesandbox.model.JudgeInfo;
import com.sen.senoj.model.entity.Question;
import com.sen.senoj.model.enums.JudgeInfoMessageEnum;

import java.util.List;

/**
 * @ClassDescription:执行判题逻辑
 * @JdkVersion: 11
 * @Author: chensen
 * @Created: 2024/5/13 18:15
 */
public class JavaLanguageJudgeStrategy implements JudgeStrategy {

    @Override
    public JudgeInfo doJudge(JudgeContext judgeContext) {
        JudgeInfo judgeInfo = judgeContext.getJudgeInfo();
        List<String> inputList = judgeContext.getInputList();
        List<String> outputList = judgeContext.getOutputList();
        Question question = judgeContext.getQuestion();
        List<JudgeCase> judgeCaseList = judgeContext.getJudgeCaseList();

        JudgeInfoMessageEnum judgeInfoMessageEnum = JudgeInfoMessageEnum.ACCEPTED;
        Long respMemory = judgeInfo.getMemory();
        Long respTime = judgeInfo.getTime();
        JudgeInfo judgeInfoResponse = new JudgeInfo();
        judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
        judgeInfoResponse.setMemory(respMemory);
        judgeInfoResponse.setTime(respTime);

        if (outputList.size() != inputList.size()) {
            judgeInfoMessageEnum = JudgeInfoMessageEnum.WRONG_ANSWER;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResponse;
        }

        for (int i = 0; i < judgeCaseList.size(); i++) {
            JudgeCase judgeCase = judgeCaseList.get(i);
            if (!judgeCase.getOutput().equals(outputList.get(i))) {
                judgeInfoMessageEnum = JudgeInfoMessageEnum.WRONG_ANSWER;
                judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
                return judgeInfoResponse;
            }
        }

        //        代码在代码沙箱运行的相关信息

        String judgeConfigStr = question.getJudgeConfig();
        JudgeConfig judgeConfig = JSONUtil.toBean(judgeConfigStr, JudgeConfig.class);
        Long memoryLimit = judgeConfig.getMemoryLimit();
        Long timeLimit = judgeConfig.getTimeLimit();
        //此处respMemory可能为空
        if (respMemory== null || respTime==null){
            throw new NullPointerException();
        }
        if (respMemory!= null && respMemory > memoryLimit) {
            judgeInfoMessageEnum = JudgeInfoMessageEnum.MEMORY_LIMIT_EXCEEDED;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResponse;
        }
//        long JAVA_PROGRAM_TIME_COST = 10000L;
        //todo 可以在这里设置特殊的判题逻辑
        if (respTime!=null && respTime > timeLimit) {
            judgeInfoMessageEnum = JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResponse;
        }

        return judgeInfoResponse;

    }
}
