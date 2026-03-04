package com.springwork.utils;

import cn.hutool.poi.excel.ExcelWriter;
import cn.hutool.poi.excel.ExcelUtil;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ExcelUtils {
    /**
     * 通用Excel导出工具
     * @param response 响应对象
     * @param list 导出数据列表
     * @param fileName 导出文件名（前端传入的原始名称，如"用户列表"）
     */
    public static void export(HttpServletResponse response, List<?> list, String fileName) {
        // 空值校验：避免空指针
        if (list == null) {
            throw new IllegalArgumentException("导出数据列表不能为空");
        }
        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = "导出数据"; // 默认文件名
        }

        ExcelWriter writer = null;
        try {
            writer = ExcelUtil.getWriter();
            writer.write(list, true);

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
            String encodeFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + encodeFileName + ".xlsx;filename*=UTF-8''" + encodeFileName + ".xlsx");
            // 禁用缓存
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);

            // 4. 输出流操作
            try (ServletOutputStream os = response.getOutputStream()) {
                writer.flush(os, true);
            }
        } catch (IOException e) {
            throw new RuntimeException("Excel导出失败：" + e.getMessage(), e); // 补充错误信息
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}