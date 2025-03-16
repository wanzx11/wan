package com.exmple.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exmple.entity.dto.Account;
import com.exmple.entity.dto.TopicImage;
import com.exmple.mapper.AccountMapper;
import com.exmple.mapper.ImageMapper;
import com.exmple.service.ImageService;
import com.exmple.utils.Const;
import com.exmple.utils.FlowUtils;
import io.minio.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Wrapper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;


@Slf4j
@Service
public class ImageServiceImpl extends ServiceImpl<ImageMapper, TopicImage> implements ImageService {

    @Resource
    private MinioClient minioClient;

    @Resource
    AccountMapper mapper;

    @Resource
    FlowUtils flowUtils;

    private SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

    /*
    1.minioClient.getObject(args) 返回 GetObjectResponse，它包含从 MinIO 对象存储中获取的数据流。
    2.response.transferTo(stream) 将 response 中的内容高效传输到目标 OutputStream。
    3.try-with-resources 自动关闭 response，释放资源。
    */
    @Override
    public void fetchImageFromMinio(OutputStream stream, String image) throws Exception {
        GetObjectArgs args = GetObjectArgs.builder()
                .bucket("schoolweb")
                .object(image).build();
        try (GetObjectResponse response = minioClient.getObject(args)) {
            response.transferTo(stream);
        }
    }

    @Override
    public String uploadAvatar(MultipartFile file, int id) throws IOException{
        String fileName = "/avatar/"+Const.FILE_NAME+id+".jpg";
        PutObjectArgs args = PutObjectArgs.builder()
                .bucket("schoolweb")
                .stream(file.getInputStream(), file.getSize(), -1)
                .object(fileName)
                .build();
        try {
            minioClient.putObject(args);
            String avatar = mapper.selectById(id).getAvatar();
            int update = mapper.update(Wrappers.<Account>update()
                    .eq("id", id)
                    .set("avatar",fileName));
            if(update>0) return fileName;
            return null;
        } catch (Exception e) {
            log.error("图片上传出错: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public String uploadImage(MultipartFile file, int id) throws IOException {
        String key = Const.FLOW_FORUM_IMAGE+id;
        if (!flowUtils.limitForumCheck(key,20,3600)){
            return null;
        }
        String imageName = UUID.randomUUID().toString().replace("-","");
        Date date = new Date();
        imageName = "/cache/"+format.format(date)+"/"+imageName+".jpg";
        PutObjectArgs args = PutObjectArgs.builder()
                .bucket("schoolweb")
                .stream(file.getInputStream(), file.getSize(), -1)
                .object(imageName)
                .build();
        try {
            minioClient.putObject(args);
            if (this.save(new TopicImage(id,imageName,date))){
                return imageName;
            }else {
                this.deleteOldImag(imageName);
                return null;
            }
        } catch (Exception e) {
            log.error("图片上传出错: {}", e.getMessage());
            return null;
        }
    }

    private void deleteOldImag(String image) throws Exception {
        if(image == null || image.isEmpty()) return;
        RemoveObjectArgs remove = RemoveObjectArgs.builder()
                .bucket("schoolweb")
                .object(image)
                .build();
        minioClient.removeObject(remove);
    }

}
