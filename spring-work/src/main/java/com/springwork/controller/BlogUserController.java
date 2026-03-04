package com.springwork.controller;


import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.springwork.common.Result;
import com.springwork.entity.BlogUser;
import com.springwork.service.BlogUserService;
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

@RestController
@RequestMapping("/user")
public class BlogUserController {

    @Autowired
    private BlogUserService blogUserService;

    @GetMapping("/list")
    public Result list() {
        List<BlogUser> list = blogUserService.list();
        return Result.success(list);
    }

    @GetMapping("/list01")
    public Result list01(BlogUser blogUser) {
        QueryWrapper<BlogUser> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(blogUser.getUsername())) {
            queryWrapper.like("username", blogUser.getUsername());
        }
        if (StringUtils.hasText(blogUser.getNickname())) {
            queryWrapper.like("nickname", blogUser.getNickname());
        }
        if (blogUser.getStatus() != null) {
            queryWrapper.eq("status", blogUser.getStatus());
        }

        Page<BlogUser> page = new Page<>(blogUser.getPageNum(), blogUser.getPageSize());
        blogUserService.page(page, queryWrapper);

        Map<String,Object> map = new HashMap<>();
        map.put("list", page.getRecords());
        map.put("total", page.getTotal());
        return Result.success(map);
    }

    @PostMapping
    public Result add(@RequestBody BlogUser blogUser) {
        blogUserService.save(blogUser);
        return Result.success(blogUser);
    }

    @GetMapping("/{userId}")
    public Result findOne(@PathVariable("userId") Integer userId) {
        BlogUser blogUser = blogUserService.getById(userId);
        return Result.success(blogUser);
    }

    @PutMapping
    public Result update(@RequestBody BlogUser blogUser) {
        blogUserService.updateById(blogUser);
        return Result.success(blogUser);
    }

    @DeleteMapping("/{userId}")
    public Result delete(@PathVariable("userId") Integer userId) {
        blogUserService.removeById(userId);
        return Result.success();
    }


    //导出Excel
    @RequestMapping("/export")
    public void export(HttpServletResponse response,BlogUser blogUser) throws IOException {
        QueryWrapper<BlogUser> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(blogUser.getUsername())) {
            queryWrapper.like("username", blogUser.getUsername());
        }
        if (StringUtils.hasText(blogUser.getNickname())) {
            queryWrapper.like("nickname", blogUser.getNickname());
        }
        if (blogUser.getStatus() != null) {
            queryWrapper.eq("status", blogUser.getStatus());
        }

        List<BlogUser> list = blogUserService.list(queryWrapper);
      ExcelUtils.export(response,list,"用户列表");
    }

    //导入Excel
    @PostMapping("/import")
    public Result importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        ExcelReader reader = ExcelUtil.getReader(file.getInputStream());
        List<BlogUser> list=reader.readAll(BlogUser.class);
        Integer count=0;
        for (BlogUser item : list) {
            item.setUserId(null);
            blogUserService.save(item);
            count++;
        }
        Map<String,Object> map=new HashMap<>();
        map.put("count",count);
        return Result.success(map);
    }


}
