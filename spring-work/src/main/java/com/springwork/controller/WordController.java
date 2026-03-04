package com.springwork.controller;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.springwork.common.Result;
import com.springwork.entity.Word;
import com.springwork.service.WordService;
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
 * 敏感词控制器（无分页，模仿BlogUserController风格）
 * 对应表：word
 */
@RestController
@RequestMapping("/word")
public class WordController {

    @Autowired
    private WordService wordService;

    /**
     * 查询所有敏感词（无分页）
     */
    @GetMapping("/list")
    public Result list() {
        List<Word> list = wordService.list();
        return Result.success(list);
    }

    /**
     * 条件查询敏感词（无分页）
     * 支持按敏感词内容、说明模糊查询
     */
    @GetMapping("/list01")
    public Result list01(Word word) {
        QueryWrapper<Word> queryWrapper = new QueryWrapper<>();
        // 敏感词内容模糊查询
        if (StringUtils.hasText(word.getWord())) {
            queryWrapper.like("word", word.getWord());
        }
        // 敏感词说明模糊查询
        if (StringUtils.hasText(word.getInfo())) {
            queryWrapper.like("info", word.getInfo());
        }
        // 按ID升序排列
        queryWrapper.orderByAsc("id");

        List<Word> list = wordService.list(queryWrapper);

        Map<String, Object> map = new HashMap<>();
        map.put("list", list);
        map.put("total", list.size()); // 无分页，total为查询结果总数
        return Result.success(map);
    }

    /**
     * 新增敏感词
     */
    @PostMapping
    public Result add(@RequestBody Word word) {
        // 校验敏感词是否已存在（避免唯一索引冲突）
        QueryWrapper<Word> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("word", word.getWord());
        if (wordService.count(queryWrapper) > 0) {
            return Result.error("该敏感词已存在，无需重复添加");
        }
        wordService.save(word);
        return Result.success(word);
    }

    /**
     * 根据ID查询单个敏感词
     */
    @GetMapping("/{id}")
    public Result findOne(@PathVariable("id") Integer id) {
        Word word = wordService.getById(id);
        return Result.success(word);
    }

    /**
     * 修改敏感词
     */
    @PutMapping
    public Result update(@RequestBody Word word) {
        // 校验修改后的敏感词是否与其他记录重复
        QueryWrapper<Word> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("word", word.getWord());
        queryWrapper.ne("id", word.getId()); // 排除当前修改的记录
        if (wordService.count(queryWrapper) > 0) {
            return Result.error("该敏感词已存在，修改失败");
        }
        wordService.updateById(word);
        return Result.success(word);
    }

    /**
     * 根据ID删除敏感词
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable("id") Integer id) {
        wordService.removeById(id);
        return Result.success();
    }

    /**
     * 导出敏感词数据到Excel
     */
    @RequestMapping("/export")
    public void export(HttpServletResponse response, Word word) throws IOException {
        QueryWrapper<Word> queryWrapper = new QueryWrapper<>();
        // 导出条件和条件查询一致
        if (StringUtils.hasText(word.getWord())) {
            queryWrapper.like("word", word.getWord());
        }
        if (StringUtils.hasText(word.getInfo())) {
            queryWrapper.like("info", word.getInfo());
        }
        queryWrapper.orderByAsc("id");

        List<Word> list = wordService.list(queryWrapper);
        ExcelUtils.export(response, list, "敏感词列表");
    }

    /**
     * 从Excel导入敏感词数据
     */
    @PostMapping("/import")
    public Result importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        ExcelReader reader = ExcelUtil.getReader(file.getInputStream());
        List<Word> list = reader.readAll(Word.class);
        Integer successCount = 0;
        Integer failCount = 0;

        for (Word item : list) {
            // 清空主键ID，避免导入时主键冲突
            item.setId(null);
            // 校验敏感词是否已存在
            QueryWrapper<Word> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("word", item.getWord());
            if (wordService.count(queryWrapper) > 0) {
                failCount++;
                continue;
            }
            wordService.save(item);
            successCount++;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("successCount", successCount); // 成功导入数量
        map.put("failCount", failCount);       // 失败数量（重复敏感词）
        return Result.success(map);
    }
}