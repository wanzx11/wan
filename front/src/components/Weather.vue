<script setup>
defineProps({
  data: Object
})

</script>
<!--v-loading="!data.success"-->
<template>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/qweather-icons@1.3.0/font/qweather-icons.css">
  <div style="height: 160px" element-loading-text="正在加载天气信息..." v-loading="!data.success">
    <div style="display: flex; justify-content: space-between; margin: 10px 20px" v-if="data.success">
      <div style="font-size: 45px; color: grey">
        <i :class="`qi-${data.now.icon}`"></i>

      </div>
      <div style="font-weight: bold; text-align: center">
        <div style="font-size: 25px">{{data.now.temp}}℃</div>
        <div style="font-size: 15px">{{data.now.text}}</div>
      </div>
      <div style="margin-top: 13px">
        <div style="font-size: 15px">{{`${data.location.country} ${data.location.adm1}`}}</div>
        <div style="font-size: 14px; color: grey">{{`${data.location.adm2} ${data.location.name}区`}}</div>
      </div>
    </div>
    <el-divider style="margin: 10px 0"/>
    <div style="display: grid; grid-template-columns: repeat(5, 1fr); text-align: center; gap: 15px; ">
      <div v-for="item in data.daily.slice(1, 5)" :key="item.fxDate">
        <div style="font-size: 15px">{{ new Date(item.fxDate).getDate() }} 日</div>
        <div style="font-size: 30px">
          <i :class="`qi-${item.iconDay}`"></i>
        </div>
        <div style="font-size: 12px">{{item.tempMax}}℃~{{item.tempMin}}℃</div>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>
