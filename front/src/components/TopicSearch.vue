<script setup>
import { defineProps, defineEmits } from 'vue';
import router from "@/router/index.js";
import LightCard from "@/components/LightCard.vue";
import {CircleCheck, Clock, Star} from "@element-plus/icons-vue";
import {userStore} from "@/store/index.js";
import TopicTag from "@/components/TopicTag.vue";

const store = userStore()

const props = defineProps({
  show: Boolean,       // 控制抽屉显示
  searchText: String,  // 搜索文本
  topics: Object       // 传递搜索结果
});

const emit = defineEmits(['close']);

// 关闭抽屉
function closeDrawer() {
  emit('close');
}
function pushDetail(item) {
  closeDrawer();
  router.push('/index/topic-detail/'+item.id)
}
</script>

<template>
  <el-drawer :model-value="show" @close="closeDrawer"
             size="60%"
             title="搜索列表"
             direction="ltr">
    <div v-if="topics.list.length">
      <!-- 渲染搜索结果 -->
        <light-card v-for="item in topics.list"
                    @click="pushDetail(item)"
                    :key="item.id"
                    class="topic-card"
                    style="background-color: #fcfce5;margin-bottom: 10px">
          <div >
            <div style="display: flex">
              <div>
                <el-avatar :size="30" :src="store.avatarUserUrl(item.avatar)"/>
              </div>
              <div style="margin-left: 7px;transform: translateY(-2px)">
                <div style="font-size: 13px;font-weight: bold">{{item.username}}</div>
                <div style="font-size: 12px;color: grey">
                  <el-icon><Clock/></el-icon>
                  <div style="margin-left: 2px;display: inline-block;transform: translateY(-2px)">
                    {{new Date(item.time).toLocaleString()}}
                  </div>
                </div>
              </div>
            </div>

            <div style="margin-top: 5px">
              <topic-tag :type="item.type"/>
              <span style="font-weight: bold;margin-left: 7px">{{item.title}}</span>
            </div>
            <div class="topic-content">{{item.text}}</div>
            <div style="display: grid;grid-template-columns: repeat(3, 1fr);grid-gap: 10px">
              <el-image class="topic-image" v-for="img in item.images" :src="img" fit="cover"></el-image>
            </div>
            <div style="display: flex;gap: 20px;font-size: 13px;margin-top: 10px;opacity: 0.8">
              <div>
                <el-icon style="vertical-align: middle"><CircleCheck/></el-icon> {{item.like}}点赞
              </div>
              <div>
                <el-icon style="vertical-align: middle"><Star/></el-icon> {{item.collect}}收藏
              </div>
            </div>
          </div>
      </light-card>
    </div>
    <div v-else>
      <p>没有搜索到相关内容</p>
    </div>
  </el-drawer>
</template>
<style>

.topic-card {
  padding: 15px;
  transition: scale .3s;

  &:hover {
    scale: 1.015;
    cursor: pointer;
  }

  .topic-content {
    font-size: 13px;
    color: grey;
    margin: 5px 0;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 3;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .topic-image {
    width: 100%;
    height: 100%;
    max-height: 110px;
    border-radius: 5px;
  }
}

</style>

