package com.exmple.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exmple.entity.dto.Notification;
import com.exmple.entity.vo.response.NotificationVo;
import com.exmple.mapper.NotificationMapper;
import com.exmple.service.NotificationService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {
    @Override
    public List<NotificationVo> findAllNotification(int uid) {
        return baseMapper.selectList(Wrappers.<Notification>query().eq("uid",uid))
                .stream().map(notification -> {
                    NotificationVo notificationVo = new NotificationVo();
                    BeanUtils.copyProperties(notification,notificationVo);
                    return notificationVo;
                }).toList();
    }

    @Override
    public void deleteNotification(int id, int uid) {
        Notification notification = baseMapper.selectById(id);
        if (notification != null && notification.getUid() ==uid) {
            baseMapper.deleteById(id);
        }
    }

    @Override
    public void deleteAllNotifications(int uid) {
        baseMapper.delete(Wrappers.<Notification>query().eq("uid", uid));
    }

    @Override
    public void addNotification(int uid, String title, String content, String type, String url) {
        Notification notification = new Notification();
        notification.setUid(uid);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setUrl(url);
        this.save(notification);
    }
}
