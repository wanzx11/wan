package com.exmple.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exmple.entity.dto.Account;
import com.exmple.mapper.AccountMapper;

import com.exmple.service.AccountService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = this.getAccountByNameOrEmail(username);
        if (account == null) {
            throw new UsernameNotFoundException("未找到："+username);
        }
        return User.withUsername(account.getUsername())
                .password(account.getPassword())
                .roles(account.getRole())
                .build();
    }

    public Account getAccountByNameOrEmail(String text) {
        return this.query()
                .eq("email", text).or()
                .eq("username", text).one();
    }
}
