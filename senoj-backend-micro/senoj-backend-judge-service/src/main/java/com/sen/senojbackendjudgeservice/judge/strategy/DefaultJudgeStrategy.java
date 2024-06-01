package com.sen.senojbackendjudgeservice.judge.strategy;

import cn.hutool.json.JSONUtil;
import com.sen.senojbackendmodel.codesandbox.JudgeInfo;
import com.sen.senojbackendmodel.dto.question.JudgeCase;
import com.sen.senojbackendmodel.dto.question.JudgeConfig;
import com.sen.senojbackendmodel.entity.Question;
import com.sen.senojbackendmodel.enums.JudgeInfoMessageEnum;

import java.util.List;
import java.util.Optional;

/**
 * @ClassDescription:执行判题逻辑
 *
 * @JdkVersion: 11
 * @Author: chensen
 * @Created: 2024/5/13 18:15
 */
public class DefaultJudgeStrategy implements JudgeStrategy {

    /*
 先判断结果的数量是否相等
 判断每一项结果是否相同
 判断事件限制信息
*/
    @Override
    public JudgeInfo doJudge(JudgeContext judgeContext) {
        JudgeInfo judgeInfo = judgeContext.getJudgeInfo();
        List<String> inputList = judgeContext.getInputList();
        List<String> outputList = judgeContext.getOutputList();
        Question question = judgeContext.getQuestion();
        List<JudgeCase> judgeCaseList = judgeContext.getJudgeCaseList();

        JudgeInfoMessageEnum judgeInfoMessageEnum = JudgeInfoMessageEnum.ACCEPTED;
        Long respMemory = Optional.ofNullable(judgeInfo.getMemory()).orElse(0L)/1024/1024 + 1;
        Long respTime = Optional.ofNullable(judgeInfo.getTime()).orElse(0L);
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
        if (respMemory== null || respTime==null){
            throw new NullPointerException();
        }
        if (respMemory > memoryLimit) {
            judgeInfoMessageEnum = JudgeInfoMessageEnum.MEMORY_LIMIT_EXCEEDED;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResponse;
        }
        if (respTime > timeLimit) {
            judgeInfoMessageEnum = JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResponse;
        }

        return judgeInfoResponse;

    }
}
