<script>
export default {
  name: "WelcomeView"
}
</script>

<template>
<div style="width: 100vw; height: 100vh;overflow: hidden;display: flex">
  <div style="flex: 1;background-color: black">
    <el-image style="width: 100%;height: 100%;" fit="cover"
        src="https://th.bing.com/th/id/R.d19a2d11cb386f690ce9b8863d53a56c?rik=JXY5K3Cqv9TmXA&riu=http%3a%2f%2fp1.so.qhmsg.com%2ft01cb386f690ce9b886.jpg&ehk=jRKYecPgofEDXQqAKx5w%2f67mDTdedP%2bjKMZK%2fHNZyTw%3d&risl=&pid=ImgRaw&r=0"></el-image>
  </div>
  <div class="welcome-text">
    <div>校园论坛</div>
    <div>哈哈哈 </div>
  </div>
  <div class="right-card">
    <router-view/>
  </div>
</div>

</template>

<style scoped>
.welcome-text{
  position: absolute;
  top: 30px;
  left: 30px;
  color: white;

}

.right-card{
  width: 30%;
  z-index: 1;
  background-color: white;
}
</style>