package com.exmple.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exmple.entity.dto.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TopicCommentMapper extends BaseMapper<Comment> {
}
