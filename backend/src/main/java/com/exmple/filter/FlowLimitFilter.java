package com.exmple.filter;

import com.exmple.entity.RestBean;
import com.exmple.utils.Const;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@Order(Const.ORDER_LIMIT)
public class FlowLimitFilter extends HttpFilter {

    @Resource
    StringRedisTemplate template;

    @Override
    protected void doFilter(HttpServletRequest request,
                            HttpServletResponse response,
                            FilterChain chain) throws IOException, ServletException {
        String address = request.getRemoteAddr();
        if (!this.IsBlock(address)){
            chain.doFilter(request, response);
        }else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(RestBean.forbidden("请求繁忙，请稍后在试").asJsonString());
        }


    }

    private boolean IsBlock(String ip){
        synchronized (ip){
            if(Boolean.TRUE.equals(template.hasKey(Const.FLOW_BLOCK + ip))){
                return true;
            }
            return this.overCount(ip);
        }
    }

    private boolean overCount(String ip){
        if(Boolean.TRUE.equals(template.hasKey(Const.FLOW_COUNT + ip))){
            Long count = template.opsForValue().increment(Const.FLOW_COUNT + ip);
            if(count == null) count = 0L;
            if(count >= 20){
                template.opsForValue().set(Const.FLOW_BLOCK+ip,"" , 20, TimeUnit.SECONDS);
                return true;
            }
        }else {
            template.opsForValue().set(Const.FLOW_COUNT+ip,"1" , 3, TimeUnit.SECONDS);
        }
        return false;
    }

}
