<script setup>

import Card from "@/components/Card.vue";
import {Setting, Switch, Lock, Message, Refresh} from "@element-plus/icons-vue";
import {reactive, ref} from "vue";
import {get, post} from "@/net";
import {ElMessage} from "element-plus";
import {userStore} from "@/store";

const emailFormRef = ref()
const store = userStore()

const emailForm = reactive({
  email: '',
  code: ''
})

const coldTime = ref(0)
const isEmailValid = ref(true)


const form = reactive({
  password: '',
  new_password: '',
  new_password_repeat: ''
})
const passwordForm = reactive({
  oldPassword:'',
  newPassword:''
})

const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.new_password) {
    callback(new Error("两次输入的密码不一致"))
  } else {
    callback()
  }
}
const rules = {
  password: [
    { required: true, message: '请输入原来的密码', trigger: 'blur' }
  ],
  new_password: [
    { required: true, message: '请输入新的密码', trigger: 'blur' },
    { min: 6, max: 16, message: '密码的长度必须在6-16个字符之间', trigger: ['blur'] }
  ],
  new_password_repeat: [
    { required: true, message: '请再次输入新的密码', trigger: 'blur' },
    { validator: validatePassword, trigger: ['blur', 'change'] },
  ]
}
const formRef = ref()
const valid = ref(false)
const onValidate = (prop, isValid) => valid.value = isValid

function resetPassword(){
  formRef.value.validate(valid => {
    passwordForm.oldPassword = form.password;
    passwordForm.newPassword = form.new_password
    if(valid) {
      post('/api/auth/change-password', passwordForm, () => {
        ElMessage.success('修改密码成功！')
        formRef.value.resetFields();
      },(message) =>
          ElMessage.warning(message)
      )
    }
  })
}
function sendEmailCode() {
  emailFormRef.value.validate(isValid => {
    if (isValid) {
      coldTime.value = 60
      get(`/api/auth/ask-code?email=${emailForm.email}&type=reset-email`, () => {
        ElMessage.success(`验证码已成功发送到邮箱：${emailForm.email}，请注意查收`)
        const handle = setInterval(() => {
          coldTime.value--
          if (coldTime.value === 0) {
            clearInterval(handle)
          }
        }, 1000)
      }, (message) => {
        ElMessage.warning(message)
        coldTime.value = 0
      })
    }
  })
}

function modifyEmail() {
  if (emailForm.code.length ===0 ) {
    ElMessage.warning("请先获取验证码")
    return
  }
  emailFormRef.value.validate(isValid => {
    if (isValid) {
      post('/api/auth/reset-email', emailForm, () => {
        ElMessage.success('邮件修改成功')
        store.user.email = emailForm.email
        emailForm.code = ''
      },(message) => {
            ElMessage.warning(message)
          }
      )
    }
  })
}

const saving = ref(true)
const privacy = reactive({
  phone: false,
  wx: false,
  qq: false,
  email: false,
  gender: false
})
/*get('/api/user/privacy', data => {
  privacy.phone = data.phone
  privacy.email = data.email
  privacy.wx = data.wx
  privacy.qq = data.qq
  privacy.gender = data.gender
  saving.value = false
})
function savePrivacy(type, status){
  saving.value = true
  post('/api/user/save-privacy', {
    type: type,
    status: status
  }, () => {
    ElMessage.success('隐私设置修改成功！')
    saving.value = false
  })
}*/
</script>

<template>
  <div style="margin: auto;max-width: 600px">
    <div style="margin-top: 20px">
      <card :icon="Setting" title="隐私设置" desc="在这里设置哪些内容可以被其他人看到，请各位小伙伴注重自己的隐私" v-loading="saving">
        <div class="checkbox-list">
          <el-checkbox @change="savePrivacy('phone', privacy.phone)"
                       v-model="privacy.phone">公开展示我的手机号</el-checkbox>
          <el-checkbox @change="savePrivacy('email', privacy.email)"
                       v-model="privacy.email">公开展示我的电子邮件地址</el-checkbox>
          <el-checkbox @change="savePrivacy('wx', privacy.wx)"
                       v-model="privacy.wx">公开展示我的微信号</el-checkbox>
          <el-checkbox @change="savePrivacy('qq', privacy.qq)"
                       v-model="privacy.qq">公开展示我的QQ号</el-checkbox>
          <el-checkbox @change="savePrivacy('gender', privacy.gender)"
                       v-model="privacy.gender">公开展示我的性别</el-checkbox>
        </div>
      </card>
      <card style="margin-top: 10px" :icon="Message" title="电子邮件设置"
            describe="您可以在这里修改默认绑定的电子邮件地址">
        <el-form :rules="rules" @validate="onValidate" :model="emailForm" ref="emailFormRef"
                 label-position="top" style="margin: 0 10px 10px 10px">
          <el-form-item label="请输入新的电子邮件" prop="email">
            <el-input placeholder="电子邮件" v-model="emailForm.email"/>
          </el-form-item>
          <el-form-item prop="code">
            <el-row style="width: 100%" :gutter="10">
              <el-col :span="18">
                <el-input placeholder="请获取验证码" v-model="emailForm.code"/>
              </el-col>
              <el-col :span="6">
                <el-button type="success" style="width: 100%" :disabled="!isEmailValid || coldTime > 0"
                           @click="sendEmailCode" plain>
                  {{ coldTime > 0 ? `请稍后 ${coldTime} 秒` : '获取验证码' }}
                </el-button>
              </el-col>
            </el-row>
          </el-form-item>
          <div>
            <el-button :icon="Refresh" type="success" @click="modifyEmail">更新电子邮件</el-button>
          </div>
        </el-form>
      </card>
      <card style="margin: 20px 0" :icon="Setting"
            title="修改密码" desc="修改密码请在这里进行操作，请务必牢记您的密码">
        <el-form :rules="rules" :model="form" ref="formRef" @validate="onValidate" label-width="100" style="margin: 20px">
          <el-form-item label="当前密码" prop="password">
            <el-input type="password" :prefix-icon="Lock" v-model="form.password"
                      placeholder="当前密码" maxlength="16"/>
          </el-form-item>
          <el-form-item label="新密码" prop="new_password">
            <el-input type="password" :prefix-icon="Lock" v-model="form.new_password"
                      placeholder="新密码" maxlength="16"/>
          </el-form-item>
          <el-form-item label="重复新密码" prop="new_password_repeat">
            <el-input type="password" :prefix-icon="Lock" v-model="form.new_password_repeat"
                      placeholder="重新输入新密码" maxlength="16"/>
          </el-form-item>
          <div style="text-align: center">
            <el-button @click="resetPassword" :icon="Switch" type="success">立即重置密码</el-button>
          </div>
        </el-form>
      </card>
    </div>
  </div>
</template>

<style scoped>
.checkbox-list {
  margin: 10px 0 0 10px;
  display: flex;
  flex-direction: column;
}
</style>
