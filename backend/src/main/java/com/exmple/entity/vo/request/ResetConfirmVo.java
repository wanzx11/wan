package com.exmple.entity.vo.request;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
public class ResetConfirmVo {
    @Email
    private String email;
    @Length(min = 6, max = 6)
    private String code;
}
