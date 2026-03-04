package com.springwork.controller;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.springwork.common.Result;
import com.springwork.entity.BlogCategory;
import com.springwork.service.BlogCategoryService;
import com.springwork.utils.ExcelUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 博客分类控制器（无分页版）
 * 对应 blog_category 表
 */
@RestController
@RequestMapping("/category")
public class BlogCategoryController {

    @Autowired
    private BlogCategoryService blogCategoryService;

    /**
     * 条件查询所有分类（无分页）
     * 支持按分类名称模糊查询、是否显示精准查询
     */
    @GetMapping("/list")
    public Result list(BlogCategory blogCategory) {
        QueryWrapper<BlogCategory> queryWrapper = new QueryWrapper<>();
        // 按分类名称模糊查询
        if (StringUtils.hasText(blogCategory.getName())) {
            queryWrapper.like("name", blogCategory.getName());
        }
        // 按是否显示精准查询（1=显示，0=隐藏）
        if (blogCategory.getIsShow() != null) {
            queryWrapper.eq("is_show", blogCategory.getIsShow());
        }
        // 按帖子数量降序（贴合小红书热门分类逻辑）
        queryWrapper.orderByDesc("post_count");

        List<BlogCategory> list = blogCategoryService.list(queryWrapper);
        return Result.success(list);
    }

    /**
     * 新增分类
     */
    @PostMapping
    public Result add(@RequestBody BlogCategory blogCategory) {
        // 初始化帖子数量为0（避免手动传值导致错误）
        blogCategory.setPostCount(0);
        blogCategoryService.save(blogCategory);
        return Result.success(blogCategory);
    }

    /**
     * 根据ID查询单个分类
     */
    @GetMapping("/{id}")
    public Result findOne(@PathVariable("id") Integer id) {
        BlogCategory blogCategory = blogCategoryService.getById(id);
        return Result.success(blogCategory);
    }

    /**
     * 修改分类
     */
    @PutMapping
    public Result update(@RequestBody BlogCategory blogCategory) {
        blogCategoryService.updateById(blogCategory);
        return Result.success(blogCategory);
    }

    /**
     * 根据ID删除分类
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable("id") Integer id) {
        blogCategoryService.removeById(id);
        return Result.success();
    }

    /**
     * 导出分类数据到Excel
     * 支持按条件导出
     */
    @RequestMapping("/export")
    public void export(HttpServletResponse response, BlogCategory blogCategory) throws IOException {
        QueryWrapper<BlogCategory> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(blogCategory.getName())) {
            queryWrapper.like("name", blogCategory.getName());
        }
        if (blogCategory.getIsShow() != null) {
            queryWrapper.eq("is_show", blogCategory.getIsShow());
        }
        queryWrapper.orderByDesc("post_count");

        List<BlogCategory> list = blogCategoryService.list(queryWrapper);
        ExcelUtils.export(response, list, "分类列表");
    }

    /**
     * 从Excel导入分类数据
     */
    @PostMapping("/import")
    public Result importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        ExcelReader reader = ExcelUtil.getReader(file.getInputStream());
        List<BlogCategory> list = reader.readAll(BlogCategory.class);
        Integer count = 0;
        for (BlogCategory item : list) {
            // 清空ID（避免导入时主键冲突），初始化帖子数量为0
            item.setId(null);
            item.setPostCount(0);
            blogCategoryService.save(item);
            count++;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("count", count);
        return Result.success(map);
    }
}