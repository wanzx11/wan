package com.exmple.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.exmple.entity.dto.Account;
import com.exmple.entity.vo.request.ResetConfirmVo;
import com.exmple.entity.vo.request.RegisterVo;
import com.exmple.entity.vo.request.ResetByEmailVo;
import org.springframework.security.core.userdetails.UserDetailsService;


public interface AccountService extends IService<Account>, UserDetailsService {
     Account getAccountByNameOrEmail(String text);

     String registerEmailCode(String type,String email,String ip);

     String registerAccount(RegisterVo vo);

     String resetPasswordConfirm(ResetConfirmVo vo);

     String resetPassword(ResetByEmailVo vo);
}

