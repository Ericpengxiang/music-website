<template>
  <div>
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span style="font-weight: bold; font-size: 18px;">💿 全部专辑</span>
          <el-input
            v-model="keyword"
            placeholder="搜索专辑"
            clearable
            style="width: 300px;"
            @input="searchAlbums"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
      </template>

      <el-row :gutter="20">
        <el-col :span="6" v-for="album in albums" :key="album.id" style="margin-bottom: 20px;">
          <el-card shadow="hover" class="album-card" @click="viewAlbum(album)">
            <el-image 
              :src="getImageUrl(album.coverUrl)" 
              style="width: 100%; aspect-ratio: 1; border-radius: 8px;"
              fit="cover"
            >
              <template #error>
                <div class="no-cover">💿</div>
              </template>
            </el-image>
            <div class="album-title">{{ album.title }}</div>
            <div class="album-artist">{{ album.artist?.name || '未知歌手' }}</div>
            <div class="album-date">{{ album.releaseDate || '-' }}</div>
          </el-card>
        </el-col>
      </el-row>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[12, 24, 48]"
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
const albums = ref<any[]>([])
const keyword = ref('')
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

async function loadList() {
  loading.value = true
  try {
    const { data } = await http.get('/albums/page', {
      params: { keyword: keyword.value || undefined, page: currentPage.value - 1, size: pageSize.value }
    })
    albums.value = data.content
    total.value = data.totalElements
  } catch (err: any) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function searchAlbums() {
  currentPage.value = 1
  loadList()
}

function getImageUrl(url: string | undefined): string {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  return url.startsWith('/') ? url : `/${url}`
}

function viewAlbum(album: any) {
  router.push(`/album/${album.id}`)
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

:deep(.el-input__wrapper) {
  border-radius: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.album-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #f0f0f0;
}

.album-card:hover {
  transform: translateY(-12px);
  box-shadow: 0 16px 40px rgba(0,0,0,0.15);
  border-color: #8b5cf6;
}

:deep(.el-image) {
  position: relative;
  overflow: hidden;
}

:deep(.el-image img) {
  transition: transform 0.4s;
}

.album-card:hover :deep(.el-image img) {
  transform: scale(1.08);
}

.no-cover {
  width: 100%;
  aspect-ratio: 1;
  background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 64px;
  border-radius: 8px;
  color: white;
  position: relative;
}

.no-cover::before {
  content: '';
  position: absolute;
  inset: 0;
  background: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 200 200"><circle cx="100" cy="100" r="60" fill="rgba(255,255,255,0.1)"/></svg>') center/cover;
}

.album-title {
  margin-top: 12px;
  font-weight: 600;
  font-size: 15px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #1f2937;
}

.album-artist {
  margin-top: 6px;
  font-size: 13px;
  color: #6b7280;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.album-date {
  margin-top: 4px;
  font-size: 12px;
  color: #9ca3af;
}

:deep(.el-pagination) {
  justify-content: center;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%);
}
</style>

