package com.exmple.backend;


import com.exmple.mapper.AccountMapper;
import com.exmple.service.impl.AccountServiceImpl;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;


@SpringBootTest
@MapperScan("com.exmple.mapper")
class BackendApplicationTests {


    @Resource
    AccountServiceImpl service;

    @Test
    public void contextLoads() {
        service.registerEmailCode("register", "2388824995@qq.com", "122455");
    }


}
