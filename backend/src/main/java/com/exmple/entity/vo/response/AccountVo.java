package com.exmple.entity.vo.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
public class AccountVo {
     Integer id;
     String username;
     String email;
     String role;
     String avatar;
     Date registerTime;
}
