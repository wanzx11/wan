package com.exmple.utils;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

//限流工具
@Component
public class FlowUtils {

    @Resource
    StringRedisTemplate stringRedisTemplate;

    private static final LimitAction defaultAction = overclock -> !overclock;

   // registerEmailCode限流
    public boolean limitOnceCheck(String key,int blockTime){
        return internalCheck(key,1,blockTime,defaultAction);
    }


    public boolean limitForumCheck(String key,int frequency,int blockTime){
        return internalCheck(key,frequency,blockTime,defaultAction);
    }




    /**
     * 内部使用请求限制主要逻辑
     * @param key 计数键
     * @param frequency 请求频率
     * @param period 计数周期
     * @param action 限制行为与策略
     * @return 是否通过限流检查
     */
    private boolean internalCheck(String key, int frequency, int period, LimitAction action){
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))) {
            Long value = Optional.ofNullable(stringRedisTemplate.opsForValue().increment(key)).orElse(0L);
            return action.run(value > frequency);
        } else {
            stringRedisTemplate.opsForValue().set(key, "1", period, TimeUnit.SECONDS);
            return true;
        }
    }

    /**
     * 内部使用，限制行为与策略
     */
    private interface LimitAction {
        boolean run(boolean overclock);
    }

}
