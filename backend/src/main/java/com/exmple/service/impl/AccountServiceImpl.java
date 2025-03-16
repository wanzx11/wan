package com.exmple.service.impl;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exmple.entity.dto.Account;
import com.exmple.entity.vo.request.*;
import com.exmple.mapper.AccountMapper;

import com.exmple.service.AccountService;
import com.exmple.utils.Const;
import com.exmple.utils.FlowUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    @Resource
    private AmqpTemplate amqpTemplate;


    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Value("${spring.mail.username}")
    String username;

    @Resource
    FlowUtils flowUtils;

    @Resource
    PasswordEncoder passwordEncoder;

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

    @Override
    public String registerEmailCode(String type, String email, String ip) {
        //加锁防止同时多次请求验证
        synchronized (ip.intern()){
            String key = Const.EMAIL_LIMIT+ip;
            if(!flowUtils.limitOnceCheck(key, 60)){
                return "请求频繁";
            }
            int code = (int)((Math.random()*9+1)*100000);
            Map<String, Object> data = Map.of("type", type, "email", email, "code", code);
            log.info("aaaaa  email data: {}", data);
            amqpTemplate.convertAndSend(Const.MQ_MAIL, data);
            stringRedisTemplate.opsForValue().
                    set(Const.EMAIL_DATA+email, String.valueOf(code),5, TimeUnit.MINUTES);
            return null;
        }

    }

    @Override
    @Transactional
    public String registerAccount(RegisterVo vo) {
        String username = vo.getUsername();
        String email = vo.getEmail();
        String key = Const.EMAIL_DATA+email;
        String code = stringRedisTemplate.opsForValue().get(key);
        if (code == null)    return "请先获取验证码";
        if (!code.equals(vo.getCode()))     return "验证码错误";
        if (hasAccountByEmail(email))    return "此电子邮件已被注册";
        if (hasAccountByUsername(username))     return "该用户名已被使用";
        String password = passwordEncoder.encode(vo.getPassword());
        Account account = new Account(null,username,password,email,"user",new Date(),null);
        if (this.save(account)){
            stringRedisTemplate.delete(key);
            return null;
        }else {
            return "内部错误";
        }
    }

    @Override
    public Account getAccountById(Integer id) {
        return this.query().eq("id", id).one();
    }

    @Override
    public String resetPasswordConfirm(ResetConfirmVo vo) {
        String email = vo.getEmail();
        String key = Const.EMAIL_DATA+email;
        String code = stringRedisTemplate.opsForValue().get(key);
        if (code == null) return "请先获取验证码";
        if (!code.equals(vo.getCode())) return "验证码错误";
        return null;
    }

    @Override
    public String resetPassword(ResetByEmailVo vo) {
        String email = vo.getEmail();
        String confirm = this.resetPasswordConfirm(new ResetConfirmVo(email,vo.getCode()));
        if (confirm != null) return confirm;
        String password = passwordEncoder.encode(vo.getPassword());
        boolean updated =  this.update().eq("email", email).set("password", password).update();
        if (updated) {
            stringRedisTemplate.delete(Const.EMAIL_DATA+email);
            return null;
        }else{
            return "内部错误";
        }
    }

    @Override
    public String resetEmail(int id, ResetEmailVo vo) {
        String email = vo.getEmail();
        String key = Const.EMAIL_DATA+email;
        String code = stringRedisTemplate.opsForValue().get(key);
        if (code == null) return "请先获取验证码";
        if (!code.equals(vo.getCode())) return "验证码错误，请重新输入";
        if (this.getAccountByNameOrEmail(email) != null) return "改电子邮件已被其他用户绑定";
        this.update().eq("id", id)
                .set("email", email)
                .update();
        return null;
    }

    @Override
    public String changePassword(int id,ChangePasswordVo vo) {
        String password = this.query().eq("id", id).one().getPassword();
        if (!passwordEncoder.matches(vo.getOldPassword(),password)){
            return "原密码错误，请重新输入";
        }
        password = passwordEncoder.encode(vo.getNewPassword());
        this.update().eq("id", id)
                .set("password",password)
                .update();
        return null;
    }

    private boolean hasAccountByEmail(String email) {
        return this.baseMapper.exists(Wrappers.<Account>query().eq("email", email));
    }
    private boolean hasAccountByUsername(String username) {
        return this.baseMapper.exists(Wrappers.<Account>query().eq("username", username));
    }

}
