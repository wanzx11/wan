package com.exmple.entity.vo.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopicTypeVo {
    Integer id;
    String name;
    String description;
    String color;
}
