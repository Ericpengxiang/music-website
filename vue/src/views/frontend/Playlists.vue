<template>
  <div class="pl-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">📚 我的歌单</h1>
        <p class="page-subtitle">共 {{ list.length }} 个歌单</p>
      </div>
      <el-button type="primary" @click="openCreate = true">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor" style="margin-right: 6px;">
          <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/>
        </svg>
        新建歌单
      </el-button>
    </div>

    <div v-if="!isLoggedIn" class="need-login">
      <p>登录后可管理歌单</p>
      <el-button type="primary" @click="$router.push('/user/login')">去登录</el-button>
    </div>

    <div v-else class="playlist-grid" v-loading="loading">
      <div class="playlist-card" v-for="item in list" :key="item.id" @click="$router.push(`/playlist/${item.id}`)">
        <div class="playlist-cover">
          <img v-if="item.coverUrl" :src="getImageUrl(item.coverUrl)" alt="" />
          <div v-else class="cover-placeholder">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="currentColor">
              <path d="M15 6H3v2h12V6zm0 4H3v2h12v-2zM3 16h8v-2H3v2zM17 6v8.18c-.31-.11-.65-.18-1-.18-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3V8h3V6h-5z"/>
            </svg>
          </div>
        </div>
        <div class="playlist-info">
          <div class="playlist-name">{{ item.name }}</div>
          <div class="playlist-meta">
            <span class="meta-tag" :class="item.isPublic ? 'public' : 'private'">
              {{ item.isPublic ? '公开' : '私密' }}
            </span>
            <span class="meta-count">{{ item.playCount }} 次播放</span>
          </div>
        </div>
        <button class="delete-btn" @click.stop="remove(item.id)">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
          </svg>
        </button>
      </div>
      <div v-if="list.length === 0" class="empty">暂无歌单，点击右上角"新建歌单"开始创建</div>
    </div>

    <el-dialog v-model="openCreate" title="新建歌单" width="420">
      <el-form :model="form">
        <el-form-item label="名称"><el-input v-model="form.name" placeholder="给歌单起个名字" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" placeholder="可选" /></el-form-item>
        <el-form-item label="是否公开"><el-switch v-model="form.isPublic" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="openCreate = false">取消</el-button>
        <el-button type="primary" @click="create">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '../../store/auth'
import http from '../../api/http'
import { ElMessage } from 'element-plus'

const auth = useAuthStore()
const isLoggedIn = computed(() => !!auth.token)
const loading = ref(false)
const list = ref<any[]>([])
const openCreate = ref(false)
const form = ref({ name: '', description: '', isPublic: true as boolean })

function getImageUrl(url: string | undefined): string {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  return url.startsWith('/') ? url : `/${url}`
}

async function load() {
  if (!isLoggedIn.value) return
  loading.value = true
  try {
    const { data } = await http.get('/playlists')
    list.value = data || []
  } catch (e) {
    ElMessage.error('加载歌单失败')
  } finally {
    loading.value = false
  }
}

async function create() {
  if (!form.value.name.trim()) { ElMessage.warning('请输入名称'); return }
  try {
    const { data } = await http.post('/playlists', form.value)
    list.value.unshift(data)
    openCreate.value = false
    form.value = { name: '', description: '', isPublic: true }
    ElMessage.success('创建成功')
  } catch (e) {
    ElMessage.error('创建失败')
  }
}

async function remove(id: number) {
  try {
    await http.delete(`/playlists/${id}`)
    list.value = list.value.filter(i => i.id !== id)
    ElMessage.success('已删除')
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

onMounted(load)
</script>

<style scoped>
.pl-page { max-width: 1200px; margin: 0 auto; }
.page-header { background: white; padding: 24px 32px; border-radius: 16px; margin-bottom: 24px; box-shadow: 0 2px 12px rgba(0,0,0,0.06); display: flex; align-items: center; justify-content: space-between; }
.page-title { margin: 0 0 4px 0; font-size: 28px; font-weight: 700; }
.page-subtitle { margin: 0; font-size: 14px; color: #6b7280; }
.need-login { background: white; padding: 60px; text-align: center; border-radius: 16px; }

/* 歌单网格 */
.playlist-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 24px; }
.playlist-card { background: white; border-radius: 16px; overflow: hidden; position: relative; border: 1px solid #f0f0f0; transition: all 0.3s; cursor: pointer; }
.playlist-card:hover { transform: translateY(-8px); box-shadow: 0 12px 32px rgba(0,0,0,0.12); border-color: #8b5cf6; }

.playlist-cover { position: relative; width: 100%; padding-top: 100%; background: linear-gradient(135deg, #f5f7fa 0%, #e5e7eb 100%); overflow: hidden; }
.playlist-cover img { position: absolute; top: 0; left: 0; width: 100%; height: 100%; object-fit: cover; }
.cover-placeholder { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; }

.playlist-info { padding: 16px; }
.playlist-name { font-size: 16px; font-weight: 600; color: #1f2937; margin-bottom: 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.playlist-meta { display: flex; align-items: center; gap: 8px; font-size: 13px; }
.meta-tag { padding: 2px 8px; border-radius: 12px; font-size: 12px; font-weight: 500; }
.meta-tag.public { background: #dbeafe; color: #1d4ed8; }
.meta-tag.private { background: #fef3c7; color: #92400e; }
.meta-count { color: #6b7280; }

.delete-btn { position: absolute; top: 8px; right: 8px; width: 32px; height: 32px; border-radius: 50%; border: none; background: rgba(255,255,255,0.9); color: #ef4444; cursor: pointer; display: flex; align-items: center; justify-content: center; opacity: 0; transition: all 0.3s; backdrop-filter: blur(10px); }
.playlist-card:hover .delete-btn { opacity: 1; }
.delete-btn:hover { transform: scale(1.1); background: #fef2f2; }

.empty { padding: 60px; text-align: center; color: #9ca3af; grid-column: 1 / -1; }
</style>


