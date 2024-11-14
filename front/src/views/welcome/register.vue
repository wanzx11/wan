

<template>
  <div style="text-align: center; margin:0 20px;">
    <div style="margin-top: 120px">
      <div STYLE="font-size: 20px;font-weight: bold">注册新用户</div>
      <div STYLE="font-size: 14px;color: grey ">欢迎使用xxxxx</div>
    </div>
    <div style="margin-top: 50px;">
      <el-form :model="form" :rules="rules">
        <el-form-item>
          <el-input id="username" v-model="form.username" type="text" placeholder="请输入用户名">
            <template  #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-input id="email" v-model="form.email"  type="text" placeholder="请输入邮箱">
            <template  #prefix>
              <el-icon><Message /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-input id="code" v-model="form.code" type="text" placeholder="验证码">
            <template  #prefix>
              <el-icon><ChatDotSquare /></el-icon>
            </template>
            <template #append style="color: green" @click="">
              获取验证码
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
        <el-form-item>
          <el-input id="password" v-model="form.password_repeat" type="text" placeholder="再次输入密码">
            <template  #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>

          <el-link type="info" style="margin-top: 10px" href="/"> 返回登录 </el-link>

        </el-form-item>
      </el-form>
    </div>

    <div>
      <el-button  type="success" style="width: 200px" plain >立即注册</el-button>
    </div>
  </div>
</template>

<script setup>
import { User } from '@element-plus/icons-vue'
import { Lock } from '@element-plus/icons-vue'
import{ ChatDotSquare } from '@element-plus/icons-vue'
import {Message} from '@element-plus/icons-vue'
import {reactive,ref} from "vue";
import router from "@/router/index.js";

/*
import {reactive, ref} from "vue";
import {ElMessage} from "element-plus";
import {get, post} from "@/net";*/

const form = reactive({
  username: '',
  password: '',
  password_repeat: '',
  email: '',
  code: ''
})

const validateUsername = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入用户名'))
  } else if(!/^[a-zA-Z0-9\u4e00-\u9fa5]+$/.test(value)){
    callback(new Error('用户名不能包含特殊字符，只能是中文/英文'))
  } else {
    callback()
  }
}

const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error("两次输入的密码不一致"))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { validator: validateUsername, trigger: ['blur', 'change'] },
    { min: 2, max: 8, message: '用户名的长度必须在2-8个字符之间', trigger: ['blur', 'change'] },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 16, message: '密码的长度必须在6-16个字符之间', trigger: ['blur', 'change'] }
  ],
  password_repeat: [
    { validator: validatePassword, trigger: ['blur', 'change'] },
  ],
  email: [
    { required: true, message: '请输入邮件地址', trigger: 'blur' },
    {type: 'email', message: '请输入合法的电子邮件地址', trigger: ['blur', 'change']}
  ],
  code: [
    { required: true, message: '请输入获取的验证码', trigger: 'blur' },
  ]
}

const formRef = ref()
const isEmailValid = ref(false)
const coldTime = ref(0)

const onValidate = (prop, isValid) => {
  if(prop === 'email')
    isEmailValid.value = isValid
}

const register = () => {
  formRef.value.validate((isValid) => {
    if(isValid) {
      post('/api/auth/register', {
        username: form.username,
        password: form.password,
        email: form.email,
        code: form.code
      }, () => {
        ElMessage.success('注册成功，欢迎加入我们')
        router.push("/")
      })
    } else {
      ElMessage.warning('请完整填写注册表单内容！')
    }
  })
}

const validateEmail = () => {
  coldTime.value = 60
  get(`/api/auth/ask-code?email=${form.email}&type=register`, () => {
    ElMessage.success(`验证码已发送到邮箱: ${form.email}，请注意查收`)
    const handle = setInterval(() => {
      coldTime.value--
      if(coldTime.value === 0) {
        clearInterval(handle)
      }
    }, 1000)
  }, undefined, (message) => {
    ElMessage.warning(message)
    coldTime.value = 0
  })
}
</script>

<style scoped>

</style>