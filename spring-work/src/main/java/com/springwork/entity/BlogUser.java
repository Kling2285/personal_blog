package com.springwork.entity;

import cn.hutool.core.annotation.Alias;
import cn.hutool.core.annotation.PropIgnore;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("blog_user")
public class BlogUser extends BaseEntity {
  @TableId(value = "user_id",type = IdType.AUTO)
  @Alias("用户Id")
  private Integer userId;
  @Alias("用户名")
  private String username;
  @Alias("登录密码")
  private String password;
  @Alias("昵称")
  private String nickname;
  @PropIgnore
  private String avatar;
  @Alias("用户邮箱")
  private String email;
  @Alias("电话")
  private String phone;
  @Alias("简介")
  private String intro;
  @Alias("用户类型")
  private Integer userType;
  @Alias("用户状态")
  private Integer status;
  @Alias("创建时间")
  private Date createTime;
  @Alias("更新时间")
  private Date updateTime;
  @Alias("上一次登录时间")
  private Date lastLoginTime;
  @TableField(exist = false)
  private String token;
}
