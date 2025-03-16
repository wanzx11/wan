package com.exmple.entity.vo.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class AddCommentVo {
    @Min(1)
    int tid;
    String content;
    @Min(-1)
    int quote;
}
