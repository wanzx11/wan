package com.exmple.entity.vo.request;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
public class ResetByEmailVo {
    @Email
    private String email;
    @Length(min = 6, max = 6)
    private String code;
    @Length(min = 6,max = 20)
    private String password;
}
