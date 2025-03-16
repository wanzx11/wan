package com.exmple.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exmple.entity.dto.Account;
import com.exmple.entity.dto.AccountDetails;
import com.exmple.entity.vo.request.DetailsSaveVo;
import com.exmple.mapper.AccountDetailsMapper;
import com.exmple.service.AccountDetailsService;
import com.exmple.service.AccountService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class AccountDetailsServiceImp extends ServiceImpl<AccountDetailsMapper, AccountDetails>
        implements AccountDetailsService {

    @Resource
    AccountService service;

    @Override
    public AccountDetails findByAccountDetailsById(int id) {
        return this.getById(id);
    }

    @Override
    public boolean saveAccountDetails(int id, DetailsSaveVo vo) {
        Account account = service.getAccountByNameOrEmail(vo.getUsername());
        if (account == null||account.getId().equals(id)) {
            service.update()
                    .eq("id", id)
                    .set("username", vo.getUsername())
                    .update();
            this.saveOrUpdate(new AccountDetails(
                    id,vo.getGender(), vo.getPhone(), vo.getQq(), vo.getWx(), vo.getDescribe()
            ));
            return true;
        }
        return false;
    }
}
