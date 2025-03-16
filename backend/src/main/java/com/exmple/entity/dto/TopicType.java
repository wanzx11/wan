package com.exmple.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("topic_type")
public class TopicType {
    @TableId(type = IdType.AUTO)
    Integer id;
    String name;
    String description;
    String color;
}
