<script setup>

import {logout} from "@/net/index.js";
import router from "@/router/index.js";

function userLogout(){
  logout(()=>{
    router.push("/");
  })
}
</script>

<template>
<view>
  sdadssadasda
  <el-button @click="userLogout">退出登录</el-button>
</view>
</template>

<style scoped>

</style>