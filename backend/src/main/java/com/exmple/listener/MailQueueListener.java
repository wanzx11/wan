/*
package com.exmple.listener;

import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RabbitListener(queues = "email")
public class MailQueueListener {

    @Resource
    JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    String username;

    @RabbitHandler
    public void setMailMassage(Map<String,Object> data) {
        String email = (String) data.get("email");
        Integer code = (Integer) data.get("code");
        String type = (String) data.get("type");
        SimpleMailMessage message = switch (type){
            case "register" -> createMessage(
                    "欢迎注册我的网站",
                    "您的邮寄验证码为："+code+"有效时间五分钟，为了你的安全请勿向他人泄露该验证码。",
                    email
            );
            case "resetPassword" -> createMessage(
                    "重置密码验证码",
                    "您好，本次重置密码验证码为："+code+"有效时间五分钟，为了你的安全请勿向他人泄露该验证码。",
                    email
            );
            default -> null;
        };
        if (message == null) return;

        mailSender.send(message);


    }

    private SimpleMailMessage createMessage(String title, String content,String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(title);
        message.setText(content);
        message.setFrom(username);
        return message;
    }

}
*/
