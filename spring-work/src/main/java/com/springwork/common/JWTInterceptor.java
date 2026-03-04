package com.springwork.common;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.springwork.Exception.CustomException;
import com.springwork.entity.BlogUser;
import com.springwork.service.BlogUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
public class JWTInterceptor implements HandlerInterceptor {

    @Autowired
    private BlogUserService blogUserService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放过option预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 检验token
        String token = request.getHeader("token");
        if (StrUtil.isBlank(token)) {
            token = request.getParameter("token");
        }

        // 没有token抛出异常
        if (StrUtil.isBlank(token)) {
            throw new CustomException(401, "无权操作,请登录");
        }

        BlogUser blogUser = null;
        // 解析token
        try {
            // 获取Token中存储的用户ID
            String audience = JWT.decode(token).getAudience().get(0);
            blogUser = blogUserService.getById(Integer.parseInt(audience));
        } catch (NumberFormatException e) {
            throw new CustomException(401, "Token 格式错误");
        } catch (Exception e) {
            throw new CustomException(401, "Token 无效或已过期");
        }

        // 校验用户是否存在
        if (blogUser == null) {
            throw new CustomException(401, "Token关联的用户不存在");
        }

        // 验证Token签名
        try {
            JWT.require(Algorithm.HMAC256(blogUser.getUsername())).build().verify(token);
        } catch (JWTVerificationException e) {
            throw new CustomException(401, "无权操作，Token验证失败");
        }

        return true;
    }
}