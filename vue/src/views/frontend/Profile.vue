<template>
  <div class="profile-page">
    <!-- 用户卡片 -->
    <div class="user-card" v-loading="loading">
      <div class="user-banner">
        <div class="banner-bg"></div>
      </div>
      <div class="user-main">
        <div class="user-avatar-section">
          <div class="avatar-wrapper">
            <img v-if="form.avatarUrl" :src="getImageUrl(form.avatarUrl)" class="user-avatar" />
            <div v-else class="user-avatar-placeholder">{{ form.nickname?.charAt(0) || form.username?.charAt(0) }}</div>
            <button class="avatar-edit-btn" @click="uploadDialogVisible = true">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/>
              </svg>
            </button>
          </div>
          <div class="user-info-text">
            <h1 class="user-name">{{ form.nickname || form.username }}</h1>
            <p class="user-username">@{{ form.username }}</p>
          </div>
        </div>

        <!-- 统计卡片 -->
        <div class="stats-row">
          <div class="stat-item" @click="$router.push('/me/favorites')">
            <div class="stat-value">{{ stats.favoriteCount }}</div>
            <div class="stat-label">收藏</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item" @click="$router.push('/me/playlists')">
            <div class="stat-value">{{ playlists.length }}</div>
            <div class="stat-label">歌单</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item" @click="$router.push('/me/history')">
            <div class="stat-value">{{ stats.playCount }}</div>
            <div class="stat-label">播放</div>
          </div>
        </div>
      </div>
    </div>

    <div class="content" v-loading="loading">

      <!-- 个人资料 -->
      <div class="section-card">
        <div class="section-header">
          <h2 class="section-title">个人资料</h2>
          <el-button type="primary" @click="updateProfile" :loading="updating">保存修改</el-button>
        </div>
        <el-form :model="form" label-width="100px" class="profile-form">
          <el-form-item label="用户名">
            <el-input v-model="form.username" disabled />
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="form.nickname" placeholder="请输入昵称" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="form.email" type="email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item label="个人简介">
            <el-input v-model="form.bio" type="textarea" :rows="4" placeholder="介绍一下自己吧~" maxlength="200" show-word-limit />
          </el-form-item>
        </el-form>
      </div>

      <!-- 修改密码 -->
      <div class="section-card">
        <h2 class="section-title">修改密码</h2>
        <el-form :model="passwordForm" label-width="100px">
          <el-form-item label="原密码">
            <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" />
          </el-form-item>
          <el-form-item label="确认密码">
            <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="再次输入新密码" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="updatePassword">修改密码</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <!-- 头像上传对话框 -->
    <el-dialog v-model="uploadDialogVisible" title="上传头像" width="420px">
      <el-upload
        class="avatar-uploader"
        action="/api/upload"
        :headers="{ Authorization: `Bearer ${auth.token}` }"
        :show-file-list="false"
        :on-success="handleUploadSuccess"
        :before-upload="beforeUpload"
      >
        <img v-if="uploadedUrl" :src="getImageUrl(uploadedUrl)" class="uploaded-avatar" />
        <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
      </el-upload>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAvatar" :disabled="!uploadedUrl">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import http from '../../api/http'
import { useAuthStore } from '../../store/auth'

const auth = useAuthStore()

const loading = ref(false)
const updating = ref(false)
const uploadDialogVisible = ref(false)
const uploadedUrl = ref('')
const playlists = ref<any[]>([])

