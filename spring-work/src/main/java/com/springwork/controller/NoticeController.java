package com.springwork.controller;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.springwork.common.Result;
import com.springwork.entity.Notice;
import com.springwork.service.NoticeService;
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
@RequestMapping("/notice")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    /**
     * 查询所有公告（按发布时间降序，最新在前）
     */
    @GetMapping("/list")
    public Result list() {
        QueryWrapper<Notice> queryWrapper = new QueryWrapper<>();
        // 按发布时间降序（为空则按创建时间降序）
        queryWrapper.orderByDesc("publish_time", "create_time");
        List<Notice> list = noticeService.list(queryWrapper);
        return Result.success(list);
    }

    /**
     * 条件查询公告（按发布时间降序，最新在前）
     * 支持按标题模糊查询
     */
    @GetMapping("/list01")
    public Result list01(Notice notice) {
        QueryWrapper<Notice> queryWrapper = new QueryWrapper<>();
        // 标题模糊查询
        if (StringUtils.hasText(notice.getTitle())) {
            queryWrapper.like("title", notice.getTitle());
        }
        // 按发布时间降序，无分页
        queryWrapper.orderByDesc("publish_time", "create_time");

        List<Notice> list = noticeService.list(queryWrapper);
        Map<String, Object> map = new HashMap<>();
        map.put("list", list);
        map.put("total", list.size()); // 无分页，总数为列表长度
        return Result.success(map);
    }

    /**
     * 新增公告
     */
    @PostMapping
    public Result add(@RequestBody Notice notice) {
        noticeService.save(notice);
        return Result.success(notice);
    }

    /**
     * 根据ID查询单条公告
     */
    @GetMapping("/{id}")
    public Result findOne(@PathVariable("id") Long id) {
        Notice notice = noticeService.getById(id);
        return Result.success(notice);
    }

    /**
     * 修改公告
     */
    @PutMapping
    public Result update(@RequestBody Notice notice) {
        noticeService.updateById(notice);
        return Result.success(notice);
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable("id") Long id) {
        noticeService.removeById(id);
        return Result.success();
    }

    /**
     * 导出Excel（按条件筛选，按发布时间降序）
     */
    @RequestMapping("/export")
    public void export(HttpServletResponse response, Notice notice) throws IOException {
        QueryWrapper<Notice> queryWrapper = new QueryWrapper<>();
        // 标题模糊筛选
        if (StringUtils.hasText(notice.getTitle())) {
            queryWrapper.like("title", notice.getTitle());
        }
        // 按发布时间降序（最新在前）
        queryWrapper.orderByDesc("publish_time", "create_time");

        List<Notice> list = noticeService.list(queryWrapper);
        ExcelUtils.export(response, list, "公告列表");
    }

    /**
     * 导入Excel
     */
    @PostMapping("/import")
    public Result importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        ExcelReader reader = ExcelUtil.getReader(file.getInputStream());
        List<Notice> list = reader.readAll(Notice.class);
        Integer count = 0;
        for (Notice item : list) {
            // 清空主键，避免导入时主键冲突
            item.setId(null);
            noticeService.save(item);
            count++;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("count", count);
        return Result.success(map);
    }
}
