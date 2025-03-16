package com.exmple.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.exmple.entity.dto.Account;
import com.exmple.entity.vo.request.*;
import org.springframework.security.core.userdetails.UserDetailsService;


public interface AccountService extends IService<Account>, UserDetailsService {
     Account getAccountByNameOrEmail(String text);

     String registerEmailCode(String type,String email,String ip);

     String registerAccount(RegisterVo vo);

     Account getAccountById(Integer id);

     String resetPasswordConfirm(ResetConfirmVo vo);
     //忘记密码后修改密码
     String resetPassword(ResetByEmailVo vo);

     String resetEmail(int id, ResetEmailVo vo);
     //修改密码
     String changePassword(int id,ChangePasswordVo vo);
}

