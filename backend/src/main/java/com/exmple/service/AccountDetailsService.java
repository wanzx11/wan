package com.exmple.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.exmple.entity.dto.AccountDetails;
import com.exmple.entity.vo.request.DetailsSaveVo;

public interface AccountDetailsService extends IService<AccountDetails> {
    AccountDetails findByAccountDetailsById(int id);
    boolean saveAccountDetails(int id, DetailsSaveVo vo);
}
