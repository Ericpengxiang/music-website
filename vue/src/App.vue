<template>
  <router-view />
  <AudioCore />
</template>

<script setup lang="ts">
import AudioCore from './components/AudioCore.vue'
</script>

<style>
/* 全局样式重置 */
html, body, #app { 
  height: 100%; 
  margin: 0; 
  transition: background-color 0.3s, color 0.3s;
}

/* 浅色模式（默认）*/
:root {
  --bg-primary: #fafafa;
  --bg-secondary: #ffffff;
  --bg-tertiary: #f5f7fa;
  --text-primary: #1f2937;
  --text-secondary: #6b7280;
  --text-tertiary: #9ca3af;
  --border-color: #e5e7eb;
  --shadow: 0 2px 12px rgba(0,0,0,0.06);
  --header-bg: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
}

/* 深色模式 */
[data-theme="dark"] {
  --bg-primary: #1a1a2e;
  --bg-secondary: #16213e;
  --bg-tertiary: #0f3460;
  --text-primary: #e5e7eb;
  --text-secondary: #9ca3af;
  --text-tertiary: #6b7280;
  --border-color: #374151;
  --shadow: 0 2px 12px rgba(0,0,0,0.3);
  --header-bg: linear-gradient(135deg, #0f0f1e 0%, #0a0e1e 100%);
}

/* 应用变量 */
body {
  background-color: var(--bg-primary);
  color: var(--text-primary);
}
</style>
