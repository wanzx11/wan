package com.exmple.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.exmple.entity.dto.TopicImage;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;

public interface ImageService extends IService<TopicImage> {
    String uploadAvatar(MultipartFile file, int id) throws IOException;
    String uploadImage(MultipartFile file, int id) throws IOException;
    void fetchImageFromMinio(OutputStream stream, String image) throws Exception;
}
