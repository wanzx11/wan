package com.exmple.controller;

import com.exmple.entity.RestBean;
import com.exmple.entity.vo.request.ResetByEmailVo;
import com.exmple.entity.vo.request.ResetConfirmVo;
import com.exmple.entity.vo.request.RegisterVo;
import com.exmple.service.AccountService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.function.Supplier;

@RestController
@Validated
@RequestMapping("/api/auth")
public class AuthorizeController {

    @Resource
    AccountService service;

    @GetMapping("/ask-code")
    public RestBean<Void> askCode(@RequestParam @Email String email,
                                     @RequestParam @Pattern(regexp = "(register|reset)") String type,
                                    HttpServletRequest request){
        return this.messageHandle(()->
                service.registerEmailCode(type, email, request.getRemoteAddr()));
    }
    //post请求用json接受
    @PostMapping("/register")
    public RestBean<Void> registerUser(@RequestBody @Valid RegisterVo vo){
        return this.messageHandle(()->
                service.registerAccount(vo));
    }

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

    private RestBean<Void> messageHandle(Supplier<String> action){
        String message = action.get();
        if (message == null)
            return RestBean.success();
        else return RestBean.fail(400, message);
    }

}
