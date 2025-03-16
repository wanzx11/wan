package com.exmple.backend;

import com.exmple.entity.RestBean;
import com.exmple.entity.dto.TopicType;
import com.exmple.entity.vo.response.WeatherVo;
import com.exmple.mapper.TopicTypeMapper;
import com.exmple.service.TopicService;
import com.exmple.service.WeatherService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
@MapperScan("com.exmple.mapper")
public class WeatherTest {

    @Resource
    private WeatherService weatherService;

    @Resource
    TopicService topicService;
    @Test
    public void test() {
        List<TopicType> list = topicService.getAllTopicType();
        for (TopicType topicType : list) {
            System.out.println(topicType);
        }
    }
    @Test
    public void contextLoads() {
        WeatherVo weatherVo = weatherService.getWeather(112, 28);
        if (weatherVo == null) {
            System.out.println("null");
        }else {
            System.out.println(weatherVo);
        }
    }
}
