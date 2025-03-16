package com.exmple.controller;

import com.baomidou.mybatisplus.annotation.TableName;
import com.exmple.entity.RestBean;
import com.exmple.entity.vo.request.*;
import com.exmple.service.AccountService;
import com.exmple.utils.Const;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.function.Supplier;

@RestController
@Validated
@RequestMapping("/api/auth")
public class AuthorizeController {

    private static final Logger log = LoggerFactory.getLogger(AuthorizeController.class);
    @Resource
    AccountService service;

    @GetMapping("/ask-code")
    @Transactional
    public RestBean<Void> askCode(@RequestParam @Email String email,
                                  @RequestParam @Pattern(regexp = "(register|reset|reset-email)") String type,
                                  HttpServletRequest request) {
        return this.messageHandle(()->{
            log.info("Processing request for email: {}, type: {}", email, type);
            return service.registerEmailCode(type, email, request.getRemoteAddr());
        });
    }


    //post请求用json接受
    @PostMapping("/register")
    public RestBean<Void> registerUser(@RequestBody @Valid RegisterVo vo){
        return this.messageHandle(()->
                service.registerAccount(vo));
    }
    //验证码确认
    @PostMapping("/reset-confirm")
    public RestBean<Void> resetConfirm(@RequestBody @Valid ResetConfirmVo vo){
        return this.messageHandle(()->
            service.resetPasswordConfirm(vo)
        );
    }

    @PostMapping("/reset-password")
    public RestBean<Void> resetPassword(@RequestBody @Valid ResetByEmailVo vo){
        return this.messageHandle(()->
                service.resetPassword(vo));
    }

    @PostMapping("/reset-email")
    public RestBean<Void> resetEmail(@RequestAttribute(Const.USER_ID) int id,
            @RequestBody @Valid ResetEmailVo vo){
        return this.messageHandle(()->
            service.resetEmail(id, vo)
        );
    }

    @PostMapping("/change-password")
    public RestBean<Void> changePassword(@RequestAttribute(Const.USER_ID) int id,
            @RequestBody @Valid ChangePasswordVo vo){
        log.warn(vo.toString());
        return  this.messageHandle(()->
                service.changePassword(id, vo)
        );
    }

    private RestBean<Void> messageHandle(Supplier<String> action){
        String message = action.get();
        if (message == null)
            return RestBean.success();
        else return RestBean.fail(400, message);
    }

}
