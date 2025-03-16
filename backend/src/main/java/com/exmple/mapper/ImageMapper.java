package com.exmple.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exmple.entity.dto.TopicImage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ImageMapper extends BaseMapper<TopicImage> {
}
