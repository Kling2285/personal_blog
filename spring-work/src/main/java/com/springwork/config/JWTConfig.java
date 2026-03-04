package com.springwork.config;

import com.springwork.common.JWTInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class JWTConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login/**",
                        "/register/**",
                        "/findPwd/**",
                        "/file/**",
                        "/api/**",
                        "/error",
                        "/post/**",
                        "/category/**",
                        "/user/**",
                        "/comment/**",
                        "/word/**"
                );
    }

    @Bean
    public JWTInterceptor jwtInterceptor(){
        return new JWTInterceptor();
    }
}