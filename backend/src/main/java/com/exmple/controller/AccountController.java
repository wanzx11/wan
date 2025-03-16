package com.exmple.controller;

import com.exmple.entity.RestBean;
import com.exmple.entity.dto.Account;
import com.exmple.entity.dto.AccountDetails;
import com.exmple.entity.vo.request.DetailsSaveVo;
import com.exmple.entity.vo.response.AccountDetailsVo;
import com.exmple.entity.vo.response.AccountVo;
import com.exmple.service.AccountDetailsService;
import com.exmple.service.AccountService;
import com.exmple.utils.Const;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class AccountController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);
    @Resource
    AccountService accountService;

    @Resource
    AccountDetailsService detailsService;

    @GetMapping("/info")
    public RestBean<AccountVo> info(@RequestAttribute(Const.USER_ID) int userId){
        Account account =  accountService.getAccountById(userId);
        AccountVo accountVo = new AccountVo();
        BeanUtils.copyProperties(account,accountVo);
        return RestBean.success(accountVo);
    }// 返回一个封装好的 RestBean 对象，标志请求成功并携带用户信息。

    @GetMapping("/details")
    public RestBean<AccountDetailsVo> details(@RequestAttribute(Const.USER_ID) int userId){
        AccountDetails details = new AccountDetails();
        if (detailsService.getById(userId) != null){
            details = detailsService.getById(userId);
        }
        AccountDetailsVo detailsVo =  new AccountDetailsVo();
        BeanUtils.copyProperties(details,detailsVo);
        return RestBean.success(detailsVo);
    }

    @PostMapping("/save-details")
    public RestBean<Void> saveDetails(@RequestAttribute(Const.USER_ID) int userId,
            @RequestBody DetailsSaveVo DetailsSaveVo){
        boolean save = detailsService.saveAccountDetails(userId, DetailsSaveVo);
        log.warn(DetailsSaveVo.toString());
        if(save) return RestBean.success();
        return RestBean.fail(400, "此用户名已被使用");
    }

}
