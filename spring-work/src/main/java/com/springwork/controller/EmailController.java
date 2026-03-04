package com.springwork.controller;

import com.springwork.common.Result;
import com.springwork.service.EmailSendService;
import com.springwork.service.impl.BlogUserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email")
public class EmailController {

    @Autowired
    private EmailSendService emailSendService;

    @Autowired
    private BlogUserServiceImpl blogUserServiceImpl;

    /**
     * 校验邮箱是否存在（修复参数名异常 + 统一返回Result）
     * @param email 待校验的邮箱
     * @return Result 统一返回格式
     */
    @GetMapping("/checkExist")
    // 核心修复：显式指定@RequestParam的name，解决参数名识别异常
    public Result checkEmailExist(@RequestParam(name = "email") String email) {
        boolean exists = blogUserServiceImpl.existsByEmail(email);
        if (exists) {
            // 使用Result.success重载方法，返回msg+默认code=200
            return Result.success(null, "邮箱存在");
        } else {
            // 明确指定错误码500，和前端约定统一
            return Result.error("该邮箱未注册，无法发送验证码", 500);
        }
    }

    /**
     * 发送邮箱验证码（统一返回Result，替代字符串JSON）
     * @param email 接收验证码的邮箱
     * @return Result 统一返回格式
     */
    @GetMapping("/sendCode")
    // 显式指定参数名，避免后续异常
    public Result sendCode(@RequestParam(name = "email") String email) {
        try {
            // 先校验邮箱是否存在，再发送验证码（可选，增强逻辑）
            boolean exists = blogUserServiceImpl.existsByEmail(email);
            if (!exists) {
                return Result.error("该邮箱未注册，无法发送验证码", 500);
            }
            // 发送验证码
            emailSendService.sendVerifyCodeEmail(email);
            return Result.success(null, "验证码发送成功");
        } catch (Exception e) {
            // 捕获异常并返回友好提示，避免暴露敏感信息
            return Result.error("验证码发送失败：" + e.getMessage(), 500);
        }
    }

    /**
     * 验证邮箱验证码（统一返回Result，替代字符串JSON）
     * @param email 邮箱
     * @param code 验证码
     * @return Result 统一返回格式
     */
    @PostMapping("/checkCode")
    // 显式指定参数名，规范写法
    public Result checkCode(
            @RequestParam(name = "email") String email,
            @RequestParam(name = "code") String code
    ) {
        try {
            boolean result = emailSendService.checkVerifyCode(email, code);
            if (result) {
                return Result.success(null, "验证通过");
            } else {
                return Result.error("验证码无效/过期", 500);
            }
        } catch (Exception e) {
            return Result.error("验证码验证失败：" + e.getMessage(), 500);
        }
    }
}