<template>
  <el-container style="height: 100vh;">
    <el-aside width="200px">
      <div class="logo">音乐后台</div>
      <el-menu :default-active="$route.path" router>
        <el-menu-item index="/admin">首页</el-menu-item>
        <el-menu-item index="/admin/banners">轮播图</el-menu-item>
        <el-menu-item index="/admin/genres">流派管理</el-menu-item>
        <el-menu-item index="/admin/artists">歌手管理</el-menu-item>
        <el-menu-item index="/admin/albums">专辑管理</el-menu-item>
        <el-menu-item index="/admin/songs">歌曲管理</el-menu-item>
        <el-menu-item index="/admin/lyrics">歌词管理</el-menu-item>
        <el-menu-item index="/admin/stories">音乐故事</el-menu-item>
        <el-menu-item index="/admin/users">用户管理</el-menu-item>
        <el-menu-item index="/admin/logs">操作日志</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div />
        <div class="right">
          <el-button size="small" text @click="$router.push('/')">返回前台</el-button>
          <el-button size="small" @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import { useAuthStore } from '../store/auth';
const router = useRouter();
const auth = useAuthStore();
function logout() {
  auth.logout();
  router.replace('/login');
}
</script>

<style scoped>
.logo { height: 60px; display: flex; align-items: center; justify-content: center; font-weight: bold; }
.header { display: flex; align-items: center; justify-content: space-between; }
</style>
