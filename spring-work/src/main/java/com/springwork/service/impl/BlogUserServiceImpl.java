package com.springwork.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.springwork.entity.BlogUser;
import com.springwork.mapper.BlogUserMapper;
import com.springwork.service.BlogUserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class BlogUserServiceImpl extends ServiceImpl<BlogUserMapper, BlogUser> implements BlogUserService {
    @Resource
    private BlogUserMapper blogUserMapper;
    /**
     * 校验邮箱是否存在（MyBatis-Plus 实现）
     * @param email 待校验的邮箱
     * @return true=存在，false=不存在
     */
    public boolean existsByEmail(String email) {
        // 构造查询条件
        LambdaQueryWrapper<BlogUser> queryWrapper = new LambdaQueryWrapper<BlogUser>()
                .eq(BlogUser::getEmail, email);

        // MP的exists方法：判断是否存在符合条件的记录
        return blogUserMapper.exists(queryWrapper);
    }
}
