package com.exmple.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exmple.entity.dto.Interact;
import com.exmple.entity.dto.Topic;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TopicMapper extends BaseMapper<Topic> {
    @Insert({
            "<script>",
            "INSERT IGNORE INTO interact_${type} (tid, uid, time) VALUES ",
            "<foreach collection='list' item='item' separator=','>",
            "(#{item.tid}, #{item.uid}, #{item.time})",
            "</foreach>",
            "</script>"
    })
    void addInteract(@Param("list") List<Interact> interactList, @Param("type") String type);

    @Delete({
            "<script>",
            "DELETE FROM interact_${type} WHERE ",
            "<foreach collection='list' item='item' open='' separator=' OR ' close=''>",
            "(tid = #{item.tid} and uid = #{item.uid})",
            "</foreach>",
            "</script>"
    })
    void deleteInteract(@Param("list") List<Interact> interactList, @Param("type") String type);

    @Delete({
            "DELETE FROM interact_${type} " +
                    "WHERE tid = #{tid}"
    })
    void deleteOneInteract(@Param("tid") String tid, @Param("type") String type);

    @Select(
            "SELECT COUNT(*) AS record_count " +
                    "FROM interact_${type} " +  // 使用 ${} 来动态拼接表名
                    "WHERE tid = #{tid} AND uid = #{uid}"
    )
    int hasUserInteract(@Param("tid") Integer tid, @Param("uid") Integer uid, @Param("type") String type);

    @Select(
            "SELECT COUNT(*) AS record_count " +
                    "FROM interact_${type} " +  // 使用 ${} 来动态拼接表名
                    "WHERE tid = #{tid}"
    )
    int getInteractCount(@Param("tid") Integer tid,  @Param("type") String type);

    @Select(
            "SELECT * FROM interact_collect left join topic " +
                    "on tid = topic.id " +
                    "where interact_collect.uid = #{uid}"
    )
    List<Topic> collectTopics(@Param("uid") int uid);
}

