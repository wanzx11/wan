<script setup>
import { defineProps } from 'vue';
import { ElAvatar, ElDivider, ElIcon } from 'element-plus';
import { Male, Female } from '@element-plus/icons-vue';

// 接收传递给组件的props
const props = defineProps({
  user: {
    type: Object,
    required: true
  },
  avatarUrl: {
    type: String,
    required: true
  }
});
</script>


<template>
  <div class="user-info">
    <el-avatar :src="avatarUrl" :size="60" />
    <div>
      <div style="font-size: 18px; font-weight: bold">
        {{ user.username }}
        <span style="color: hotpink" v-if="user.gender === 1">
          <el-icon><Female /></el-icon>
        </span>
        <span style="color: dodgerblue" v-if="user.gender === 0">
          <el-icon><Male /></el-icon>
        </span>
      </div>
      <div class="desc">{{ user.email }}</div>
    </div>
    <el-divider style="margin: 10px 0" />
    <div style="text-align: left; margin: 0 5px">
      <div class="desc">微信号: {{ user.wx || '已隐藏或未填写' }}</div>
      <div class="desc">QQ号: {{ user.qq || '已隐藏或未填写' }}</div>
      <div class="desc">手机号: {{ user.phone || '已隐藏或未填写' }}</div>
    </div>
    <el-divider style="margin: 10px 0" />
    <div class="desc" style="margin: 0 5px">{{ user.desc }}</div>
  </div>
</template>

<style scoped>
.user-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px;
}

.desc {
  font-size: 14px;
  color: #666;
}
</style>
