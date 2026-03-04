package com.springwork.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("verify_code") // 对应数据库表名
public class VerifyCode {
    @TableId(type = IdType.AUTO)
    private Long id;          // 主键自增
    private String email;     // 收件人邮箱
    private String code;      // 6位验证码
    private LocalDateTime expireTime; // 过期时间（1分钟）
    private LocalDateTime createTime = LocalDateTime.now(); // 创建时间
    private Integer used = 0; // 是否使用（0=未用，1=已用）
}