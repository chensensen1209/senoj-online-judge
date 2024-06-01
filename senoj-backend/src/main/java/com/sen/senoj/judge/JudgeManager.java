package com.sen.senoj.judge;

import com.sen.senoj.judge.strategy.DefaultJudgeStrategy;
import com.sen.senoj.judge.strategy.JavaLanguageJudgeStrategy;
import com.sen.senoj.judge.strategy.JudgeContext;
import com.sen.senoj.judge.strategy.JudgeStrategy;
import com.sen.senoj.judge.codesandbox.model.JudgeInfo;
import com.sen.senoj.model.entity.QuestionSubmit;
import org.springframework.stereotype.Service;

/**
 * @ClassDescription:判题管理简化调用
 * @JdkVersion: 11
 * @Author: chensen
 * @Created: 2024/5/13 18:35
 */
@Service
public class JudgeManager {
    JudgeInfo doJudge(JudgeContext judgeContext){
        QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
        String language = questionSubmit.getLanguage();
        JudgeStrategy judgeStrategy = new DefaultJudgeStrategy();
        if ("java".equals(language)){
            judgeStrategy = new JavaLanguageJudgeStrategy();
        }
        return judgeStrategy.doJudge(judgeContext);
    }
}
