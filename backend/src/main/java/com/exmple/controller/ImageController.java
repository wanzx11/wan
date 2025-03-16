package com.exmple.controller;

import com.exmple.entity.RestBean;
import com.exmple.service.ImageService;
import com.exmple.utils.Const;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/image")
public class ImageController {

    @Resource
    private ImageService imageService;

    @PostMapping("/cache")
    public RestBean<String> uploadCache(@RequestParam("file") MultipartFile file,
                                        @RequestAttribute(Const.USER_ID) int id) throws IOException {
        if(file.getSize() > 1024 * 1024 * 5) {
            return RestBean.fail(400,"图片不能大于5mb");
        }
        log.info("图片正在上传");
        String fileName = imageService.uploadImage(file, id);
        if (fileName != null){
            log.info("图片上传成功，大小为：{}", file.getSize()+fileName);
            return RestBean.success(fileName);
        }
        return RestBean.fail(400, "头像上传失败");
    }

    @PostMapping("/avatar")
    public RestBean<String> uploadAvatar(@RequestParam("file") MultipartFile file,
                                         @RequestAttribute(Const.USER_ID)int userId) throws IOException {
        if (file.getSize() > 1024*200)
            return RestBean.fail(400, "头像不能大于200kb");
        log.info("头像正在上传");
        String fileName = imageService.uploadAvatar(file, userId);

        if (fileName != null){
            log.info("头像上传成功，大小为：{}", file.getSize());
            return RestBean.success(fileName);
        }
        return RestBean.fail(400, "头像上传失败");
    }

    @GetMapping("/**")
    public void getImage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Content-Type", "image/jpg");
        String imagePath = request.getServletPath();
        ServletOutputStream out = response.getOutputStream();
        // 校验路径是否以正确的前缀开头，这里你可以根据你的实际需求进行更改
        if (imagePath.startsWith(Const.FILE_NAME)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.println(RestBean.fail(404, "Not Found").toString());
        } else {
            try {
                // 从路径中提取文件名，去除 "/file"
                String image = imagePath.substring("/api/image".length());
                imageService.fetchImageFromMinio(out, image);  // 调用服务从 MinIO 获取文件
                response.setHeader("Content-Type", "image/jpg");
                // 设置缓存控制头
                response.setHeader("Cache-Control", "max-age=2592000");  // 30天的缓存
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.println(RestBean.fail(404, "Not Found").toString());
                log.error(e.getMessage());
            }
        }
    }

}
