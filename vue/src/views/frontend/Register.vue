<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-card">
        <div class="card-header">
          <h2>注册账号</h2>
          <p>加入 MusicHub，开启你的音乐之旅</p>
        </div>

        <form @submit.prevent="onSubmit" class="register-form">
          <div class="form-group">
            <label>用户名</label>
            <input 
              v-model="form.username" 
              type="text" 
              placeholder="请输入用户名（3-20字符）"
              required
              minlength="3"
              maxlength="20"
            />
          </div>

          <div class="form-group">
            <label>密码</label>
            <input 
              v-model="form.password" 
              type="password" 
              placeholder="请输入密码（6-30字符）"
              required
              minlength="6"
              maxlength="30"
            />
          </div>

          <div class="form-group">
            <label>确认密码</label>
            <input 
              v-model="confirmPassword" 
              type="password" 
              placeholder="再次输入密码"
              required
            />
          </div>

          <div class="form-group">
            <label>昵称（可选）</label>
            <input 
              v-model="form.nickname" 
              type="text" 
              placeholder="给自己起个好听的昵称"
            />
          </div>

          <div class="form-group">
            <label>邮箱（可选）</label>
            <input 
              v-model="form.email" 
              type="email" 
              placeholder="用于找回密码"
            />
          </div>

          <button type="submit" class="submit-btn" :disabled="loading">
            <span v-if="!loading">立即注册</span>
            <span v-else>注册中...</span>
          </button>
        </form>

        <div class="footer-links">
          <span>已有账号？</span>
          <a @click="$router.push('/user/login')">立即登录</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../store/auth'
import http from '../../api/http'
import { ElMessage } from 'element-plus'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const form = reactive({
  username: '',
  password: '',
  nickname: '',
  email: ''
})
const confirmPassword = ref('')

async function onSubmit() {
  if (form.password !== confirmPassword.value) {
    ElMessage.error('两次输入的密码不一致')
    return
  }

  loading.value = true
  try {
    const { data } = await http.post('/frontend/auth/register', form)
    auth.login(data.token, data.username)
    ElMessage.success('注册成功，欢迎加入！')
    router.push('/')
  } catch (err: any) {
    const msg = err?.response?.data?.message || '注册失败，请稍后重试'
    ElMessage.error(msg)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.register-container {
  width: 100%;
  max-width: 480px;
}

.register-card {
  background: white;
  border-radius: 24px;
  padding: 48px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}

.card-header {
  text-align: center;
  margin-bottom: 32px;
}

.card-header h2 {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 12px 0;
  background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.card-header p {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.form-group input {
  padding: 12px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  font-size: 14px;
  outline: none;
  transition: all 0.3s;
}

.form-group input:focus {
  border-color: #8b5cf6;
  box-shadow: 0 0 0 3px rgba(139, 92, 246, 0.1);
}

.submit-btn {
  margin-top: 12px;
  padding: 14px;
  background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(139, 92, 246, 0.4);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.footer-links {
  margin-top: 24px;
  text-align: center;
  font-size: 14px;
  color: #6b7280;
}

.footer-links a {
  color: #8b5cf6;
  font-weight: 600;
  cursor: pointer;
  margin-left: 8px;
  transition: color 0.2s;
}

.footer-links a:hover {
  color: #ec4899;
}
</style>



