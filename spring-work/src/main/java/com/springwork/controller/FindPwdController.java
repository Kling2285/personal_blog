package com.springwork.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.springwork.common.Result;
import com.springwork.entity.BlogUser;
import com.springwork.service.BlogUserService;
import com.springwork.service.EmailSendService;
import com.springwork.utils.PasswordEncoderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 密码找回专用控制器
 * 复用EmailController的核心逻辑，避免代码冗余
 */
@RestController
@RequestMapping("/findPwd") // 独立前缀，与登录/邮箱接口区分
public class FindPwdController {

    @Autowired
    private BlogUserService blogUserService;

    @Autowired
    private EmailSendService emailSendService;

    /**
     * 1. 发送找回密码的邮箱验证码（复用EmailController的校验+发送逻辑）
     * 请求URL：/findPwd/sendEmailCode
     * 请求参数：{ "email": "xxx@xxx.com" }
     */
    @PostMapping("/sendEmailCode")
    public Result sendFindPwdEmailCode(@RequestBody Map<String, Object> map) {
        // 1. 校验参数非空（严谨性）
        if (map.get("email") == null || map.get("email").toString().trim().isEmpty()) {
            return Result.error("邮箱不能为空", 500);
        }
        String email = map.get("email").toString().trim();

        // 2. 校验邮箱是否已注册
        boolean emailExists = blogUserService.exists(new QueryWrapper<BlogUser>().eq("email", email));
        if (!emailExists) {
            return Result.error("该邮箱未注册，无法找回密码", 500);
        }

        // 3. 发送验证码
        try {
            emailSendService.sendVerifyCodeEmail(email);
            return Result.success(null, "验证码已发送至您的邮箱，有效期10分钟");
        } catch (Exception e) {
            // 和EmailController的异常处理逻辑一致，返回友好提示
            return Result.error("验证码发送失败：" + e.getMessage(), 500);
        }
    }

    /**
     * 2. 验证找回密码的邮箱验证码（复用EmailController的checkCode逻辑）
     * 请求URL：/findPwd/checkEmailCode
     * 请求参数：{ "email": "xxx@xxx.com", "code": "123456" }
     */
    @PostMapping("/checkEmailCode")
    public Result checkEmailCode(@RequestBody Map<String, Object> map) {
        // 1. 校验参数非空
        if (map.get("email") == null || map.get("email").toString().trim().isEmpty()) {
            return Result.error("邮箱不能为空", 500);
        }
        if (map.get("code") == null || map.get("code").toString().trim().isEmpty()) {
            return Result.error("验证码不能为空", 500);
        }
        String email = map.get("email").toString().trim();
        String code = map.get("code").toString().trim();

        // 2. 验证验证码
        try {
            boolean isValid = emailSendService.checkVerifyCode(email, code);
            if (isValid) {
                return Result.success(null, "验证码验证通过");
            } else {
                return Result.error("验证码无效/过期", 500);
            }
        } catch (Exception e) {
            return Result.error("验证码验证失败：" + e.getMessage(), 500);
        }
    }

    /**
     * 3. 重置密码（固定重置为123456，新增核心逻辑）
     * 请求URL：/findPwd/resetByEmail
     * 请求参数：{ "email": "xxx@xxx.com" }
     */
    @PostMapping("/resetByEmail")
    public Result resetPwdByEmail(@RequestBody Map<String, Object> map) {
        // 1. 校验参数非空
        if (map.get("email") == null || map.get("email").toString().trim().isEmpty()) {
            return Result.error("邮箱不能为空", 500);
        }
        String email = map.get("email").toString().trim();
        String newPwd = "123456";
        String encryptedPwd = PasswordEncoderUtil.encodePassword(newPwd);

        // 2. 再次校验邮箱是否注册（防止验证码验证后邮箱被删除的极端情况）
        boolean emailExists = blogUserService.exists(new QueryWrapper<BlogUser>().eq("email", email));
        if (!emailExists) {
            return Result.error("该邮箱未注册，无法重置密码", 500);
        }

        // 3. 更新密码（严谨的更新逻辑）
        LambdaUpdateWrapper<BlogUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(BlogUser::getEmail, email)
                .set(BlogUser::getPassword, encryptedPwd) // 存加密后的密码
                .set(BlogUser::getUpdateTime, new java.util.Date());

        // 4. 执行更新并返回结果（和业务逻辑一致的返回格式）
        boolean updateSuccess = blogUserService.update(updateWrapper);
        if (updateSuccess) {
            return Result.success(null, "密码重置成功！新密码为123456，请及时修改");
        } else {
            return Result.error("密码重置失败，请稍后重试", 500);
        }
    }
}