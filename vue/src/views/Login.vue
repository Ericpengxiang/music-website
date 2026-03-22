<template>
  <div class="login">
    <el-card class="card">
      <h2>管理员登录</h2>
      <el-form :model="form" @keyup.enter="onSubmit">
        <el-form-item label="用户名">
          <el-input v-model="form.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" autocomplete="current-password" />
        </el-form-item>
        <el-button type="primary" @click="onSubmit" :loading="loading">登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../store/auth';
import http from '../api/http';

const router = useRouter();
const auth = useAuthStore();
const loading = ref(false);
const form = reactive({ username: 'admin', password: 'admin123' });

async function onSubmit() {
  if (loading.value) return;
  loading.value = true;
  try {
    const { data } = await http.post('/auth/login', form);
    auth.login(data.token, data.username, true);
    router.replace('/');
  } catch (err: any) {
    // 统一错误提示，避免未捕获导致无反馈
    const msg = err?.response?.data?.message || '登录失败，请检查用户名或密码';
    // 动态引入避免全局依赖
    const { ElMessage } = await import('element-plus');
    ElMessage.error(msg);
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login { display: flex; align-items: center; justify-content: center; height: 100vh; }
.card { width: 360px; }
</style>
