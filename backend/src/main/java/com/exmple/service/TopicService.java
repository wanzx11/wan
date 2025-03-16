package com.exmple.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.exmple.entity.dto.Interact;
import com.exmple.entity.dto.Topic;
import com.exmple.entity.dto.TopicType;
import com.exmple.entity.vo.request.AddCommentVo;
import com.exmple.entity.vo.request.TopicCreateVo;
import com.exmple.entity.vo.response.CommentVo;
import com.exmple.entity.vo.response.TopTopicVo;
import com.exmple.entity.vo.response.TopicDetailVo;
import com.exmple.entity.vo.response.TopicPreviewVo;

import java.util.List;

public interface TopicService extends IService<Topic> {

    List<TopicType> getAllTopicType();
    String creatTopic(int userId,TopicCreateVo topicCreateVo);
    String updateTopic(int userId,TopicCreateVo topicCreateVo);
    List<TopicPreviewVo> listTopicByPage(int page, int type);
    List<TopTopicVo> listTopTopic();
    TopicDetailVo getTopicDetail(int tId,int uid);
    void deleteTopic(int tid,int uid);
    void interact(Interact interact,boolean state);
    List<TopicPreviewVo> listUssrTopic(int uid);
    List<TopicPreviewVo> listTopicByCollect(int uid);
    String creatComment(AddCommentVo vo,int uid);
    List<CommentVo> listComment(int tid,int page);
    void deleteComment(int cid,int uid);
    List<TopicPreviewVo> listTopicBySearch(String search);
}
