package com.exmple.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.exmple.entity.dto.Notification;
import com.exmple.entity.vo.response.NotificationVo;

import java.util.List;

public interface NotificationService extends IService<Notification> {
    List<NotificationVo> findAllNotification(int uid);
    void deleteNotification(int id,int uid);
    void deleteAllNotifications(int uid);
    void addNotification(int uid,String title,String content,String type,String url);

}
