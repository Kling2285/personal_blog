package com.springwork.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notice")
public class Notice {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    @TableField(value = "publish_time")
    private LocalDateTime publishTime;
}
