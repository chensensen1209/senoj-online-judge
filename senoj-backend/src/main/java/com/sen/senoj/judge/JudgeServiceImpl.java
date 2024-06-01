package com.sen.senoj.judge;
import java.util.List;
import java.util.stream.Collectors;

import cn.hutool.json.JSONUtil;
import com.sen.senoj.common.ErrorCode;
import com.sen.senoj.exception.BusinessException;
import com.sen.senoj.judge.codesandbox.CodeSandbox;
import com.sen.senoj.judge.codesandbox.CodeSandboxFactory;
import com.sen.senoj.judge.codesandbox.CodeSandboxProxy;
import com.sen.senoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.sen.senoj.judge.codesandbox.model.ExecuteCodeResponse;
import com.sen.senoj.judge.strategy.DefaultJudgeStrategy;
import com.sen.senoj.judge.strategy.JudgeContext;
import com.sen.senoj.model.dto.question.JudgeCase;
import com.sen.senoj.judge.codesandbox.model.JudgeInfo;
import com.sen.senoj.model.entity.Question;
import com.sen.senoj.model.entity.QuestionSubmit;
import com.sen.senoj.model.enums.QuestionSubmitStatusEnum;
import com.sen.senoj.service.QuestionService;
import com.sen.senoj.service.QuestionSubmitService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @ClassDescription:
 * @JdkVersion: 11
 * @Author: chensen
 * @Created: 2024/5/13 17:43
 */
@Service
public class JudgeServiceImpl implements JudgeService{

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private JudgeManager judgeManager;
    @Value("${codesandbox.type:example}")
    private String type;

    @Override
    public QuestionSubmit doJudge(long questionSubmitId) {
        //1、根据传入的题目id，获取对应的题目，提交信息，包含代码、编程语言
        QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
        if (questionSubmit == null){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"提交信息不存在");
        }
        Long questionId = questionSubmit.getQuestionId();
        Question question = questionService.getById(questionId);
        if (question == null){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"题目不存在");

        }
        //2、如果题目提交状态不为等待中，就不重复执行，读取等待中的题目信息去判题
        if (!questionSubmit.getStatus().equals(QuestionSubmitStatusEnum.WATTING.getValue())){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"题目判题中");
        }
        //3、更改题目状态，为判题中，防止重复执行
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean b = questionSubmitService.updateById(questionSubmitUpdate);
        if (!b){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"提目状态更新错误");
        }
        //4、调用沙箱，获取执行结果
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
        codeSandbox = new CodeSandboxProxy(codeSandbox);
        String language = questionSubmit.getLanguage();
        String code = questionSubmit.getCode();
            //获取输入用例
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        List<String> inputList = judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());
        ExecuteCodeRequest build = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(build);
        if (executeCodeResponse == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"代码沙箱出现问题");
        }
        List<String> outputList = executeCodeResponse.getOutputList();
        //5、根据沙箱的结果，设置题目判题状态以及信息
        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setJudgeInfo(executeCodeResponse.getJudgeInfo());
        judgeContext.setInputList(inputList);
        judgeContext.setOutputList(outputList);
        judgeContext.setQuestion(question);
        judgeContext.setJudgeCaseList(judgeCaseList);
        judgeContext.setQuestionSubmit(questionSubmit);
        JudgeInfo judgeInfo = judgeManager.doJudge(judgeContext);

        System.out.println("修数据库中的各判题结果");
        //修数据库中的各判题结果
        questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        b = questionSubmitService.updateById(questionSubmitUpdate);
        if (!b){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"提目状态更新错误");
        }
        System.out.println("");
        QuestionSubmit questionSubmitResult = questionSubmitService.getById(questionId);
        return questionSubmitResult;
    }
}
