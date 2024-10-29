package com.exmple.backend;


import com.exmple.mapper.AccountMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
@MapperScan("com.exmple.mapper")
class BackendApplicationTests {

    @Resource
    AccountMapper mapper;

    @Test
    void contextLoads() {
        System.out.println(mapper.selectById(1));
    }

}
