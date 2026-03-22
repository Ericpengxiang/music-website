<template>
  <div class="layout">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-container">
        <div class="logo" @click="$router.push('/')">
          <div class="logo-circle">
            <svg width="32" height="32" viewBox="0 0 32 32">
              <circle cx="16" cy="16" r="15" fill="url(#gradient)" />
              <path d="M16 8 L16 24 M12 12 L20 16 L12 20 Z" stroke="white" stroke-width="2" fill="white" />
              <defs>
                <linearGradient id="gradient" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" style="stop-color:#ec4899;stop-opacity:1" />
                  <stop offset="100%" style="stop-color:#8b5cf6;stop-opacity:1" />
                </linearGradient>
              </defs>
            </svg>
          </div>
          <span class="logo-text">MusicHub</span>
        </div>

        <nav class="nav">
          <a @click="$router.push('/')" :class="{active: $route.path === '/'}">发现音乐</a>
          <a @click="$router.push('/songs')" :class="{active: $route.path === '/songs'}">歌曲库</a>
          <a @click="$router.push('/albums')" :class="{active: $route.path === '/albums'}">专辑</a>
          <a @click="$router.push('/artists')" :class="{active: $route.path === '/artists'}">歌手</a>
          <a @click="$router.push('/ranking')" :class="{active: $route.path === '/ranking'}">排行榜</a>
          <a @click="$router.push('/stories')" :class="{active: $route.path === '/stories'}">音乐故事</a>
        </nav>

        <div class="header-actions">
          <div class="search-box">
            <input 
              v-model="searchKeyword" 
              placeholder="搜索音乐" 
              @keyup.enter="search"
              class="search-input"
            />
            <button @click="search" class="search-btn">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="11" cy="11" r="8"/>
                <path d="m21 21-4.35-4.35"/>
              </svg>
            </button>
          </div>
          
          <div v-if="auth.token" class="user-menu">
            <el-dropdown @command="handleUserCommand">
              <span class="username-dropdown">
                {{ auth.username }}
                <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor" style="margin-left: 4px;">
                  <path d="M7 10l5 5 5-5z"/>
                </svg>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">👤 个人中心</el-dropdown-item>
                  <el-dropdown-item command="recommend">✨ 猜你喜欢</el-dropdown-item>
                  <el-dropdown-item command="favorites">❤️ 我的收藏</el-dropdown-item>
                  <el-dropdown-item command="playlists">📚 我的歌单</el-dropdown-item>
                  <el-dropdown-item command="history">🕒 播放历史</el-dropdown-item>
                  <el-dropdown-item divided command="logout">🚪 退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <div v-else class="auth-buttons">
            <button class="login-btn" @click="$router.push('/user/login')">登录</button>
            <button class="register-btn" @click="$router.push('/user/register')">注册</button>
          </div>
          
          <button class="theme-btn" @click="toggleTheme" :title="themeStore.isDark ? '切换到浅色模式' : '切换到深色模式'">
            <svg v-if="themeStore.isDark" width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z"/>
            </svg>
            <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
              <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/>
            </svg>
          </button>
          
          <button class="admin-btn" @click="$router.push('/admin')">管理后台</button>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="main">
      <router-view />
    </main>

    <!-- 底部播放器（重做版） -->
    <PlayerBar />
  </div>
</template>

<script setup lang="ts">
import { ref, provide } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../store/auth'
import { useThemeStore } from '../../store/theme'
import PlayerBar from '../../components/PlayerBar.vue'
import { usePlayerStore } from '../../store/player'
import { ElMessage } from 'element-plus'

const router = useRouter()
const auth = useAuthStore()
const themeStore = useThemeStore()
const searchKeyword = ref('')
const player = usePlayerStore()

function search() {
  if (searchKeyword.value.trim()) {
    router.push(`/search?q=${encodeURIComponent(searchKeyword.value)}`)
  }
}

function playSong(song: any) { player.playSong(song) }

