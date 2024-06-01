package com.sen.senojbackendjudgeservice.judge.strategy;

import com.sen.senojbackendmodel.codesandbox.JudgeInfo;
import com.sen.senojbackendmodel.dto.question.JudgeCase;
import com.sen.senojbackendmodel.entity.Question;
import com.sen.senojbackendmodel.entity.QuestionSubmit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @ClassDescription:
 * @JdkVersion: 11
 * @Author: chensen
 * @Created: 2024/5/13 18:14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JudgeContext {
    private JudgeInfo judgeInfo;
    private List<String> inputList;
    private List<String> outputList;
    private Question question;
    private List<JudgeCase> judgeCaseList;
    private QuestionSubmit questionSubmit;
}
