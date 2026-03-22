<template>
  <div>
    <el-card>
      <template #header>
        <span style="font-weight: bold; font-size: 18px;">🎼 音乐流派</span>
      </template>

      <el-row :gutter="20">
        <el-col :span="6" v-for="genre in genres" :key="genre.id" style="margin-bottom: 20px;">
          <el-card shadow="hover" class="genre-card" @click="viewGenre(genre)">
            <div class="genre-icon">{{ getGenreIcon(genre.name) }}</div>
            <div class="genre-name">{{ genre.name }}</div>
            <div class="genre-desc">点击浏览该流派音乐</div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import http from '../../api/http'
import { ElMessage } from 'element-plus'

const router = useRouter()
const genres = ref<any[]>([])
const loading = ref(false)

async function loadList() {
  loading.value = true
  try {
    const { data } = await http.get('/genres')
    genres.value = data
  } catch (err: any) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function getGenreIcon(name: string): string {
  const icons: Record<string, string> = {
    '流行': '🎵',
    '摇滚': '🎸',
    '爵士': '🎷',
    '古典': '🎻',
    '电子': '🎹',
    '民谣': '🎤',
    '嘻哈': '🎧',
    '蓝调': '🎺'
  }
  return icons[name] || '🎼'
}

function viewGenre(genre: any) {
  router.push(`/genre/${genre.id}`)
}

onMounted(loadList)
</script>

<style scoped>
:deep(.el-card) {
  border: none;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

:deep(.el-card__header) {
  border-bottom: 1px solid #f0f0f0;
  padding: 20px 24px;
}

.genre-card {
  background: white;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.4s;
  text-align: center;
  padding: 48px 32px;
  border: 2px solid #f0f0f0;
  position: relative;
  overflow: hidden;
}

.genre-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(236, 72, 153, 0.05) 0%, rgba(139, 92, 246, 0.05) 100%);
  opacity: 0;
  transition: opacity 0.4s;
}

.genre-card:hover::before {
  opacity: 1;
}

.genre-card:hover {
  transform: translateY(-12px) scale(1.02);
  box-shadow: 0 20px 48px rgba(0,0,0,0.15);
  border-color: #8b5cf6;
}

.genre-icon {
  font-size: 80px;
  margin-bottom: 16px;
  display: inline-block;
  transition: all 0.4s;
  filter: drop-shadow(0 4px 12px rgba(0,0,0,0.1));
}

.genre-card:hover .genre-icon {
  transform: scale(1.15) rotate(5deg);
}

.genre-name {
  font-weight: 700;
  font-size: 20px;
  margin-bottom: 8px;
  color: #1f2937;
  position: relative;
  z-index: 1;
}

.genre-desc {
  font-size: 13px;
  color: #6b7280;
  position: relative;
  z-index: 1;
}
</style>

