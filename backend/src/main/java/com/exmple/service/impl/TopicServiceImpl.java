package com.exmple.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exmple.entity.dto.*;
import com.exmple.entity.vo.request.AddCommentVo;
import com.exmple.entity.vo.request.TopicCreateVo;
import com.exmple.entity.vo.response.CommentVo;
import com.exmple.entity.vo.response.TopTopicVo;
import com.exmple.entity.vo.response.TopicDetailVo;
import com.exmple.entity.vo.response.TopicPreviewVo;
import com.exmple.mapper.*;
import com.exmple.service.NotificationService;
import com.exmple.service.TopicService;
import com.exmple.utils.Const;
import com.exmple.utils.FlowUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public  class TopicServiceImpl extends ServiceImpl<TopicMapper, Topic> implements TopicService {

    @Resource
    TopicTypeMapper topicTypeMapper;

    @Resource
    AccountMapper accountMapper;
    @Resource
    AccountDetailsMapper accountDetailsMapper;

    @Resource
    FlowUtils flowUtils;

    @Resource
    StringRedisTemplate template;

    @Resource
    TopicCommentMapper topicCommentMapper;

    @Resource
    NotificationService notificationService;

    //@PostConstruct 注解的方法将在依赖注入完成后，且对象构造器调用后自动执行。
    // 也就是说，这个方法会在 Spring 管理的 Bean 被完全初始化后被调用，适合用于一些初始化操作。
    /*private final Set<Integer> types = this.getAllTopicType()
            .stream()
            .map(TopicType::getId)
            .collect(Collectors.toSet());*/
    private Set<Integer> types = null;
    @PostConstruct
    private void initTypes() {
        types = this.getAllTopicType()
                .stream()
                .map(TopicType::getId)
                .collect(Collectors.toSet());
    }


    @Override
    public List<TopicType> getAllTopicType() {
        return topicTypeMapper.selectList(null);
    }

    @Override
    public String creatTopic(int Uid,TopicCreateVo topicCreateVo) {
        if (!textLimit(topicCreateVo.getContent(),20000)){
            return "发帖失败，请减少字数";
        }
        if (!types.contains(topicCreateVo.getType())){
            return "非法文章类型";
        }
        String key = Const.FLOW_FORUM_TOPIC+Uid;
        if (!flowUtils.limitForumCheck(key,10,3600))
            return "发文次数超过限制";
        Topic topic = new Topic();
        topic.setUid(Uid);
        topic.setType(topicCreateVo.getType());
        topic.setTitle(topicCreateVo.getTitle());
        topic.setContent(topicCreateVo.getContent().toJSONString());
        topic.setTime(new Date());
        if (this.save(topic)){
            // 获取所有符合指定模式的 keys
            Set<String> keys = template.keys(Const.FORUM_TOPIC_PREVIEW_CACHE + "*");
            if (keys != null && !keys.isEmpty()) {
                // 批量删除匹配的 keys
                template.delete(keys);
            }
            return null;
        }else {
            return "内部错误，请联系管理员";
        }
    }

    @Override
    public String updateTopic(int userId, TopicCreateVo topicCreateVo) {
        if (!textLimit(topicCreateVo.getContent(),20000)){
            return "发帖失败，请减少字数";
        }
        if (!types.contains(topicCreateVo.getType())){
            return "非法文章类型";
        }
        Topic topic = new Topic();
        topic.setUid(userId);
        topic.setType(topicCreateVo.getType());
        topic.setTitle(topicCreateVo.getTitle());
        topic.setContent(topicCreateVo.getContent().toJSONString());
        topic.setId(topicCreateVo.getId());
        baseMapper.updateById(topic);
        return null;
    }

    @Override
    public List<TopicPreviewVo> listTopicByPage(int pageNumber, int type) {
        // 构造 Redis 缓存键
        String key = Const.FORUM_TOPIC_PREVIEW_CACHE + pageNumber + ":" + type;

        // 尝试从 Redis 缓存中获取数据
        String cachedData = template.opsForValue().get(key);
        List<TopicPreviewVo> list = null;
        if (cachedData != null) {
            // 如果缓存中有数据，反序列化为 List<TopicPreviewVO>
            list = JSONArray.parseArray(cachedData, TopicPreviewVo.class);
            if (list != null && !list.isEmpty()) {
                return list;
            }
        }
        // 如果缓存中没有数据，从数据库中获取
        Page<Topic> page = Page.of(pageNumber, 10);
        if (type == 0)
            baseMapper.selectPage(page, Wrappers.<Topic>query().orderByDesc("time"));
        else
            baseMapper.selectPage(page, Wrappers.<Topic>query().eq("type", type).orderByDesc("time"));

        List<Topic> topics = page.getRecords();
        if (topics.isEmpty()) return null;

        // 将 Topic 列表转换为 TopicPreviewVO 列表
        list = topics.stream().map(this::resolveToPreview).collect(Collectors.toList());

        // 将转换后的数据保存到 Redis 缓存中，设置过期时间为 60 秒
        template.opsForValue().set(key, JSONArray.toJSONString(list), 120, TimeUnit.SECONDS);
        return list;
    }

    @Override
    public List<TopTopicVo> listTopTopic() {
        List<Topic> topics = baseMapper.selectList(
                Wrappers.<Topic>query().eq("top",1).orderByDesc("time"));
        if (topics.isEmpty()) return null;
        return topics.stream().map(topic -> {
            TopTopicVo vo = new TopTopicVo();
            BeanUtils.copyProperties(topic, vo);
            return vo;
        }).toList();
    }

    @Override
    public TopicDetailVo getTopicDetail(int tId,int uid) {
        TopicDetailVo vo = new TopicDetailVo();
        Topic topic = baseMapper.selectById(tId);
        if (topic == null) {
            log.error("Topic not found with ID: {}"+ tId);
            return null;
        }
        BeanUtils.copyProperties(topic, vo);
        TopicDetailVo.User user = new TopicDetailVo.User();
        Account account = accountMapper.selectById(topic.getUid());
        if (account != null) {
            BeanUtils.copyProperties(account, user);
        } else {
            log.warn("Account not found for UID: {}"+ topic.getUid());
        }
        AccountDetails accountDetails = accountDetailsMapper.selectById(topic.getUid());
        if (accountDetails != null) {
            BeanUtils.copyProperties(accountDetails, user);
        } else {
            log.warn("Account details not found for UID: {}"+ topic.getUid());
        }
        vo.setUser(user);
        TopicDetailVo.Interact interact = new TopicDetailVo.Interact(
                hasInteract(tId, uid, "like"),
                hasInteract(tId, uid, "collect")
        );
        vo.setInteract(interact);
        vo.setComments(topicCommentMapper.selectCount(Wrappers.<Comment>query().eq("tid",tId)));
        return vo;
    }

    @Override
    public void deleteTopic(int tid, int uid) {
        int deletedRows = baseMapper.delete(Wrappers.<Topic>query()
                .eq("id", tid)
                .eq("uid", uid));

        if (deletedRows > 0) { // 确保帖子属于该用户并成功删除
            baseMapper.deleteOneInteract(String.valueOf(tid), "collect");
            baseMapper.deleteOneInteract(String.valueOf(tid), "like");
            topicCommentMapper.delete(Wrappers.<Comment>query().eq("tid", tid));
        }
    }


    @Override
    public void interact(Interact interact, boolean state) {
        String type = interact.getType();
        String stateV = String.valueOf(state);
        synchronized (type.intern()){
            template.opsForHash().put(type,interact.toKey(),stateV);
            this.saveInteractSchedule(type);
        }
    }

    @Override
    public List<TopicPreviewVo> listUssrTopic(int uid) {
        List<Topic> topics = baseMapper.selectList(Wrappers.<Topic>query().eq("uid",uid));
        return topics.stream().map(topic -> {
            TopicPreviewVo vo = new TopicPreviewVo();
            BeanUtils.copyProperties(topic, vo);
            return vo;
        }).toList();
    }

    @Override
    public List<TopicPreviewVo> listTopicByCollect(int uid) {
        List<Topic> topics = baseMapper.collectTopics(uid);
        return topics.stream().map(topic -> {
            TopicPreviewVo vo = new TopicPreviewVo();
            BeanUtils.copyProperties(topic, vo);
            return vo;
        }).toList();
    }

    @Override
    public String creatComment(AddCommentVo vo, int uid) {
        if (!textLimit(JSONObject.parseObject(vo.getContent()),1000)){
            return "评论失败，请减少字数";
        }
        String key = Const.FLOW_FORUM_TOPIC_COMMENT+uid;
        if (!flowUtils.limitForumCheck(key,5,60))
            return "评论次数超过限制";
        Comment comment = new Comment();
        BeanUtils.copyProperties(vo, comment);
        comment.setUid(uid);
        comment.setTime(new Date());
        topicCommentMapper.insert(comment);
        Account account = accountMapper.selectById(uid);
        Topic topic = baseMapper.selectById(vo.getTid());
        if(vo.getQuote() > 0) {
            Comment com = topicCommentMapper.selectById(vo.getQuote());
            if(!Objects.equals(account.getId(), com.getUid())) {
                notificationService.addNotification(
                        com.getUid(),
                        "您有新的帖子评论回复",
                        account.getUsername()+" 回复了你发表的评论，快去看看吧！",
                        "success", "/index/topic-detail/"+com.getTid()
                );
            }
        } else if (!Objects.equals(account.getId(), topic.getUid())) {
            notificationService.addNotification(
                    topic.getUid(),
                    "您有新的帖子回复",
                    account.getUsername()+" 回复了你发表主题: "+topic.getTitle()+"，快去看看吧！",
                    "success", "/index/topic-detail/"+topic.getId()
            );
        }
        return null;
    }

    @Override
    public List<CommentVo> listComment(int tid, int pageNumber) {
        Page<Comment> page = Page.of(pageNumber, 10);
        topicCommentMapper.selectPage(page, Wrappers.<Comment>query().eq("tid",tid));
        List<Comment> comments = page.getRecords();
        return comments.stream().map(this::resolveToCommentVo).toList();
    }

    @Override
    public void deleteComment(int cid, int uid) {
         Comment comment =  topicCommentMapper.selectById(cid);
         if (comment == null) return;
         if (uid == comment.getUid()) {
             topicCommentMapper.deleteById(cid);

         }
    }

    @Override
    public List<TopicPreviewVo> listTopicBySearch(String search) {
        List<Topic> topics =  baseMapper.selectList(Wrappers.<Topic>query()
                .like("title",search).or()
                .like("content",search)
                .orderByDesc("time")
        );
        return topics.stream().map(this::resolveToPreview).toList();
    }

    private CommentVo resolveToCommentVo(Comment comment) {
        CommentVo vo = new CommentVo();
        BeanUtils.copyProperties(comment, vo);
        CommentVo.User user = new CommentVo.User();
        Account account = accountMapper.selectById(comment.getUid());
        AccountDetails accountDetails = accountDetailsMapper.selectById(comment.getUid());
        if (account != null)
            BeanUtils.copyProperties(account,user);
        if (accountDetails != null)
             BeanUtils.copyProperties(accountDetails, user);
        vo.setUser(user);
        if (comment.getQuote() > 0){
            Comment comment1 =  topicCommentMapper.selectById(comment.getQuote());
            if (comment1 == null) {
                vo.setQuote("评论已删除...");
                return vo;
            }
            JSONObject jsonObject = JSONObject.parseObject(comment1.getContent());
            // 获取数组中的第一个对象
            String text = jsonObject.getJSONArray("ops").getJSONObject(0).getString("insert");
            // 获取前 10 个字符
            String result = text.length() >= 10 ? text.substring(0, 10) : text;
            vo.setQuote(accountMapper.selectById(comment1.getUid()).getUsername()+" : "
                    +result+" ...");
        }
        return vo;
    }

    private boolean hasInteract(int tid,int uid ,String type) {
        String key = tid + ":" + uid;
        if (template.opsForHash().hasKey(type,key))
            return Boolean.parseBoolean(template.opsForHash().get(type,key).toString());
        return baseMapper.hasUserInteract(tid,uid,type) > 0;
    }


    // 用于存储每个 'type' 对应的状态（是否正在执行保存操作）
    private final Map<String, Boolean> state = new HashMap<>();
    // 创建一个定时任务线程池，最大线程数为2
    ScheduledExecutorService service = Executors.newScheduledThreadPool(2);
    //用于安排保存交互操作的定时任务。确保同一 'type' 的交互操作在短时间内不会被重复触发。
    private void saveInteractSchedule(String type) {
        // 如果当前 'type' 对应的状态是 false，表示没有正在进行的保存操作
        if (!state.getOrDefault(type, false)) {
            // 将该 'type' 的状态设置为 true，表示正在进行保存操作
            state.put(type, true);
            // 使用线程池安排一个定时任务，延迟 3 秒后执行保存操作
            service.schedule(() -> {
                // 执行保存操作
                this.saveInteract(type);
                // 操作完成后，将该 'type' 对应的状态重置为 false，表示保存操作已完成
                state.put(type, false);
            }, 3, TimeUnit.SECONDS); // 延迟 3 秒执行保存任务
        }
    }


    private void saveInteract(String type) {
        synchronized (type.intern()){
            List<Interact> check = new LinkedList<>();
            List<Interact> uncheck = new LinkedList<>();
            template.opsForHash().entries(type).forEach((k,v)->{
                if (Boolean.parseBoolean(v.toString()))
                    check.add(Interact.fromKey((String) k,type));
                else
                    uncheck.add(Interact.fromKey((String) k,type));
            });
            if(!check.isEmpty()){
                baseMapper.addInteract(check,type);
            }
            if(!uncheck.isEmpty()){
                baseMapper.deleteInteract(uncheck,type);
            }
            template.delete(type);
         }

    }


    private TopicPreviewVo resolveToPreview(Topic topic) {
        TopicPreviewVo vo = new TopicPreviewVo();
        BeanUtils.copyProperties(accountMapper.selectById(topic.getUid()), vo);
        BeanUtils.copyProperties(topic, vo);
        vo.setLike(baseMapper.getInteractCount(topic.getId(), "like"));
        vo.setCollect(baseMapper.getInteractCount(topic.getId(), "collect"));
        List<String> images = new ArrayList<>();
        StringBuilder previewText = new StringBuilder();
        JSONArray ops = JSONObject.parseObject(topic.getContent()).getJSONArray("ops");
        this.shortContent(ops, previewText, obj -> images.add(obj.toString()));
        vo.setText(previewText.length() > 300 ? previewText.substring(0, 300) : previewText.toString());
        vo.setImages(images);
        return vo;
    }

    private void shortContent(JSONArray ops, StringBuilder previewText, Consumer<Object> imageHandler){
        for (Object op : ops) {
            Object insert = JSONObject.from(op).get("insert");
            if(insert instanceof String text) {
                if(previewText.length() >= 300) continue;
                previewText.append(text);
            } else if(insert instanceof Map<?, ?> map) {
                Optional.ofNullable(map.get("image")).ifPresent(imageHandler);
            }
        }
    }


    private  boolean textLimit(JSONObject jsonObject,int max){
        if(jsonObject == null) return false;
        long length = 0;
        for(Object op : jsonObject.getJSONArray("ops")){
            length += JSONObject.from(op).getString("insert").length();
            if(length > max) return false;
        }
        return true;
    }
}
