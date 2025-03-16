package com.exmple.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Component 注解：标记该类为 Spring 的组件，使其可以被自动扫描和注入。
 * 这是一个 JWT 工具类，用于生成、解析、验证和失效管理 Token。
 */
@Component
public class JwtUtils {

    @Value("${spring.security.jwt.key}") // 注入 JWT 加密密钥
    String key;

    @Value("${spring.security.jwt.expiry}") // 注入 Token 的过期时间（单位：天）
    int expiry;

    @Resource // 注入 Redis 操作类，用于管理黑名单
    StringRedisTemplate stringRedisTemplate;

    /**
     * 使 Token 失效的方法。
     * @param headerToken 从请求头中获取的 Token。
     * @return 是否成功将 Token 设置为失效状态。
     */
    public boolean invalidateJwt(String headerToken) {
        String token = this.converToken(headerToken); // 将完整的 Bearer Token 转换为原始 Token
        if (token == null) return false; // 如果 Token 无效，返回 false
        Algorithm algorithm = Algorithm.HMAC256(key); // 使用 HMAC256 加密算法验证 Token
        JWTVerifier verifier = JWT.require(algorithm).build();
        try {
            DecodedJWT jwt = verifier.verify(token); // 验证 Token 是否合法
            String id = jwt.getId(); // 获取 JWT 的唯一 ID
            return deleteToken(id, jwt.getExpiresAt()); // 将 Token 加入黑名单
        } catch (JWTVerificationException e) {
            return false; // 验证失败，返回 false
        }
    }

    /**
     * 将 Token 加入 Redis 黑名单，设置过期时间与 JWT 的过期时间一致。
     * @param uuid JWT 的唯一 ID
     * @param expired JWT 的过期时间
     * @return 是否成功加入黑名单
     */
    private boolean deleteToken(String uuid, Date expired) {
        if (this.isInvalidToken(uuid)) return false; // 如果已经在黑名单中，返回 false
        Date now = new Date();
        long expon = Math.max(0, expired.getTime() - now.getTime()); // 计算剩余过期时间
        stringRedisTemplate.opsForValue().set(Const.Jwt_Black_List + uuid, "", expon, TimeUnit.MILLISECONDS);
        return true;
    }

    /**
     * 检查指定的 Token 是否在黑名单中。
     * @param uuid JWT 的唯一 ID
     * @return 是否在黑名单中
     */
    private boolean isInvalidToken(String uuid) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(Const.Jwt_Black_List + uuid));
    }

    /**
     * 验证并解析 Token，如果合法且未过期，返回解析后的 JWT 对象。
     * @param headerToken 从请求头中获取的 Token
     * @return 解码后的 JWT，如果无效返回 null。
     */
    public DecodedJWT resolveJwt(String headerToken) {
        String token = this.converToken(headerToken); // 转换 Token
        if (token == null) {
            return null;
        }
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier verifier = JWT.require(algorithm).build();
        try {
            DecodedJWT jwt = verifier.verify(token); // 验证 Token 是否合法
            if (this.isInvalidToken(jwt.getId())) // 检查是否在黑名单中
                return null;
            Date expiresAt = jwt.getExpiresAt(); // 获取过期时间
            return new Date().after(expiresAt) ? null : jwt; // 如果已过期，返回 null
        } catch (JWTVerificationException e) {
            return null; // 验证失败，返回 null
        }
    }

    /**
     * 生成 JWT Token 的方法。
     * @param userDetails 用户详细信息（包含权限等）
     * @param id 用户的唯一标识
     * @param username 用户名
     * @return 生成的 JWT Token
     */
    public String crestJwt(UserDetails userDetails, int id, String username) {
        Algorithm algorithm = Algorithm.HMAC256(key); // 使用 HMAC256 算法加密
        Date expiry = this.expiryDate(); // 生成过期时间
        return JWT.create()
                .withJWTId(UUID.randomUUID().toString()) // 设置唯一 ID
                .withClaim("id", id) // 设置用户 ID
                .withClaim("name", username) // 设置用户名
                .withClaim("authorities", userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()) // 设置用户权限
                .withExpiresAt(expiry) // 设置过期时间
                .withIssuedAt(new Date()) // 设置签发时间
                .sign(algorithm); // 签名并生成 Token
    }

    /**
     * 生成 Token 的过期时间。
     * @return 过期时间（Date 类型）
     */
    public Date expiryDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, expiry * 24); // 将过期时间设为当前时间加上配置的天数
        return calendar.getTime();
    }

    /**
     * 将解析后的 JWT 转换为 Spring Security 的 UserDetails 对象。
     * @param jwt 解码后的 JWT
     * @return 包含用户信息的 UserDetails 对象
     */
    public UserDetails toUserDetails(DecodedJWT jwt) {
        Map<String, Claim> claims = jwt.getClaims(); // 获取 JWT 中的自定义 Claims
        return User
                .withUsername(claims.get("name").asString()) // 设置用户名
                .password("123456") // 默认密码（不会被实际使用，因为认证时只需要 Token）
                .authorities(claims.get("authorities").asArray(String.class)) // 设置权限
                .build();
    }

    /**
     * 从解析后的 JWT 中提取用户 ID。
     * @param jwt 解码后的 JWT
     * @return 用户 ID
     */
    public Integer toId(DecodedJWT jwt) {
        Map<String, Claim> claims = jwt.getClaims(); // 获取自定义 Claims
        return claims.get("id").asInt(); // 提取用户 ID
    }

    /**
     * 将 Authorization Header 中的 Bearer Token 提取为原始 Token。
     * @param token 完整的 Authorization Header
     * @return 原始 Token，如果格式不合法返回 null。
     */
    private String converToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) // 检查是否以 "Bearer " 开头
            return null;
        return token.substring(7); // 去掉 "Bearer " 前缀
    }
}
