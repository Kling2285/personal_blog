package com.springwork.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.springwork.common.Result;
import com.springwork.entity.BlogUser;
import com.springwork.service.BlogUserService;
import com.springwork.service.EmailSendService;
import com.springwork.utils.PasswordEncoderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/register")
public class RegisterController {
    @Autowired
    private BlogUserService blogUserService;

    @Autowired
    private EmailSendService emailSendService;

    // ========== 对齐LoginController的byPassword/byEmail风格：邮箱注册 ==========
        @PostMapping("/byEmail")
        @Transactional
        public Result registerByEmail(@RequestBody Map<String, Object> map) {
            // 1. 提取参数（保留你原有写法）
            String username = map.get("username").toString();
            String rawPassword = map.get("password").toString(); // 明文密码
            String nickname = map.get("nickname").toString();
            String phone = map.get("phone").toString();
            String email = map.get("email").toString();
            String code = map.get("code").toString();

            // 2. 原有校验逻辑（完全保留）
            BlogUser existEmailUser = blogUserService.getOne(
                    new QueryWrapper<BlogUser>().eq("email", email)
            );
            if (existEmailUser != null) {
                return Result.error("该邮箱已注册，无法重复注册");
            }

            BlogUser existPhoneUser = blogUserService.getOne(
                    new QueryWrapper<BlogUser>().eq("phone", phone)
            );
            if (existPhoneUser != null) {
                return Result.error("该手机号已注册");
            }

            boolean codeValid = emailSendService.checkVerifyCode(email, code);
            if (!codeValid) {
                return Result.error("验证码无效/过期，请重新获取");
            }

            // 3. 核心：加密密码
            String encryptedPassword = PasswordEncoderUtil.encodePassword(rawPassword);

            // 4. 构建用户对象（密码替换为加密后的）
            BlogUser blogUser = new BlogUser();
            blogUser.setUsername(username);
            blogUser.setPassword(encryptedPassword); // 存加密密码，替换明文
            blogUser.setNickname(nickname);
            blogUser.setPhone(phone);
            blogUser.setEmail(email);
            blogUser.setUserType(1);
            blogUser.setStatus(0);
            blogUserService.save(blogUser);

            return Result.success(blogUser, "注册成功，请前往登录");
        }


    @PostMapping("/sendEmailCode")
    public Result sendRegisterEmailCode(@RequestBody Map<String, Object> map) {

        String email = map.get("email").toString();

        // 校验邮箱是否已注册（和上面注册逻辑复用同一套查询方式）
        BlogUser existEmailUser = blogUserService.getOne(
                new QueryWrapper<BlogUser>().eq("email", email)
        );
        if (existEmailUser != null) {
            return Result.error("该邮箱已注册，无需发送验证码");
        }

        try {
            // 复用已有发送验证码逻辑
            emailSendService.sendVerifyCodeEmail(email);
            return Result.success("验证码已发送至您的邮箱，有效期10分钟");
        } catch (Exception e) {
            return Result.error("验证码发送失败：" + e.getMessage());
        }
    }

}