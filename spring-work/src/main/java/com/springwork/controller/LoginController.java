package com.springwork.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.springwork.common.Result;
import com.springwork.entity.BlogUser;
import com.springwork.service.BlogUserService;
import com.springwork.service.EmailSendService; // 导入验证码服务
import com.springwork.utils.PasswordEncoderUtil;
import com.springwork.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login") // 统一前缀，规范接口路径
public class LoginController {
    @Autowired
    private BlogUserService blogUserService;

    @Autowired
    private EmailSendService emailSendService; // 注入验证码校验服务


    @PostMapping("/byPassword")
    public Result loginByPassword(@RequestBody Map<String, Object> map) {
        // 1. 新增：空值校验（避免NPE）
        if (map.get("username") == null || map.get("password") == null) {
            return Result.error("用户名或密码不能为空");
        }

        // 2. 新增：类型转换+去空格（避免空字符串/空格问题）
        String username = map.get("username").toString().trim();
        String rawPwd = map.get("password").toString().trim();

        // 3. 新增：空字符串校验
        if (username.isEmpty() || rawPwd.isEmpty()) {
            return Result.error("用户名或密码不能为空");
        }

        // 单个查询（按用户名）- 优化：复用已处理的username变量
        BlogUser blogUser = blogUserService.getOne(
                new QueryWrapper<BlogUser>()
                        .eq("username", username)
                        .last("LIMIT 1")
        );
        // 比对
        if (blogUser != null) {
            if (!PasswordEncoderUtil.matchPassword(rawPwd, blogUser.getPassword())) {
                return Result.error("密码错误");
            }
            blogUser.setPassword(null); // 新增：脱敏，不返回密码
            //生成token
           String token=TokenUtils.createToken(blogUser.getUserId()+"",blogUser.getUsername());
            blogUser.setToken(token);
            return Result.success(blogUser);
        }
        return Result.error("账号不存在");
    }


    @PostMapping("/byEmail")
    public Result loginByEmail(@RequestBody Map<String, Object> map) {
        String email = map.get("email").toString();

        // 直接按邮箱查询用户
        BlogUser blogUser = blogUserService.getOne(
                new QueryWrapper<BlogUser>().eq("email", email)
        );

        if (blogUser == null) {
            return Result.error("该邮箱未注册，无法登录");
        }
        //生成token
        String token=TokenUtils.createToken(blogUser.getUserId()+"",blogUser.getUsername());
        blogUser.setToken(token);
        // 登录成功，返回用户信息
        return Result.success(blogUser, "登录成功");
    }

}