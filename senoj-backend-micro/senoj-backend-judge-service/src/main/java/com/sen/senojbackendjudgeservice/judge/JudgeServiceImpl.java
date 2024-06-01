package com.sen.senojbackendjudgeservice.judge;

import cn.hutool.json.JSONUtil;
import com.sen.senojbackendcommon.common.ErrorCode;
import com.sen.senojbackendcommon.exception.BusinessException;
import com.sen.senojbackendjudgeservice.judge.codesandbox.CodeSandbox;
import com.sen.senojbackendjudgeservice.judge.codesandbox.CodeSandboxFactory;
import com.sen.senojbackendjudgeservice.judge.codesandbox.CodeSandboxProxy;
import com.sen.senojbackendjudgeservice.judge.strategy.JudgeContext;
import com.sen.senojbackendmodel.codesandbox.ExecuteCodeRequest;
import com.sen.senojbackendmodel.codesandbox.ExecuteCodeResponse;
import com.sen.senojbackendmodel.codesandbox.JudgeInfo;
import com.sen.senojbackendmodel.dto.question.JudgeCase;
import com.sen.senojbackendmodel.entity.Question;
import com.sen.senojbackendmodel.entity.QuestionSubmit;
import com.sen.senojbackendmodel.enums.JudgeInfoMessageEnum;
import com.sen.senojbackendmodel.enums.QuestionSubmitStatusEnum;
import com.sen.senojbackendserviceclient.service.QuestionFeignClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassDescription:
 * @JdkVersion: 11
 * @Author: chensen
 * @Created: 2024/5/13 17:43
 */
@Service
public class JudgeServiceImpl implements JudgeService{

    @Resource
    private QuestionFeignClient questionFeignClient;


    @Resource
    private JudgeManager judgeManager;
    @Value("${codesandbox.type:example}")
    private String type;

    @Override
    public QuestionSubmit doJudge(long questionSubmitId) {
        //1、根据传入的题目id，获取对应的题目，提交信息，包含代码、编程语言
        QuestionSubmit questionSubmit = questionFeignClient.getQuestionSubmitById(questionSubmitId);
        if (questionSubmit == null){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"提交信息不存在");
        }
        Long questionId = questionSubmit.getQuestionId();
        Question question = questionFeignClient.getQuestionById(questionId);
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
        boolean b = questionFeignClient.updateQuestionSubmitById(questionSubmitUpdate);
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

        JudgeInfo judgeInfo = new JudgeInfo();
        if (executeCodeResponse.getStatus() == 2) {
            judgeInfo.setMemory(0L);
            judgeInfo.setTime(0L);
            judgeInfo.setMessage(String.valueOf(JudgeInfoMessageEnum.COMPILE_ERROR));
            QuestionSubmit questionSubmitRes = updateQuestion(question, judgeInfo, questionSubmitId, questionId);
            return questionSubmitRes;
        }
        if (executeCodeResponse.getStatus() == 3) {
            judgeInfo.setMemory(0L);
            judgeInfo.setTime(0L);
            judgeInfo.setMessage(String.valueOf(JudgeInfoMessageEnum.RUNTIME_ERROR));
            QuestionSubmit questionSubmitRes = updateQuestion(question, judgeInfo, questionSubmitId, questionId);
            return questionSubmitRes;
        }
        judgeInfo = judgeManager.doJudge(judgeContext);
        System.out.println("修数据库中的各判题结果");
        //6|修数据库中的各判题结果
        question.setSubmitNum(question.getSubmitNum()+1);
        if (JudgeInfoMessageEnum.ACCEPTED.getValue().equals(judgeInfo.getMessage())){
            question.setAcceptedNum(question.getAcceptedNum()+1);
        }
        questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        questionFeignClient.updateQuestion(question);
        b = questionFeignClient.updateQuestionSubmitById(questionSubmitUpdate);
        if (!b){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"提目状态更新错误");
        }
        System.out.println("");
        QuestionSubmit questionSubmitResult = questionFeignClient.getQuestionSubmitById(questionId);
        return questionSubmitResult;
    }

    private QuestionSubmit updateQuestion(Question question, JudgeInfo judgeInfo,Long questionSubmitId,Long questionId){
        question.setSubmitNum(question.getSubmitNum()+1);
        if (JudgeInfoMessageEnum.ACCEPTED.getValue().equals(judgeInfo.getMessage())){
            question.setAcceptedNum(question.getAcceptedNum()+1);
        }
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        questionFeignClient.updateQuestion(question);
        boolean b = questionFeignClient.updateQuestionSubmitById(questionSubmitUpdate);
        if (!b){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"提目状态更新错误");
        }
        System.out.println("");
        QuestionSubmit questionSubmitResult = questionFeignClient.getQuestionSubmitById(questionId);
        return questionSubmitResult;
    }
}
