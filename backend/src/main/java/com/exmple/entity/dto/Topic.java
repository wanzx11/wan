package com.exmple.entity.dto;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;


@Data
@TableName("topic")
public class Topic {
    @TableId(type = IdType.AUTO)
    Integer id;
    String title;
    String content;
    Integer uid;
    Integer type;
    Date time;
}
