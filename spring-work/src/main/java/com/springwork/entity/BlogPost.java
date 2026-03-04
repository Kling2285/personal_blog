package com.springwork.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("blog_post") // 指定对应表名
public class BlogPost extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String title;
    private String content;
    private Integer categoryId;
    private Long userId;
    private String coverImg;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Date createTime;
    @TableField("is_show")
    private Integer isShow;
}