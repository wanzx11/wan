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

@Component
public class JwtUtils {

    @Value("${spring.security.jwt.key}")
    String key;

    @Value("${spring.security.jwt.expiry}")
    int expiry;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    //令牌失效
    public boolean invalidateJwt(String headerToken) {
        String token = this.converToken(headerToken);
        if (token == null) return false;
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier verifier = JWT.require(algorithm).build();
        try {
            DecodedJWT jwt = verifier.verify(token);
            String id = jwt.getId();
            return deleteToken(id, jwt.getExpiresAt());
        }catch (JWTVerificationException e) {
            return false;
        }

    }
    //判断令牌是否国企
    private boolean deleteToken(String uuid,Date expired) {
        if (this.isInvalidToken(uuid)) return false;
        Date now = new Date();
        long expon =Math.max(0, expired.getTime() - now.getTime());
        System.out.println("过期时间："+expon);
        System.out.println(Const.Jwt_Black_List + uuid);
        stringRedisTemplate.opsForValue().set(Const.Jwt_Black_List+uuid, "", expon, TimeUnit.MILLISECONDS);
        return true;
    }

    private boolean isInvalidToken(String uuid) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(Const.Jwt_Black_List + uuid));
    }

    public DecodedJWT resolveJwt(String headerToken) {
        String token = this.converToken(headerToken);
        if (token == null) {
            return null;
        }
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier verifier = JWT.require(algorithm).build();
        try {
            DecodedJWT jwt = verifier.verify(token);
            if (this.isInvalidToken(jwt.getId()))
                return null;
            Date expiresAt = jwt.getExpiresAt();
            return new Date().after(expiresAt) ? null : jwt;
        }catch (JWTVerificationException e) {
            return null;
        }
    }

    public String crestJwt(UserDetails userDetails,int id,String username){
        Algorithm algorithm = Algorithm.HMAC256(key);
        Date expiry = this.expiryDate();
        return JWT.create()
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("id", id)
                .withClaim("name", username)
                .withClaim("authorities", userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .withExpiresAt(expiry)
                .withIssuedAt(new Date())
                .sign(algorithm);

    }

    public Date expiryDate(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, expiry*24);
        return calendar.getTime();
    }
    //解析用户方法
    public UserDetails toUserDetails(DecodedJWT jwt) {
        Map<String, Claim> claims = jwt.getClaims();
        return User
                .withUsername(claims.get("name").asString())
                .password("123456")
                .authorities(claims.get("authorities").asArray(String.class))
                .build();
    }

    public Integer toId(DecodedJWT jwt){
        Map<String, Claim> claims = jwt.getClaims();
        return claims.get("id").asInt();
    }

    //将headertoken合法
    private String converToken(String token){
        if (token == null || !token.startsWith("Bearer "))
            return null;
        return token.substring(7);
    }

}
