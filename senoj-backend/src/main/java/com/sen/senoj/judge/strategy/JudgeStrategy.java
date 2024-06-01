package com.sen.senoj.judge.strategy;

import com.sen.senoj.judge.codesandbox.model.JudgeInfo;

/**
 * @ClassDescription:
 * @JdkVersion: 11
 * @Author: chensen
 * @Created: 2024/5/13 18:13
 */
public interface JudgeStrategy {
    /**
     * @param judgeContext:
     * @return JudgeInfo
     * @author xh
     * @description TODO
     * @date 2024/5/13 18:15
     */

    JudgeInfo doJudge(JudgeContext judgeContext);
}
