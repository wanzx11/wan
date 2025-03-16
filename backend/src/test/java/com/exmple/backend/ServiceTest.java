package com.exmple.backend;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.exmple.entity.dto.Comment;
import com.exmple.service.TopicService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Wrapper;
import java.util.List;
import java.util.Map;

@SpringBootTest
@MapperScan("com.exmple.mapper")
public class ServiceTest {
    @Resource
    TopicService topicService;
    @Resource
    RabbitTemplate rabbitTemplate;

    @Test
    public void test() {
        Map<String, Object> testData = Map.of(
                "type", "register",
                "email", "1300969822@qq.com",
                "code", 123456
        );
        System.out.println(testData);
        rabbitTemplate.convertAndSend("email", testData); // 确保队列名与消费者监听一致

    }
}
