package com.springwork.controller;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.auth0.jwt.JWT;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.springwork.common.Result;
import com.springwork.entity.BlogPost;
import com.springwork.service.BlogPostService;
import com.springwork.utils.ExcelUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/post")
public class BlogPostController {

    @Autowired
    private BlogPostService blogPostService;

    @GetMapping("/list")
    public Result list() {
        List<BlogPost> list = blogPostService.list();
        return Result.success(list);
    }

    @GetMapping("/list01")
    public Result list01(BlogPost blogPost) {
        QueryWrapper<BlogPost> queryWrapper = new QueryWrapper<>();
        // 标题模糊查询
        if (StringUtils.hasText(blogPost.getTitle())) {
            queryWrapper.like("title", blogPost.getTitle());
        }
        // 分类ID精确查询
        if (blogPost.getCategoryId() != null) {
            queryWrapper.eq("category_id", blogPost.getCategoryId());
        }
        // 用户ID精确查询
        if (blogPost.getUserId() != null) {
            queryWrapper.eq("user_id", blogPost.getUserId());
        }
        // 新增：是否显示精确查询（is_show=1 查显示，is_show=0 查隐藏）
        if (blogPost.getIsShow() != null) {
            queryWrapper.eq("is_show", blogPost.getIsShow());
        } else {
            // 可选：未传is_show时，默认只查显示的帖子（is_show=1）
            queryWrapper.eq("is_show", 1);
        }
        // 按创建时间降序排列
        queryWrapper.orderByDesc("create_time");

        // 分页查询（继承BaseEntity的pageNum/pageSize）
        Page<BlogPost> page = new Page<>(blogPost.getPageNum(), blogPost.getPageSize());
        blogPostService.page(page, queryWrapper);

        // 封装分页结果
        Map<String, Object> map = new HashMap<>();
        map.put("list", page.getRecords());
        map.put("total", page.getTotal());
        return Result.success(map);
    }


    /**
     * 随机推荐
     */
    @GetMapping("/list-random")
    public Result listRandom(
            HttpServletRequest request,
            @RequestParam(defaultValue = "30") int count // 新增：从前端接收count，默认20
    ) {
        String token = request.getHeader("token");
        Integer userid = null;
        if(token != null) {
            String s= JWT.decode(token).getAudience().get(0);
            userid = Integer.parseInt(s);
        }
        List<BlogPost> recommendList = blogPostService.selectRecommendList(userid, count);

        Map<String, Object> map = new HashMap<>();
        map.put("list", recommendList);
        map.put("total", recommendList.size());

        return Result.success(map);
    }

    /**
     * 新增帖子
     */
    @PostMapping
    public Result add(@RequestBody BlogPost blogPost) {
        blogPostService.save(blogPost);
        return Result.success(blogPost);
    }

    /**
     * 根据ID查询单个帖子
     */
    @GetMapping("/{id}")
    public Result findOne(@PathVariable("id") Integer id) {
        BlogPost blogPost = blogPostService.getById(id);
        return Result.success(blogPost);
    }

    /**
     * 修改帖子
     */
    @PutMapping
    public Result update(@RequestBody BlogPost blogPost) {
        blogPostService.updateById(blogPost);
        return Result.success(blogPost);
    }

    /**
     * 根据ID删除帖子
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable("id") Integer id) {
        blogPostService.removeById(id);
        return Result.success();
    }

    /**
     * 导出帖子数据到Excel
     */
    @RequestMapping("/export")
    public void export(HttpServletResponse response, BlogPost blogPost) throws IOException {
        QueryWrapper<BlogPost> queryWrapper = new QueryWrapper<>();
        // 导出条件和分页查询一致
        if (StringUtils.hasText(blogPost.getTitle())) {
            queryWrapper.like("title", blogPost.getTitle());
        }
        if (blogPost.getCategoryId() != null) {
            queryWrapper.eq("category_id", blogPost.getCategoryId());
        }
        if (blogPost.getUserId() != null) {
            queryWrapper.eq("user_id", blogPost.getUserId());
        }
        queryWrapper.orderByDesc("create_time");

        List<BlogPost> list = blogPostService.list(queryWrapper);
        ExcelUtils.export(response, list, "帖子列表");
    }

    /**
     * 从Excel导入帖子数据
     */
    @PostMapping("/import")
    public Result importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        ExcelReader reader = ExcelUtil.getReader(file.getInputStream());
        List<BlogPost> list = reader.readAll(BlogPost.class);
        Integer count = 0;
        for (BlogPost item : list) {
            // 清空主键ID，避免导入时主键冲突
            item.setId(null);
            // 初始化默认值（浏览量/点赞数/评论数默认0）
            if (item.getViewCount() == null) {
                item.setViewCount(0);
            }
            if (item.getLikeCount() == null) {
                item.setLikeCount(0);
            }
            if (item.getCommentCount() == null) {
                item.setCommentCount(0);
            }
            blogPostService.save(item);
            count++;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("count", count);
        return Result.success(map);
    }
}
