package com.exmple.entity.vo.request;

import com.alibaba.fastjson2.JSONObject;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class TopicCreateVo {
    Integer id;
    @Min(1)
    int type;
    @Length(min = 1,max = 40)
    String title;
    JSONObject content;
}
