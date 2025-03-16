package com.exmple.config;

import com.exmple.entity.RestBean;
import com.exmple.entity.dto.Account;
import com.exmple.entity.vo.response.AuthorizeVO;
import com.exmple.filter.JwtAuthroizeFilter;
import com.exmple.service.AccountService;
import com.exmple.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.io.PrintWriter;
@EnableWebSecurity
@Configuration
public class SecurityConfiguration {
    // 配置 Spring Security 的安全设置类

    @Resource
    JwtUtils jwtUtils; // JWT 工具类，用于生成和解析 JWT
    @Resource
    JwtAuthroizeFilter jwtAuthroizeFilter; // JWT 授权过滤器
    @Resource
    AccountService accountService; // 账户服务类，用于查询账户信息

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // 配置请求的访问权限
                .authorizeHttpRequests(conf -> conf
                        .requestMatchers("/api/auth/**", "/error").permitAll() // 登录、注册接口和错误页面允许所有用户访问
                        .requestMatchers("api/image/**").permitAll() // 图片资源接口允许所有用户访问
                        .anyRequest().authenticated() // 其他请求需要认证
                )
                // 配置表单登录
                .formLogin(conf -> conf
                        .loginProcessingUrl("/api/auth/login") // 登录接口
                        .successHandler(this::onAuthenticationSuccess) // 登录成功的处理逻辑
                        .failureHandler(this::onAuthenticationFailure) // 登录失败的处理逻辑
                )
                // 配置登出
                .logout(conf -> conf
                        .logoutUrl("/api/auth/logout") // 登出接口
                        .logoutSuccessHandler(this::onLogoutSuccess) // 登出成功的处理逻辑
                )
                // 配置异常处理
                .exceptionHandling(conf -> conf
                        .authenticationEntryPoint(this::unAuthorized) // 未登录时的处理逻辑
                        .accessDeniedHandler(this::accessDeny) // 无权限访问时的处理逻辑
                )
                // 配置跨域
                .cors(conf -> {
                    CorsConfiguration cors = new CorsConfiguration();
                    cors.addAllowedOrigin("http://localhost:5173"); // 允许的跨域请求来源
                    cors.setAllowCredentials(false); // 不允许发送 Cookie
                    cors.addAllowedHeader("*"); // 允许的请求头
                    cors.addAllowedMethod("*"); // 允许的请求方法
                    cors.addExposedHeader("*"); // 允许客户端访问的响应头
                    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                    source.registerCorsConfiguration("/**", cors); // 注册跨域配置
                    conf.configurationSource(source);
                })
                .csrf(AbstractHttpConfigurer::disable) // 禁用 CSRF 保护
                .sessionManagement(conf -> conf
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 使用无状态会话
                )
                // 在 UsernamePasswordAuthenticationFilter 之前添加自定义 JWT 授权过滤器
                .addFilterBefore(jwtAuthroizeFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // 登录成功的处理逻辑
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        User user = (User) authentication.getPrincipal(); // 获取登录的用户信息
        Account account = accountService.getAccountByNameOrEmail(user.getUsername()); // 根据用户名查询账户
        String token = jwtUtils.crestJwt(user, account.getId(), account.getUsername()); // 生成 JWT
        AuthorizeVO vo = new AuthorizeVO(); // 构建返回的用户授权信息
        vo.setUsername(account.getUsername());
        vo.setExpires(jwtUtils.expiryDate()); // JWT 过期时间
        vo.setToken(token);
        vo.setRole(account.getRole());
        response.getWriter().write(RestBean.success(vo).asJsonString()); // 返回 JSON 格式的成功信息
    }

    // 登录失败的处理逻辑
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(RestBean.fail(401, exception.getMessage()).asJsonString()); // 返回 JSON 格式的失败信息
    }

    // 登出成功的处理逻辑
    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String authorization = request.getHeader("Authorization"); // 获取请求头中的 JWT
        if (jwtUtils.invalidateJwt(authorization)) { // 作废 JWT
            out.write(RestBean.success("退出登录成功").asJsonString()); // 返回成功信息
        } else {
            out.write(RestBean.fail(400, "退出登录失败").asJsonString()); // 返回失败信息
        }
    }

    // 未登录时的处理逻辑
    public void unAuthorized(HttpServletRequest request,
                             HttpServletResponse response,
                             AuthenticationException authException) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(RestBean.unAuthorized(authException.getMessage()).asJsonString()); // 返回未登录信息
    }

    // 无权限访问时的处理逻辑
    public void accessDeny(HttpServletRequest request,
                           HttpServletResponse response,
                           AccessDeniedException authException) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(RestBean.forbidden(authException.getMessage()).asJsonString()); // 返回无权限信息
    }
}
