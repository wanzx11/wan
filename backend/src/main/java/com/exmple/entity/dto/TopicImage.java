package com.exmple.entity.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;


@Data
@AllArgsConstructor
@TableName("topic_image")
public class TopicImage {
    Integer uid;
    String name;
    Date time;
}
