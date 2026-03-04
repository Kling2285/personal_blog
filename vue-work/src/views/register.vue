<template>
  <div class="main">
    <div class="login-box">
      <div class="login-left">
        <div class="login-title">安全管理系统</div>
        <div class="login-desc">账号密码注册</div>
        <div class="login-footer">© 2025 安全管理平台</div>
      </div>
      <div class="login-right">
        <h1 class="login-title-right" style="margin-bottom: 20px">注册</h1>

        <div class="login-type-group" style="margin-bottom: 30px;">
          <el-button
              class="login-type-btn"
              :class="{ active: activeType === 'phone' }"
              @click="activeType = 'phone'"
          >
            电话注册
          </el-button>
          <el-button
              class="login-type-btn"
              :class="{ active: activeType === 'email' }"
              @click="activeType = 'email'"
          >
            邮箱注册
          </el-button>
        </div>

        <!-- 电话注册表单（修正邮箱绑定字段） -->
        <el-form
            v-if="activeType === 'phone'"
            :model="phoneForm"
            :rules="phoneRules"
            label-width="auto"
            style="max-width: 600px"
            ref="phoneFormRef"
            class="login-form"
        >
          <el-form-item label="用户名" prop="username" style="margin-bottom: 25px;">
            <el-input v-model="phoneForm.username" autofocus placeholder="请输入用户名" class="login-input"/>
          </el-form-item>
          <el-form-item label="密码" prop="password" style="margin-bottom: 25px;">
            <el-input v-model="phoneForm.password" type="password" show-password placeholder="请输入密码" class="login-input"/>
          </el-form-item>
          <el-form-item label="昵称" prop="nickname" style="margin-bottom: 25px;">
            <el-input v-model="phoneForm.nickname" placeholder="请输入昵称" class="login-input"/>
          </el-form-item>
          <!-- 修复：邮箱绑定改为phoneForm.email -->
          <el-form-item label="邮箱" prop="email" style="margin-bottom: 25px;">
            <el-input v-model="phoneForm.email" placeholder="请输入邮箱" class="login-input"/>
          </el-form-item>
          <el-form-item label="电话" prop="phone" style="margin-bottom: 25px;">
            <el-input v-model="phoneForm.phone" placeholder="请输入电话号码" class="login-input"/>
          </el-form-item>
          <el-form-item label="验证码" prop="code" style="margin-bottom: 25px;" class="code-item">
            <el-input
                v-model="phoneForm.code"
                placeholder="请输入短信验证码"
                class="login-input code-input"
            />
            <el-button class="code-btn">获取验证码</el-button>
          </el-form-item>
          <el-form-item>
            <el-button
                type="primary"
                class="login-btn"
                @click="handlePhoneRegister"
            >
              注册
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 邮箱注册表单（修正手机号绑定字段） -->
        <el-form
            v-if="activeType === 'email'"
            :model="emailForm"
            :rules="emailRules"
            label-width="auto"
            style="max-width: 600px"
            ref="emailFormRef"
            class="login-form"
        >
          <el-form-item label="用户名" prop="username" style="margin-bottom: 25px;">
            <el-input v-model="emailForm.username" autofocus placeholder="请输入用户名" class="login-input"/>
          </el-form-item>
          <el-form-item label="密码" prop="password" style="margin-bottom: 25px;">
            <el-input v-model="emailForm.password" type="password" show-password placeholder="请输入密码" class="login-input"/>
          </el-form-item>
          <el-form-item label="昵称" prop="nickname" style="margin-bottom: 25px;">
            <el-input v-model="emailForm.nickname" placeholder="请输入昵称" class="login-input"/>
          </el-form-item>
          <!-- 修复：手机号绑定改为emailForm.phone -->
          <el-form-item label="电话" prop="phone" style="margin-bottom: 25px;">
            <el-input v-model="emailForm.phone" placeholder="请输入电话号码" class="login-input"/>
          </el-form-item>
          <el-form-item label="邮箱" prop="email" style="margin-bottom: 25px;">
            <el-input v-model="emailForm.email" placeholder="请输入邮箱" class="login-input"/>
          </el-form-item>
          <el-form-item label="验证码" prop="code" style="margin-bottom: 25px;" class="code-item">
            <el-input
                v-model="emailForm.code"
                placeholder="请输入邮箱验证码"
                class="login-input code-input"
            />
            <el-button
                class="code-btn"
                :disabled="codeBtnDisabled"
                @click="getEmailCode"
            >
              {{ codeBtnText }}
            </el-button>
          </el-form-item>
          <el-form-item>
            <el-button
                type="primary"
                class="login-btn"
                @click="handleEmailRegister"
                :loading="registerLoading"
            >
              注册
            </el-button>
          </el-form-item>
        </el-form>

        <div class="register-link">
          已有账号，<router-link to="/login">去登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onUnmounted } from "vue";
import { ElMessage } from "element-plus";
import { useRouter } from 'vue-router';
// 导入所有注册接口（含phoneRegister）
import { sendRegisterEmailCode, emailRegister, } from "@/api/register.js"

const router = useRouter();
// 切换状态
const activeType = ref('phone');

// 电话注册表单
const phoneForm = reactive({
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  code: '',
  user_type: '1',
  status: '0'
});
const phoneFormRef = ref();

// 邮箱注册表单
const emailForm = reactive({
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  code: '',
  user_type: '1',
  status: '0'
});
const emailFormRef = ref();

// 邮箱注册相关状态
const codeBtnDisabled = ref(false);
const codeBtnText = ref('获取验证码');
const registerLoading = ref(false);
let countdownTimer = null;

