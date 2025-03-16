package com.exmple.backend;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exmple.entity.dto.Topic;
import com.exmple.entity.dto.TopicType;
import com.exmple.mapper.AccountMapper;
import com.exmple.mapper.TopicMapper;
import com.exmple.mapper.TopicTypeMapper;
import com.exmple.service.TopicService;
import com.exmple.service.WeatherService;
import com.exmple.service.impl.AccountDetailsServiceImp;
import com.exmple.service.impl.AccountServiceImpl;
import com.exmple.utils.FlowUtils;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@SpringBootTest
@MapperScan("com.exmple.mapper")
class BackendApplicationTests {


    @Resource
    PasswordEncoder passwordEncoder;
    @Resource
    AccountMapper accountMapper;
    @Resource
    TopicTypeMapper topicTypeMapper;
    @Resource
    FlowUtils flowUtils;
    @Resource
    TopicMapper topicMapper;


    @Test
    public void contextLoads() {
        int type = 0;
        int pageNumber = 1;
        Page<Topic> page = Page.of(pageNumber, 10);
        if (type == 0)
            topicMapper.selectPage(page, Wrappers.<Topic>query().orderByDesc("time"));
        else
            topicMapper.selectPage(page, Wrappers.<Topic>query().eq("type", type).orderByDesc("time"));

        List<Topic> topics = page.getRecords();
        topics.forEach(topic -> {
            System.out.println(topic);
        });
    }
    @Test
    public void test(){
        topicMapper.collectTopics(1).forEach(System.out::println);

    }

}
