package com.springwork.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.springwork.entity.BlogCategory;
import com.springwork.mapper.BlogCategoryMapper;
import com.springwork.service.BlogCategoryService;
import org.springframework.stereotype.Service;

@Service
public class BlogCategoryServiceImpl extends ServiceImpl<BlogCategoryMapper, BlogCategory> implements BlogCategoryService {
}
