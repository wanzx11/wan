package com.exmple.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.exmple.entity.dto.Account;
import org.springframework.security.core.userdetails.UserDetailsService;


public interface AccountService extends IService<Account>, UserDetailsService {
    public Account getAccountByNameOrEmail(String text);
}

