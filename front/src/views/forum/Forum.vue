<script setup>
import {get} from "@/net/index.js";
import {userStore} from "@/store/index.js";
const store = userStore()

get('/api/forum/types', data => {
  const array = []
  array.push({name: '全部', id: 0, color: 'linear-gradient(45deg, white, red, orange, gold, green, blue)'})
  data.forEach(d => array.push(d))
  store.forum.types = array
})
</script>

<template>
  <div>
    <router-view v-slot="{ Component }">
      <transition name="fade" mode="out-in">
        <keep-alive include="TopicList">
          <component :is="Component" />
        </keep-alive>
      </transition>
    </router-view>
  </div>
</template>