function logout() {
  auth.logout()
  ElMessage.success('已退出登录')
  router.push('/')
}

function handleUserCommand(command: string) {
  if (command === 'logout') {
    logout()
  } else if (command === 'profile') {
    router.push('/me/profile')
  } else if (command === 'recommend') {
    router.push('/me/recommend')
  } else if (command === 'favorites') {
    router.push('/me/favorites')
  } else if (command === 'playlists') {
    router.push('/me/playlists')
  } else if (command === 'history') {
    router.push('/me/history')
  }
}

function toggleTheme() {
  themeStore.toggle()
}

// 提供给子组件使用
provide('playSong', playSong)
</script>

<style scoped>
.layout {
  min-height: 100vh;
  background: #fafafa;
  display: flex;
  flex-direction: column;
}

/* 顶部导航 */
.header {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  color: white;
  position: sticky;
  top: 0;
  z-index: 1000;
  box-shadow: 0 2px 12px rgba(0,0,0,0.15);
}

.header-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 32px;
  height: 70px;
  display: flex;
  align-items: center;
  gap: 48px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  user-select: none;
  transition: transform 0.2s;
}

.logo:hover {
  transform: scale(1.05);
}

.logo-circle {
  filter: drop-shadow(0 2px 8px rgba(236, 72, 153, 0.5));
}

.logo-text {
  font-size: 24px;
  font-weight: 700;
  background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: -0.5px;
}

.nav {
  display: flex;
  gap: 32px;
  flex: 1;
}

.nav a {
  color: rgba(255,255,255,0.7);
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  padding: 8px 4px;
}

.nav a:hover {
  color: white;
}

.nav a.active {
  color: white;
}

.nav a.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #ec4899 0%, #8b5cf6 100%);
  border-radius: 3px 3px 0 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-box {
  display: flex;
  align-items: center;
  background: rgba(255,255,255,0.1);
  border-radius: 24px;
  padding: 8px 16px;
  transition: all 0.3s;
  backdrop-filter: blur(10px);
}

.search-box:focus-within {
  background: rgba(255,255,255,0.15);
  box-shadow: 0 0 0 2px rgba(139, 92, 246, 0.3);
}

.search-input {
  border: none;
  background: none;
  color: white;
  outline: none;
  width: 180px;
  font-size: 14px;
}

.search-input::placeholder {
  color: rgba(255,255,255,0.5);
}

.search-btn {
  border: none;
  background: none;
  color: rgba(255,255,255,0.7);
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  transition: color 0.2s;
}

.search-btn:hover {
  color: white;
}

.admin-btn {
  background: rgba(255,255,255,0.1);
  border: 1px solid rgba(255,255,255,0.2);
  color: white;
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  backdrop-filter: blur(10px);
}

.admin-btn:hover {
  background: rgba(255,255,255,0.2);
  border-color: rgba(255,255,255,0.3);
}

/* 用户菜单 */
.user-menu {
  display: flex;
  align-items: center;
  gap: 12px;
}

.username-dropdown {
  color: white;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  padding: 8px 16px;
  border-radius: 20px;
  transition: all 0.3s;
  background: rgba(255,255,255,0.1);
}

.username-dropdown:hover {
  background: rgba(255,255,255,0.2);
}

/* 登录注册按钮 */
.auth-buttons {
  display: flex;
  gap: 8px;
}

.login-btn, .register-btn {
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
}

.login-btn {
  background: transparent;
  border: 1px solid rgba(255,255,255,0.3);
  color: white;
}

.login-btn:hover {
  background: rgba(255,255,255,0.1);
}

.register-btn {
  background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%);
  color: white;
}

.register-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(236, 72, 153, 0.4);
}

/* 主题切换按钮 */
.theme-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: rgba(255,255,255,0.1);
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.theme-btn:hover {
  background: rgba(255,255,255,0.2);
  transform: rotate(180deg);
}

/* 主内容区 */
.main {
  flex: 1;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  padding: 32px;
}
</style>

