package com.springwork.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.springwork.common.Result;
import com.springwork.entity.Comment;
import com.springwork.service.CommentService;
import com.springwork.utils.DFAUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 评论控制器
 * 适配MyBatis-Plus，无分页功能
 */
@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;
    @Autowired
    private DFAUtil dfaUtil;

    /**
     * 查询所有评论
     */
    @GetMapping("/list")
    public Result list() {
        List<Comment> list = commentService.list();
        return Result.success(list);
    }

    /**
     * 条件查询评论
     * 支持按帖子ID、用户ID、父评论ID筛选
     */
    @GetMapping("/list01")
    public Result list01(Comment comment) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        // 按帖子ID精准查询
        if (comment.getPostId() != null) {
            queryWrapper.eq("post_id", comment.getPostId());
        }
        // 按用户ID精准查询
        if (comment.getUserId() != null) {
            queryWrapper.eq("user_id", comment.getUserId());
        }
        // 按父评论ID精准查询（查某条评论的所有回复）
        if (comment.getParentId() != null) {
            queryWrapper.eq("parent_id", comment.getParentId());
        }
        // 按评论内容模糊查询（可选）
        if (StringUtils.hasText(comment.getContent())) {
            queryWrapper.like("content", comment.getContent());
        }
        // 按创建时间降序排列（最新评论在前）
        queryWrapper.orderByDesc("create_time");

        List<Comment> list = commentService.list(queryWrapper);
        return Result.success(list);
    }

    /**
     * 新增评论
     */
    @PostMapping
    public Result add(@RequestBody Comment comment) {
        // 1. 设置创建时间
        comment.setCreateTime(LocalDateTime.now());

        // 2. 获取评论原始内容，做非空校验
        String originalContent = comment.getContent();
        if (!StringUtils.hasText(originalContent)) {
            return Result.error("评论内容不能为空");
        }

        String filteredContent = dfaUtil.replaceSensitiveWord(originalContent);
        comment.setContent(filteredContent);

        boolean save = commentService.save(comment);
        if (save) {
            return Result.success("评论提交成功");
        } else {
            return Result.error("评论提交失败");
        }
    }

    /**
     * 根据ID查询单条评论
     */
    @GetMapping("/{id}")
    public Result findOne(@PathVariable("id") Long id) {
        Comment comment = commentService.getById(id);
        return Result.success(comment);
    }
    /**
     * 根据ID删除评论
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable("id") Long id) {
        commentService.removeById(id);
        return Result.success();
    }
}