// 电话注册校验规则（优化空值判断）
const phoneRules = reactive({
  username: [
    { required: true, message: '请填写用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请填写密码', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请填写昵称', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请填写邮箱', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback();
          return;
        }
        if (!/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(value)) {
          callback(new Error('请输入正确的邮箱'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
  phone: [
    { required: true, message: '请填写手机号', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback();
          return;
        }
        if (!/^1[3-9]\d{9}$/.test(value)) {
          callback(new Error('请输入正确的手机号'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
  code: [
    { required: true, message: '请填写短信验证码', trigger: 'blur' }
  ]
});

// 邮箱注册校验规则（优化空值判断）
const emailRules = reactive({
  username: [
    { required: true, message: '请填写用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请填写密码', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请填写昵称', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请填写邮箱', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback();
          return;
        }
        if (!/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(value)) {
          callback(new Error('请输入正确的邮箱'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
  phone: [
    { required: true, message: '请填写手机号', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback();
          return;
        }
        if (!/^1[3-9]\d{9}$/.test(value)) {
          callback(new Error('请输入正确的手机号'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
  code: [
    { required: true, message: '请填写邮箱验证码', trigger: 'blur' }
  ]
});

// 电话注册提交
const handlePhoneRegister = () => {
  phoneFormRef.value.validate((valid) => {
    if (valid) {
      phoneRegister(phoneForm).then(res => {
        if (res.code === 500) {
          ElMessage.error("注册失败：" + res.msg);
        } else if (res.code === 200) {
          ElMessage.success("注册成功");
          router.push("/login");
        }
      }).catch(() => {
        ElMessage.error("网络异常，请重试");
      });
    }
  });
};

// 获取邮箱验证码（修复：校验失败时终止请求）
const getEmailCode = () => {
  emailFormRef.value.validateField('email', (error) => {

    sendRegisterEmailCode({ email: emailForm.email })
        .then(res => {
          if (res.code === 200) {
            ElMessage.success(res.msg);
            codeBtnDisabled.value = true;
            let countdown = 60;
            codeBtnText.value = `重新发送(${countdown}s)`;

            countdownTimer = setInterval(() => {
              countdown--;
              codeBtnText.value = `重新发送(${countdown}s)`;
              if (countdown <= 0) {
                clearInterval(countdownTimer);
                codeBtnDisabled.value = false;
                codeBtnText.value = '获取验证码';
              }
            }, 1000);
          } else {
            ElMessage.error(res.msg);
          }
        })
        .catch(() => {
          ElMessage.error("发送验证码失败，请稍后重试");
        });
  });
};

// 邮箱注册提交逻辑
const handleEmailRegister = () => {
  emailFormRef.value.validate((valid) => {
    if (!valid) return;

    registerLoading.value = true;
    emailRegister(emailForm)
        .then(res => {
          if (res.code === 500) {
            ElMessage.error("注册失败：" + res.msg);
          } else if (res.code === 200) {
            ElMessage.success("注册成功，即将跳转到登录页");
            setTimeout(() => {
              router.push("/login");
            }, 3000);
          }
        })
        .catch(() => {
          ElMessage.error("网络异常，请重试");
        })
        .finally(() => {
          registerLoading.value = false;
        });
  });
};

// 清除倒计时定时器
onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer);
});
</script>

<style scoped>
.main {
  width: 100%;
  height: 100vh;
  background: url("@/assets/img/background2.jpg") no-repeat center center;
  background-size: cover;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-box{
  width: 700px;
  height: 680px;
  background-color: white;
  border-radius: 10px;
  margin-left: 450px;
  padding: 0;
  box-shadow: 0 4px 20px rgba(0,0,0,0.12);
  display: flex;
  overflow: hidden;
}

.login-left{
  width: 40%;
  height: 100%;
  background: linear-gradient(135deg, #f34040, #f89f31);
  color: white;
  padding: 40px 20px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  box-sizing: border-box;
}
.login-title{
  font-size: 26px;
  font-weight: 700;
  margin-bottom: 10px;
}
.login-desc{
  font-size: 16px;
  opacity: 0.9;
}
.login-footer{
  font-size: 12px;
  opacity: 0.8;
}

.login-right{
  width: 60%;
  height: 100%;
  padding: 30px 40px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-title-right {
  text-align: center;
  font-size: 22px;
  font-weight: 600;
  color: #333;
}

/* 切换按钮组样式（和登录页完全一致） */
.login-type-group {
  display: flex;
  gap: 0;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

:deep(.login-type-btn) {
  flex: 1;
  height: 40px;
  border: none !important;
  border-radius: 0 !important;
  background-color: transparent !important;
  color: #333 !important;
  font-size: 14px;
  transition: all 0.2s ease;
}

:deep(.login-type-btn.active) {
  background-color: #fb0000 !important;
  color: #fff !important;
}

:deep(.login-type-btn:not(.active):hover) {
  background-color: #f8f8f8 !important;
}

.login-form {
  width: 100%;
}

.login-input {
  height: 40px;
  width: 100%;
  border-radius: 4px;
}

/* 验证码栏样式（和登录页完全一致） */
.code-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.code-input {
  flex: 1;
}

.code-btn {
  width: 120px;
  height: 40px;
  background-color: #fb0000 !important;
  border-color: #fb0000 !important;
  color: #fff !important;
  white-space: nowrap;
}

/* 注册按钮样式（和登录页完全一致） */
.login-btn {
  width: 100%;
  height: 45px;
  font-size: 18px;
  background-color: #fb0000 !important;
  border-color: #fb0000 !important;
}

.register-link {
  text-align: right;
  margin-top: 10px;
  font-size: 14px;
  color: #666;
}

.register-link a {
  color: #fb0000;
  text-decoration: none;
}

.register-link a:hover {
  text-decoration: underline;
}
</style>