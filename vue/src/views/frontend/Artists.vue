<template>
  <div>
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span style="font-weight: bold; font-size: 18px;">🎤 全部歌手</span>
          <el-input
            v-model="keyword"
            placeholder="搜索歌手"
            clearable
            style="width: 300px;"
            @input="searchArtists"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
      </template>

      <el-row :gutter="20">
        <el-col :span="4" v-for="artist in artists" :key="artist.id" style="margin-bottom: 20px;">
          <el-card shadow="hover" class="artist-card" @click="viewArtist(artist)">
            <el-image 
              :src="getImageUrl(artist.avatarUrl)" 
              style="width: 100%; aspect-ratio: 1; border-radius: 50%;"
              fit="cover"
            >
              <template #error>
                <div class="no-avatar">🎤</div>
              </template>
            </el-image>
            <div class="artist-name">{{ artist.name }}</div>
          </el-card>
        </el-col>
      </el-row>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[18, 36, 72]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; justify-content: center;"
        @size-change="loadList"
        @current-change="loadList"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import http from '../../api/http'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const router = useRouter()
const artists = ref<any[]>([])
const keyword = ref('')
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(18)
const total = ref(0)

async function loadList() {
  loading.value = true
  try {
    const { data } = await http.get('/artists/page', {
      params: { keyword: keyword.value || undefined, page: currentPage.value - 1, size: pageSize.value }
    })
    artists.value = data.content
    total.value = data.totalElements
  } catch (err: any) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function searchArtists() {
  currentPage.value = 1
  loadList()
}

function getImageUrl(url: string | undefined): string {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  return url.startsWith('/') ? url : `/${url}`
}

function viewArtist(artist: any) {
  router.push(`/artist/${artist.id}`)
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

.artist-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.3s;
  text-align: center;
  border: 1px solid #f0f0f0;
  position: relative;
  overflow: hidden;
}

.artist-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #ec4899 0%, #8b5cf6 100%);
  transform: scaleX(0);
  transition: transform 0.3s;
}

.artist-card:hover::before {
  transform: scaleX(1);
}

.artist-card:hover {
  transform: translateY(-12px);
  box-shadow: 0 16px 40px rgba(0,0,0,0.15);
  border-color: #8b5cf6;
}

:deep(.el-image) {
  position: relative;
  overflow: hidden;
  transition: transform 0.3s;
}

.artist-card:hover :deep(.el-image) {
  transform: scale(1.05);
}

.no-avatar {
  width: 100%;
  aspect-ratio: 1;
  background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 50%, #f093fb 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 56px;
  border-radius: 50%;
  color: white;
  position: relative;
  box-shadow: 0 8px 24px rgba(139, 92, 246, 0.3);
}

.no-avatar::before {
  content: '';
  position: absolute;
  inset: 12px;
  border: 2px dashed rgba(255,255,255,0.3);
  border-radius: 50%;
}

.artist-name {
  margin-top: 16px;
  font-weight: 600;
  font-size: 15px;
  color: #1f2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:deep(.el-pagination) {
  justify-content: center;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%);
}
</style>

