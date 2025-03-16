<script setup>
import {get, logout} from "@/net";
import router from "@/router";
import {userStore} from "@/store"
import {reactive, ref} from "vue";
import {
  Location,
  Setting,
  ChatDotSquare,
  Box,
  Cherry,
  Search,
  SwitchButton,
  Suitcase, Bell, Check, FolderOpened, ArrowRightBold
} from '@element-plus/icons-vue'
import LightCard from "@/components/LightCard.vue";

import axios from "axios";
import TopicSearch from "@/components/TopicSearch.vue";
import TopicUserList from "@/components/TopicUserList.vue";
const store = userStore();
const loading = ref(true);



const notification = ref([])

get('api/user/info',(data)=>{
  store.user = data
  loading.value = false
})
const loadNotification =
    () => get('/api/notification/list', data => notification.value = data)
loadNotification()

function userLogout() {
  logout(() => router.push("/"))
}

function confirmNotification(id, url) {
  get(`/api/notification/delete?id=${id}`, () => {
    loadNotification()
    window.open(url)
  })
}

function deleteAllNotification() {
  get(`/api/notification/delete-all`, loadNotification)
}

const searchInput = ref({
  text: '',
  type: '校园圈子',
});

const topicSearchVisible = ref(false); // 控制抽屉显示的状态
const topics = ref({ list: [] }); // 存储搜索结果

// 打开抽屉
function openDrawer() {
  topicSearchVisible.value = true;  // 设置为 true 打开抽屉
  searchTopics(searchInput.value.text);  // 调用 searchTopics 方法进行搜索
}

// 关闭抽屉的处理方法
function handleClose() {
  topicSearchVisible.value = false; // 设置为 false 关闭抽屉
}

// 搜索方法
function searchTopics(searchText) {
  get(`/api/forum/search?search=${searchText}`, (data) => {
    topics.value.list = data; // 更新搜索结果
  });
}

</script>

