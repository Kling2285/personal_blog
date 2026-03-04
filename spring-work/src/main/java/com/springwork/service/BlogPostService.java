package com.springwork.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.springwork.entity.BlogPost;

import java.util.List;

public interface BlogPostService extends IService<BlogPost> {
    List<BlogPost> selectRecommendList(Integer userId,int count);
}
