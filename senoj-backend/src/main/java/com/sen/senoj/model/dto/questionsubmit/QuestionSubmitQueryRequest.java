package com.sen.senoj.model.dto.questionsubmit;

import com.baomidou.mybatisplus.annotation.TableField;
import com.sen.senoj.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 *
 * 按照什么去查
 *
 * 创建提交提目的请求
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QuestionSubmitQueryRequest extends PageRequest implements Serializable {

    /**
     * 编程语言
     */
    private String language;

    /**
     * 用户代码
     */
    private Long userId;

    /**
     * 题目 id
     */
    private Long questionId;

    /**
     * 状态
     */
    private Integer status;

    private static final long serialVersionUID = 1L;
}