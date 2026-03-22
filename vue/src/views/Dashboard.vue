<template>
  <div>
    <h2 style="margin-bottom: 20px;">数据概览</h2>
    
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center;">
            <div style="font-size: 14px; color: #999;">流派总数</div>
            <div style="font-size: 32px; font-weight: bold; margin-top: 10px;">{{ stats.genreCount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center;">
            <div style="font-size: 14px; color: #999;">歌手总数</div>
            <div style="font-size: 32px; font-weight: bold; margin-top: 10px;">{{ stats.artistCount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center;">
            <div style="font-size: 14px; color: #999;">专辑总数</div>
            <div style="font-size: 32px; font-weight: bold; margin-top: 10px;">{{ stats.albumCount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center;">
            <div style="font-size: 14px; color: #999;">歌曲总数</div>
            <div style="font-size: 32px; font-weight: bold; margin-top: 10px;">{{ stats.songCount }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover">
      <h3>最近添加的歌曲</h3>
      <el-table :data="recentSongs" style="width: 100%; margin-top: 16px;">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" />
        <el-table-column label="歌手">
          <template #default="{ row }">{{ row.artist?.name || '-' }}</template>
        </el-table-column>
        <el-table-column label="专辑">
          <template #default="{ row }">{{ row.album?.title || '-' }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '../api/http'
import { ElMessage } from 'element-plus'

interface Stats {
  genreCount: number
  artistCount: number
  albumCount: number
  songCount: number
}

const stats = ref<Stats>({
  genreCount: 0,
  artistCount: 0,
  albumCount: 0,
  songCount: 0
})

const recentSongs = ref<any[]>([])

async function loadStats() {
  try {
    const { data } = await http.get('/dashboard/stats')
    stats.value = {
      genreCount: data.genreCount || 0,
      artistCount: data.artistCount || 0,
      albumCount: data.albumCount || 0,
      songCount: data.songCount || 0
    }
    // 加载最近歌曲
    const songs = await http.get('/songs')
    recentSongs.value = songs.data.slice(0, 10)
  } catch (err: any) {
    ElMessage.error('加载统计数据失败')
  }
}

onMounted(loadStats)
</script>

<style scoped>
</style>


