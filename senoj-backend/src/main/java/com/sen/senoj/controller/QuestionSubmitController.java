package com.sen.senoj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sen.senoj.common.BaseResponse;
import com.sen.senoj.common.ErrorCode;
import com.sen.senoj.common.ResultUtils;
import com.sen.senoj.exception.BusinessException;
import com.sen.senoj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.sen.senoj.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.sen.senoj.model.entity.Question;
import com.sen.senoj.model.entity.QuestionSubmit;
import com.sen.senoj.model.entity.User;
import com.sen.senoj.model.vo.QuestionSubmitVO;
import com.sen.senoj.service.QuestionSubmitService;
import com.sen.senoj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 题目提交接口
 *
 * @author <a href="https://github.com/lisen">程序员鱼皮</a>
 * @from <a href="https://sen.icu">编程导航知识星球</a>
 */
@RestController
//@RequestMapping("/question_submit")
@Slf4j
@Deprecated
public class QuestionSubmitController {
//    @Resource
//    private QuestionSubmitService questionSubmitService;
//
//    @Resource
//    private UserService userService;
//
//    /**
//     * 提交 / 取消提交
//     *
//     * @param questionSubmitAddRequest
//     * @param request
//     * @return resultNum 本次提交变化数
//     */
//    @PostMapping("/")
//    public BaseResponse<Integer> doQuestionSubmit(@RequestBody QuestionSubmitAddRequest questionSubmitAddRequest,
//                                                  HttpServletRequest request) {
//        if (questionSubmitAddRequest == null || questionSubmitAddRequest.getQuestionId() <= 0) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        // 登录才能提交
//        final User loginUser = userService.getLoginUser(request);
//        long questionId = questionSubmitAddRequest.getQuestionId();
//        int result = (int) questionSubmitService.doQuestionSubmit(questionSubmitAddRequest, loginUser);
//        return ResultUtils.success(result);
//    }
//
//    //分页获取提目提交列表，题目的提交信息
//    @PostMapping("/list/page")
//    public BaseResponse<Page<QuestionSubmitVO>> listQuestSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest,
//                                                                      HttpServletRequest request) {
//        long current = questionSubmitQueryRequest.getCurrent();
//        long size = questionSubmitQueryRequest.getPageSize();
//        Page<QuestionSubmit> questionSubmitPage = questionSubmitService.page(new Page<>(current, size),
//                questionSubmitService.getQueryWrapper(questionSubmitQueryRequest));
//        User loginUser = userService.getLoginUser(request);
//        return ResultUtils.success(questionSubmitService.getQuestionSubmitVOPage(questionSubmitPage, loginUser));
//    }

}
