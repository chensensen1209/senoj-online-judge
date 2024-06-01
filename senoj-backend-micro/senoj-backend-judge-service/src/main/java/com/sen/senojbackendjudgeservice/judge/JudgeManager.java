package com.sen.senojbackendjudgeservice.judge;

import com.sen.senojbackendjudgeservice.judge.strategy.DefaultJudgeStrategy;
import com.sen.senojbackendjudgeservice.judge.strategy.JavaLanguageJudgeStrategy;
import com.sen.senojbackendjudgeservice.judge.strategy.JudgeContext;
import com.sen.senojbackendjudgeservice.judge.strategy.JudgeStrategy;
import com.sen.senojbackendmodel.codesandbox.JudgeInfo;
import com.sen.senojbackendmodel.entity.QuestionSubmit;
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
