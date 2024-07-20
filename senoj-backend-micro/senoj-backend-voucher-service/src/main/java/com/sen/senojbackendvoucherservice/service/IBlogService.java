package com.sen.senojbackendvoucherservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sen.senojbackendvoucherservice.dto.Result;
import com.sen.senojbackendvoucherservice.entity.Post;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IBlogService extends IService<Post> {

    Result queryBlogById(Long id);

    Result queryHotBlog(Integer current);

    Result likeBlog(Long id);

    Result queryBlogLikes(Long id);

    Result saveBlog(Blog blog);


    Result queryBlogOfFollow(Long max, Integer offset);
}
