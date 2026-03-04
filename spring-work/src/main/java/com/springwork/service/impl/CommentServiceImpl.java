package com.springwork.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.springwork.entity.Comment;
import com.springwork.mapper.CommentMapper;
import com.springwork.service.CommentService;
import org.springframework.stereotype.Service;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
}
