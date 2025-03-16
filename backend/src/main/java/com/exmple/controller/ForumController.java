package com.exmple.controller;

import com.exmple.entity.RestBean;
import com.exmple.entity.dto.Interact;
import com.exmple.entity.dto.TopicType;
import com.exmple.entity.vo.request.AddCommentVo;
import com.exmple.entity.vo.request.TopicCreateVo;
import com.exmple.entity.vo.response.*;
import com.exmple.service.TopicService;
import com.exmple.service.WeatherService;
import com.exmple.utils.Const;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

@RestController
@RequestMapping("/api/forum")
public class ForumController {

    @Resource
    private WeatherService weatherService;

    @Resource
    private TopicService topicService;

    @GetMapping("/weather")
    public RestBean<WeatherVo> weather(double lon, double lat) {
        WeatherVo weatherVo = weatherService.getWeather(lon, lat);
        if (weatherVo == null) return RestBean.fail(400,"获取地理位置与天气失败");
        return RestBean.success(weatherVo);
    }

    @GetMapping("/types")
    public RestBean<List<TopicType>> getTypes() {
        return RestBean.success(topicService.getAllTopicType());
    }

    @PostMapping("/create-topic")
    public RestBean<Void> createTopic(@Valid @RequestBody TopicCreateVo topicVo,
                                      @RequestAttribute(Const.USER_ID) int userId) {
        return this.messageHandle(()->
                topicService.creatTopic(userId,topicVo));
    }

    @GetMapping("/list-topic")
    public RestBean<List<TopicPreviewVo>> getListTopic(@RequestParam @Min(0) int page,
                                                       @RequestParam @Min(0) int type) {
        return RestBean.success(topicService.listTopicByPage(page+1,type));
    }
    @GetMapping("/search")
    public RestBean<List<TopicPreviewVo>> getTopicByTitle(@RequestParam String search) {
        return RestBean.success(topicService.listTopicBySearch(search));
    }

    @GetMapping("/top-topic")
    public RestBean<List<TopTopicVo>> getTopTopic(){
        return RestBean.success(topicService.listTopTopic());
    }

    @GetMapping("/topic")
    public RestBean<TopicDetailVo> getTopicDetail(@RequestParam @Min(0)  int tid,
                                                  @RequestAttribute(Const.USER_ID) int userId) {
        return RestBean.success(topicService.getTopicDetail(tid,userId));
    }


    @PostMapping("/update-topic")
    public RestBean<Void> updateTopic(@Valid @RequestBody TopicCreateVo topicVo,
                                               @RequestAttribute(Const.USER_ID) int userId){
        return this.messageHandle(()->
                topicService.updateTopic(userId,topicVo));
    }

    @GetMapping("/delete-topic")
    public RestBean<Void> deleteTopic(@RequestParam @Min(0) int tid,
                                      @RequestAttribute(Const.USER_ID) int userId) {
        topicService.deleteTopic(tid,userId);
        return RestBean.success();
    }


    @GetMapping("/interact")
    public RestBean<Void> interact(@RequestParam @Min(0) int tid,
                                   @RequestParam @Pattern(regexp = "(like|collect)" ) String type,
                                   @RequestParam boolean state,
                                   @RequestAttribute(Const.USER_ID) int userId) {
        topicService.interact(new Interact(tid,userId,new Date(),type),state);
        return RestBean.success();
    }

    @GetMapping("/user-topic")
    public RestBean<List<TopicPreviewVo>> getUserTopic(@RequestAttribute(Const.USER_ID) int userId){
        return RestBean.success(topicService.listUssrTopic(userId));
    }

    @GetMapping("/collects")
    public RestBean<List<TopicPreviewVo>> getCollects(@RequestAttribute(Const.USER_ID) int userId){
        return RestBean.success(topicService.listTopicByCollect(userId));
    }

    @PostMapping("/add-comment")
    public RestBean<Void> addComment(@Valid @RequestBody AddCommentVo vo,
                                     @RequestAttribute(Const.USER_ID) int userId
                                     ){
        return this.messageHandle(()->
                topicService.creatComment(vo,userId));
    }
    @GetMapping("/comment")
    public RestBean<List<CommentVo>> getComment(@RequestParam @Min(0)  int tid,
                                                @RequestParam @Min(0) int page){
        return RestBean.success(topicService.listComment(tid,page+1));
    }

    @GetMapping("/delete-comment")
    public RestBean<Void> deleteComment(@RequestParam @Min(0)  int id,
                                        @RequestAttribute(Const.USER_ID) int userId){
        topicService.deleteComment(id,userId);
        return RestBean.success();
    }


    private RestBean<Void> messageHandle(Supplier<String> action){
        String message = action.get();
        if (message == null)
            return RestBean.success();
        else return RestBean.fail(400, message);
    }
}
