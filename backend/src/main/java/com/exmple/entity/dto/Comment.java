package com.exmple.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("topic_comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    Integer id;
    Integer tid;
    Integer uid;
    String content;
    Integer quote;
    Date time;
}
