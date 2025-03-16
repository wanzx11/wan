<script setup>
import {get} from "@/net";
import {ref} from "vue";
import LightCard from "@/components/LightCard.vue";
import router from "@/router";
import TopicTag from "@/components/TopicTag.vue";
import {ElMessage, ElMessageBox} from "element-plus";

defineProps({
  show: Boolean
})

const emit = defineEmits(['close'])

const list = ref([])

function init() {
  get('/api/forum/user-topic', data => list.value = data)
}

function deleteCollect(index, tid) {
  ElMessageBox.confirm(
      '此操作将永久删除该帖子, 是否继续?',
      '提示',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(() => {
    // 用户确认删除
    get(`/api/forum/delete-topic?tid=${tid}`, () => {
      ElMessage.success('删除成功！');
      list.value.splice(index, 1); // 从列表中删除该项
    });
  }).catch(() => {
    // 用户取消删除
    ElMessage.info('删除操作已取消');
  });
}
</script>

<template>
  <el-drawer :model-value="show" @close="emit('close')" @open="init" title="我的帖子列表">
    <div class="collect-list">
      <light-card v-for="(item, index) in list" class="topic-card"
                  @click="router.push(`/index/topic-detail/${item.id}`)">
        <topic-tag :type="item.type"/>
        <div class="title">
          <b>{{item.title}}</b>
        </div>
        <el-link type="danger" @click.stop="deleteCollect(index, item.id)">删除</el-link>
      </light-card>
    </div>
  </el-drawer>
</template>

<style scoped>
.collect-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.topic-card {
  background-color: rgba(128, 128, 128, 0.2);
  transition: .3s;
  display: flex;
  justify-content: space-between;

  .title {
    margin-left: 5px;
    font-size: 14px;
    flex: 1;
    white-space: nowrap;
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &:hover {
    scale: 1.02;
    cursor: pointer;
  }
}
</style>
