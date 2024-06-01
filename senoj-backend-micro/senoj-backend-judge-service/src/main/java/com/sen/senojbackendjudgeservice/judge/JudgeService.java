package com.sen.senojbackendjudgeservice.judge;


import com.sen.senojbackendmodel.entity.QuestionSubmit;

/**
 * @ClassDescription:
 * @JdkVersion: 11
 * @Author: chensen
 * @Created: 2024/5/13 16:17
 */
public interface JudgeService {

    QuestionSubmit doJudge(long questionSubmitId);
}
