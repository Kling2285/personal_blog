package com.springwork.entity;

import cn.hutool.core.annotation.PropIgnore;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

@Data
public class BaseEntity implements Serializable {
    @TableField(exist=false)
    @PropIgnore
    private Integer pageNum=1;
    @TableField(exist=false)
    @PropIgnore
    private Integer pageSize=10;
}
