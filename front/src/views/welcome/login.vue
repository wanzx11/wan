<script setup>
import { User } from '@element-plus/icons-vue'
import { Lock } from '@element-plus/icons-vue'
import {reactive} from "vue";
import {login} from "@/net/index.js";
import router from "@/router/index.js";


const form = reactive({
  username:'',
  password:'',
  remember:false
})

const rule={
  username: [
    {required:true,message: '请输入用户名'}
  ],
  passes: [
      {required:true,message:'请输入密码'}
  ]
}

function  userLogin(){
  login(form.username,form.password,form.remember, ()=>{
    router.push('/test')
  });
}


</script>

<template>
  <div style="text-align: center; margin:0 20px;">
    <div style="margin-top: 120px">
      <div STYLE="font-size: 20px;font-weight: bold">登录</div>
      <div STYLE="font-size: 14px;color: grey ">欢迎登录xxxxx</div>
   </div>
    <div style="margin-top: 50px;" >
      <el-form :model="form" :rules="rule">
        <el-form-item>
          <el-input id="username" v-model="form.username" type="text" placeholder="用户名/邮箱">
            <template  #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-input id="password" v-model="form.password" type="text" placeholder="密码">
            <template  #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-row>
          <el-col :span="12" style="text-align: left;">
            <el-checkbox v-model="form.remember" label="记住我" />
          </el-col>
          <el-col :span="12" style="text-align: right;">
            <el-link type="info" @click="router.push('/forget')">忘记密码？</el-link>
          </el-col>
        </el-row>
      </el-form>
    </div>
    <div  style="margin-top: 20px">
      <el-button @click="userLogin" type="success" style="width: 200px" plain >立即登录</el-button>
    </div>
    <el-divider>
      <span style="font-size: 13px;color: grey">没有账号</span>
    </el-divider>
    <div>
      <el-button @click="router.push('/register')" type="info" style="width: 200px" plain >立即注册</el-button>
    </div>
  </div>
</template>



<style scoped>

</style>