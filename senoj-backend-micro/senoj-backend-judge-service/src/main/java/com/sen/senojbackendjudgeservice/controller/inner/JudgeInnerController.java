package com.sen.senojbackendjudgeservice.controller.inner;

import com.sen.senojbackendjudgeservice.judge.JudgeService;
import com.sen.senojbackendmodel.entity.QuestionSubmit;
import com.sen.senojbackendserviceclient.service.JudgeFeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @ClassDescription:
 * @Author: chensen
 * @Created: 2024/5/27 17:00
 */
@RestController
@RequestMapping("/inner")
public class JudgeInnerController implements JudgeFeignClient {
    @Resource
    private JudgeService judgeService;


    @PostMapping("/do")
    @Override
    public QuestionSubmit doJudge(@RequestParam("questionSubmitId") long questionSubmitId){
        return judgeService.doJudge(questionSubmitId);
    }
}
