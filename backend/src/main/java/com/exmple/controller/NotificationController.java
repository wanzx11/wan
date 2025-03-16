package com.exmple.controller;

import com.exmple.entity.RestBean;
import com.exmple.entity.vo.response.NotificationVo;
import com.exmple.service.NotificationService;
import com.exmple.utils.Const;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {
    @Resource
    NotificationService service;

    @GetMapping("/list")
    public RestBean<List<NotificationVo>> list(@RequestAttribute(Const.USER_ID) int uid) {
        return RestBean.success(
                service.findAllNotification(uid));
    }

    @GetMapping("/delete")
    public RestBean<Void> delete(@RequestParam @Min(1) int id,
                                 @RequestAttribute(Const.USER_ID) int uid) {
        service.deleteNotification(id,uid);
        return RestBean.success();
    }

    @GetMapping("/delete-all")
    public RestBean<Void> deleteAll(@RequestAttribute(Const.USER_ID) int uid) {
        service.deleteAllNotifications(uid);
        return RestBean.success();
    }
}
