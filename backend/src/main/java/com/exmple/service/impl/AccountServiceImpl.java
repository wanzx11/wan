package com.exmple.service.impl;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exmple.entity.dto.Account;
import com.exmple.entity.vo.request.ResetConfirmVo;
import com.exmple.entity.vo.request.RegisterVo;
import com.exmple.entity.vo.request.ResetByEmailVo;
import com.exmple.mapper.AccountMapper;

import com.exmple.service.AccountService;
import com.exmple.utils.Const;
import com.exmple.utils.FlowUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

   /* @Resource
    private AmqpTemplate amqpTemplate;*/

    @Resource
    JavaMailSender mailSender;

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
            int code = (int)(Math.random()*1000000);
            Map<String, Object> data = Map.of("type", type, "email", email, "code", code);
            sendMailMassage(data);
            stringRedisTemplate.opsForValue().
                    set(Const.EMAIL_DATA+email, String.valueOf(code),5, TimeUnit.MINUTES);
            return null;
        }

    }

    @Override
    public String registerAccount(RegisterVo vo) {
        String username = vo.getUsername();
        String email = vo.getEmail();
        String key = Const.EMAIL_DATA+email;
        System.out.println(key);
        String code = stringRedisTemplate.opsForValue().get(key);
        System.out.println(code);
        if (code == null)    return "请先获取验证码";
        if (!code.equals(vo.getCode()))     return "验证码错误";
        if (hasAccountByEmail(email))    return "此电子邮件已被注册";
        if (hasAccountByUsername(username))     return "该用户名已被使用";
        String password = passwordEncoder.encode(vo.getPassword());
        Account account = new Account(null,username,password,email,"user",new Date());
        if (this.save(account)){
            stringRedisTemplate.delete(key);
            return null;
        }else {
            return "内部错误";
        }
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

    private boolean hasAccountByEmail(String email) {
        return this.baseMapper.exists(Wrappers.<Account>query().eq("email", email));
    }
    private boolean hasAccountByUsername(String username) {
        return this.baseMapper.exists(Wrappers.<Account>query().eq("username", username));
    }

    private void sendMailMassage(Map<String,Object> data) {
        String email = (String) data.get("email");
        Integer code = (Integer) data.get("code");
        String type = (String) data.get("type");
        SimpleMailMessage message = switch (type){
            case "register" -> createMessage(
                    "欢迎注册我的网站",
                    "您的邮寄验证码为："+code+"\n有效时间五分钟，为了你的安全请勿向他人泄露该验证码。",
                    email
            );
            case "reset" -> createMessage(
                    "重置密码验证码",
                    "您好，本次重置密码验证码为："+code+"\n有效时间五分钟，为了你的安全请勿向他人泄露该验证码。",
                    email
            );
            default -> null;
        };
        if (message == null) return;
        mailSender.send(message);
    }

    private SimpleMailMessage createMessage(String title, String content, String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(title);
        message.setText(content);
        message.setFrom(username);
        return message;
    }
}