const form = reactive({
  username: '',
  nickname: '',
  email: '',
  bio: '',
  avatarUrl: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const stats = reactive({
  favoriteCount: 0,
  playCount: 0
})

async function loadProfile() {
  loading.value = true
  try {
    const [profileRes, statsRes, playlistsRes] = await Promise.all([
      http.get('/frontend/auth/me'),
      http.get('/frontend/auth/stats'),
      http.get('/playlists').catch(() => ({ data: [] }))
    ])
    
    Object.assign(form, profileRes.data)
    Object.assign(stats, statsRes.data)
    playlists.value = playlistsRes.data || []
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

async function updateProfile() {
  updating.value = true
  try {
    const { data } = await http.put('/frontend/auth/profile', {
      nickname: form.nickname,
      email: form.email,
      bio: form.bio,
      avatarUrl: form.avatarUrl
    })
    Object.assign(form, data)
    ElMessage.success('保存成功')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    updating.value = false
  }
}

async function updatePassword() {
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    ElMessage.warning('请填写完整')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次密码不一致')
    return
  }
  if (passwordForm.newPassword.length < 6) {
    ElMessage.warning('新密码至少6位')
    return
  }

  try {
    await http.put('/frontend/auth/password', {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '修改失败')
  }
}

function handleUploadSuccess(response: any) {
  uploadedUrl.value = response.url
  ElMessage.success('上传成功')
}

function beforeUpload(file: File) {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

async function confirmAvatar() {
  form.avatarUrl = uploadedUrl.value
  uploadDialogVisible.value = false
  
  // 立即保存到数据库
  try {
    await http.put('/frontend/auth/profile', {
      avatarUrl: uploadedUrl.value
    })
    uploadedUrl.value = ''
    ElMessage.success('头像更换成功')
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

function getImageUrl(url: string | undefined): string {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  return url.startsWith('/') ? url : `/${url}`
}

onMounted(loadProfile)
</script>

<style scoped>
.profile-page { max-width: 1000px; margin: 0 auto; }

/* 用户卡片 */
.user-card { background: white; border-radius: 24px; overflow: hidden; margin-bottom: 24px; box-shadow: 0 4px 20px rgba(0,0,0,0.08); }

.user-banner { height: 180px; position: relative; }
.banner-bg { width: 100%; height: 100%; background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%); }

.user-main { padding: 0 48px 32px; margin-top: -60px; }

.user-avatar-section { display: flex; align-items: flex-end; gap: 24px; margin-bottom: 32px; }

.avatar-wrapper { position: relative; }
.user-avatar, .user-avatar-placeholder { width: 120px; height: 120px; border-radius: 50%; border: 4px solid white; box-shadow: 0 8px 24px rgba(0,0,0,0.15); }
.user-avatar { object-fit: cover; }
.user-avatar-placeholder { background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%); color: white; display: flex; align-items: center; justify-content: center; font-size: 48px; font-weight: 700; }

.avatar-edit-btn { position: absolute; bottom: 4px; right: 4px; width: 36px; height: 36px; border-radius: 50%; border: 2px solid white; background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%); color: white; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: all 0.3s; box-shadow: 0 4px 12px rgba(139, 92, 246, 0.4); }
.avatar-edit-btn:hover { transform: scale(1.1); }

.user-info-text { flex: 1; padding-bottom: 12px; }
.user-name { margin: 0 0 4px 0; font-size: 32px; font-weight: 700; color: #1f2937; }
.user-username { margin: 0; font-size: 16px; color: #6b7280; }

/* 统计行 */
.stats-row { display: flex; align-items: center; justify-content: center; gap: 48px; padding: 24px; background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%); border-radius: 16px; }
.stat-item { text-align: center; cursor: pointer; transition: transform 0.2s; }
.stat-item:hover { transform: translateY(-4px); }
.stat-value { font-size: 28px; font-weight: 700; background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; margin-bottom: 4px; }
.stat-label { font-size: 14px; color: #6b7280; font-weight: 500; }
.stat-divider { width: 1px; height: 40px; background: #e5e7eb; }

/* 内容区 */
.content { display: flex; flex-direction: column; gap: 24px; }

/* 表单卡片 */
.section-card { background: white; padding: 32px; border-radius: 16px; box-shadow: 0 2px 12px rgba(0,0,0,0.06); }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.section-title { margin: 0; font-size: 20px; font-weight: 700; color: #1f2937; }

.profile-form :deep(.el-input__wrapper) { border-radius: 12px; }
.profile-form :deep(.el-textarea__inner) { border-radius: 12px; }

/* 头像上传弹窗 */
.avatar-uploader { width: 100%; }
.uploaded-avatar { width: 200px; height: 200px; border-radius: 50%; object-fit: cover; display: block; margin: 0 auto; }
.avatar-uploader-icon { font-size: 48px; color: #8c939d; width: 200px; height: 200px; border: 2px dashed #d9d9d9; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto; cursor: pointer; transition: all 0.3s; }
.avatar-uploader-icon:hover { border-color: #8b5cf6; color: #8b5cf6; }
</style>


