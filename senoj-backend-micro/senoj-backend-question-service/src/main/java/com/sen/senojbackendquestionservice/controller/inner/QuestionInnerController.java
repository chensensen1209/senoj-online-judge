package com.sen.senojbackendquestionservice.controller.inner;

import com.sen.senojbackendmodel.entity.Question;
import com.sen.senojbackendmodel.entity.QuestionSubmit;
import com.sen.senojbackendquestionservice.service.QuestionService;
import com.sen.senojbackendquestionservice.service.QuestionSubmitService;
import com.sen.senojbackendserviceclient.service.QuestionFeignClient;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @ClassDescription:
 * @Author: chensen
 * @Created: 2024/5/27 16:48
 */
@RestController
@RequestMapping("/inner")
public class QuestionInnerController implements QuestionFeignClient {
    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionSubmitService questionSubmitService;
    @GetMapping("/get/id")
    @Override
    public Question getQuestionById(@RequestParam("questionId") long questionId){
        return questionService.getById(questionId);
    }

    @GetMapping("/question_submit/get/id")
    @Override
    public QuestionSubmit getQuestionSubmitById(@RequestParam("questionSubmitId") long questionSubmitId){
        return questionSubmitService.getById(questionSubmitId);
    }

    @PostMapping("/question_submit/update")
    @Override
    public boolean updateQuestionSubmitById(@RequestBody QuestionSubmit questionSubmit){
        return questionSubmitService.updateById(questionSubmit);
    }

    @PostMapping("/update/id")
    @Override
    public void updateQuestion(Question question) {
        questionService.updateById(question);
    }

}
