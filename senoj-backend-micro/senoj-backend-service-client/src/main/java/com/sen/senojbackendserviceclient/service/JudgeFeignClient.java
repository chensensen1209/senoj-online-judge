package com.sen.senojbackendserviceclient.service;


import com.sen.senojbackendmodel.entity.QuestionSubmit;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @ClassDescription:
 * @JdkVersion: 11
 * @Author: chensen
 * @Created: 2024/5/13 16:17
 */
@FeignClient(name = "senoj-backend-judge-service",path = "/api/judge/inner")

public interface JudgeFeignClient {

    @PostMapping("/do")
    QuestionSubmit doJudge(@RequestParam("questionSubmitId") long questionSubmitId);
}
