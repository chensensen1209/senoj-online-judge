package com.sen.senoj.judge;

import com.sen.senoj.judge.codesandbox.model.ExecuteCodeResponse;
import com.sen.senoj.model.entity.QuestionSubmit;
import com.sen.senoj.model.vo.QuestionSubmitVO;

/**
 * @ClassDescription:
 * @JdkVersion: 11
 * @Author: chensen
 * @Created: 2024/5/13 16:17
 */
public interface JudgeService {

    QuestionSubmit doJudge(long questionSubmitId);
}
