package com.exmple.entity.vo.response;

import lombok.Data;

import java.util.Date;

@Data
public class CommentVo {
    Integer id;
    String content;
    Date time;
    String quote;
    User user;

    @Data
    public static class User{
        Integer id;
        String username;
        String email;
        String avatar;
        String description;
        Integer gender;
        String qq;
        String wx;
        String phone;
    }
}