<template>
  <div style="max-width: 500px">
    <topic-search
        :show="topicSearchVisible"
        :search-text="searchInput.text"
        :topics="topics"
        @close="handleClose"
    />
  </div>

  <div class="main-container" v-loading="loading" element-loading-text="正在加载，请稍后...">
    <el-container style="height: 100%">
      <el-header class="content-header">
        <el-image src="http://localhost:8080/api/image/avatar/img.png"/>
        <div style="flex: 1;padding: 0 20px;text-align: center">
          <el-input v-model="searchInput.text"
                    style="width: 125%; max-width: 500px"
                    placeholder="搜索论坛相关内容..."
                    @keydown.enter="openDrawer">
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
              <template #append>
                <el-select style="width: 100px;" v-model="searchInput.type">
                  <el-option value="1" label="校园圈子"/>
                  <el-option value="2" label="表白墙"/>
                  <el-option value="3" label="二手集市"/>
                </el-select>
              </template>
            </el-input>
          </div>

        <div class="user-info">
          <el-popover placement="bottom" :width="350" trigger="click">
            <template #reference>
              <el-badge style="margin-right: 15px" is-dot :hidden="!notification.length">
                <div class="notification">
                  <el-icon><Bell/></el-icon>
                  <div style="font-size: 10px">消息</div>
                </div>
              </el-badge>
            </template>
            <el-empty :image-size="80" description="暂时没有未读消息哦~" v-if="!notification.length"/>
            <el-scrollbar :max-height="500" v-else>
              <light-card v-for="item in notification" class="notification-item"
                          @click="confirmNotification(item.id, item.url)">
                <div>
                  <el-tag size="small" :type="item.type">消息</el-tag>&nbsp;
                  <span style="font-weight: bold">{{item.title}}</span>
                </div>
                <el-divider style="margin: 7px 0 3px 0"/>
                <div style="font-size: 13px;color: grey">
                  {{item.content}}
                </div>
              </light-card>
            </el-scrollbar>
            <div style="margin-top: 10px">
              <el-button size="small" type="info" :icon="Check" @click="deleteAllNotification"
                         style="width: 100%" plain>清除全部未读消息</el-button>
            </div>
          </el-popover>
          <div class="profile">
            <div>{{store.user.username}}</div>
            <div>{{store.user.email}}</div>
          </div>
          <el-dropdown>
            <el-avatar class="avatar" size="default" :src="store.avatarUrl" />
            <template #dropdown>
              <el-dropdown-item>
                <el-icon><Setting/></el-icon>
                个人设置
              </el-dropdown-item>
              <el-dropdown-item>
                <el-icon><Suitcase /></el-icon>
                账号安全
              </el-dropdown-item>
              <el-dropdown-item @click="userLogout" divided>
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-container>
        <el-aside width="230px">
          <el-scrollbar style="height: calc(100vh - 55px)">
            <el-menu
                router
                :default-active="$route.path"
                :default-openeds="['1','2']"
              style="height: 100%">
              <el-sub-menu index="1">
                <template #title>
                  <el-icon><location/></el-icon>
                  <span><b>校园圈子</b></span>
                </template>
                <el-menu-item index="/index">
                  <template #title>
                    <span>圈子首页</span>
                  </template>
                </el-menu-item>
                <el-menu-item>
                  <template #title>
                    <span>吐槽广场</span>
                  </template>
                </el-menu-item>
                <el-menu-item>
                  <template #title>
                    <span>朋辈互助</span>
                  </template>
                </el-menu-item>
                <el-menu-item>
                  <template #title>
                    <span>失物招领</span>
                  </template>
                </el-menu-item>
                <el-menu-item>
                  <template #title>
                    <span>新生专区</span>
                  </template>
                </el-menu-item>
              </el-sub-menu>
              <el-menu-item index="2">
                <template #title>
                  <el-icon><Cherry /></el-icon>
                  <span><b>表白墙</b></span>
                </template>
              </el-menu-item>
              <el-sub-menu index="3">
                <template #title>
                  <el-icon><Box/></el-icon>
                  <span><b>二手集市</b></span>
                </template>
                <el-menu-item>
                  <template #title>
                    <span>集市首页</span>
                  </template>
                </el-menu-item>
                <el-menu-item>
                  <template #title>
                    <span>书籍资料</span>
                  </template>
                </el-menu-item>
                <el-menu-item>
                  <template #title>
                    <span>日常用品</span>
                  </template>
                </el-menu-item>
                <el-menu-item>
                  <template #title>
                    <span>数码电子</span>
                  </template>
                </el-menu-item>
                <el-menu-item>
                  <template #title>
                    <span>鞋包服饰</span>
                  </template>
                </el-menu-item>
              </el-sub-menu>
              <el-menu-item index="4">
                <el-icon><ChatDotSquare/></el-icon>
                <span slot="title"><b>消息列表</b></span>
              </el-menu-item>
              <el-sub-menu index="5">
                <template #title>
                  <el-icon><setting/></el-icon>
                  <span><b>个人设置</b></span>
                </template>
                <el-menu-item index="/index/user-setting">
                  <template #title>
                    <span>个人信息设置</span>
                  </template>
                </el-menu-item>
                <el-menu-item index="/index/safety-setting">
                  <template #title>
                    <span>账号安全设置</span>
                  </template>
                </el-menu-item>
              </el-sub-menu>
            </el-menu>
          </el-scrollbar>
        </el-aside>
        <el-main  class="main-container-page">
          <el-scrollbar style="height: calc(100vh - 60px)">
            <router-view v-slot="{ Component }">
              <transition name="el-fade-linear" mode="out-in">
                <component :is="Component" style="height: 100%;"/>
              </transition>
            </router-view>
          </el-scrollbar>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<style scoped>
.el-drawer--bottom {
  transform: translateY(0); /* 底部展开 */
}
.main-container-page{
  padding: 0;
  background-color: #eaf3f3;
}
.notification {
  font-size: 22px;
  line-height: 14px;
  text-align: center;
  transition: color .3s;
}
.main-container{
  height: 100vh;
  width: 100vw;
}

.content-header{
  display: flex;
  line-height: 60px;
}

.user-info{
  width: 200px;
  display: flex;
  margin-right: 20px;
  justify-content: flex-end;
  align-items: center;

  .avatar:hover{
    cursor: pointer;
  }

  .profile{
    text-align:right;
    margin-right: 20px;

    :first-child{
      font-size: 15px;
      color: #b9d2da;
      font-weight: bold;
      line-height: 20px;
    }
    :last-child{
      font-size: 10px;
      color: #dce5ef;
      line-height: 15px;
    }
  }
}


</style>