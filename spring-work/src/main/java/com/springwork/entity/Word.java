package com.springwork.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("word")
public class Word {

    @TableId(type = IdType.AUTO) // 主键自增，匹配表的AUTO_INCREMENT
    private Integer id;

    private String word;

    @TableField(updateStrategy = com.baomidou.mybatisplus.annotation.FieldStrategy.IGNORED)
    private String info; // 非必填字段，赋值为null时不参与更新操作
}
