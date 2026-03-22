<template>
  <footer class="player-bar" v-if="currentSong">
    <div class="player-content">
      <div class="song-info" @click="toggleExpand">
        <el-image v-if="currentSong.album?.coverUrl" :src="imageUrl" fit="cover" class="cover" />
        <div v-else class="cover no-cover">🎵</div>
        <div class="meta">
          <div class="title">{{ currentSong.title }}</div>
          <div class="artist">{{ currentSong.artist?.name || '未知歌手' }}</div>
        </div>
      </div>

      <div class="controls">
        <div class="buttons">
          <el-button circle size="small" @click="cycleMode" :title="modeText">
            <span>{{ modeIcon }}</span>
          </el-button>
          <el-button circle @click="player.playPrevious" :disabled="!player.hasPrevious">⏮</el-button>
          <el-button circle size="large" @click="player.togglePlay">{{ player.isPlaying ? '⏸' : '▶️' }}</el-button>
          <el-button circle @click="player.playNext" :disabled="!player.hasNext">⏭</el-button>
          <el-button circle size="small" @click="toggleQueue" :class="{ active: showQueue }">🧾</el-button>
        </div>
        <div class="progress">
          <span class="time">{{ formatTime(player.currentTime) }}</span>
          <el-slider v-model="progressLocal" :show-tooltip="false" @change="player.seek" class="progress-slider" />
          <span class="time">{{ formatTime(player.duration) }}</span>
        </div>
      </div>

      <div class="volume">
        <el-button circle size="small" @click="player.toggleMute">{{ player.isMuted ? '🔇' : '🔊' }}</el-button>
        <el-slider v-model="volumeLocal" :show-tooltip="false" class="volume-slider" />
        <el-button circle size="small" :class="{ liked: isFav }" @click="player.toggleFavorite" :title="isFav ? '取消喜欢' : '喜欢'">❤</el-button>
        <el-button circle size="small" @click="toggleExpand" :title="'放大播放器'">⛶</el-button>
      </div>
    </div>

    <transition name="slide">
      <div v-if="showQueue" class="queue">
        <div class="queue-header">
          <h3>播放队列 ({{ player.queue.length }})</h3>
          <el-button text @click="showQueue = false">关闭</el-button>
        </div>
        <div class="queue-list">
          <div v-for="(it, idx) in player.queue" :key="it.id" class="queue-item" :class="{ active: idx === player.currentIndex }" @click="player.currentIndex = idx; player.loadAndPlay()">
            <span class="queue-index">{{ idx + 1 }}</span>
            <span class="queue-title">{{ it.title }}</span>
            <span class="queue-artist">{{ it.artist?.name }}</span>
          </div>
        </div>
      </div>
    </transition>
  </footer>
  <PlayerOverlay 
    :visible="player.isExpanded"
    :song="currentSong || undefined"
    :current-time="player.currentTime"
    @close="player.setExpanded(false)"
    @seek="player.seekTo"
  />
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { usePlayerStore } from '../store/player'
import PlayerOverlay from './PlayerOverlay.vue'

const player = usePlayerStore()
const currentSong = computed(() => player.currentSong)
const imageUrl = computed(() => currentSong.value?.album?.coverUrl ? normalizeImage(currentSong.value.album.coverUrl) : '')
const showQueue = ref(false)
const isFav = computed(() => player.isFavorite(currentSong.value?.id))

const progressLocal = computed({
  get: () => player.progress,
  set: (v: number) => player.seek(v)
})

const volumeLocal = computed({
  get: () => Math.round(player.volume * 100),
  set: (v: number) => player.setVolume(v / 100)
})

const modeText = computed(() => ['顺序播放', '随机播放', '单曲循环'][player.playMode])
const modeIcon = computed(() => player.playMode === 0 ? '🔁' : player.playMode === 1 ? '🔀' : '🔂')

function cycleMode() { player.playMode = ((player.playMode + 1) % 3) as any }
function toggleQueue() { showQueue.value = !showQueue.value }
function toggleExpand() { player.setExpanded(true) }

function normalizeImage(url: string): string {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  return url.startsWith('/') ? url : `/${url}`
}

function formatTime(sec: number) {
  if (!sec || isNaN(sec)) return '0:00'
  const m = Math.floor(sec / 60)
  const s = Math.floor(sec % 60)
  return `${m}:${s.toString().padStart(2, '0')}`
}
</script>

<style scoped>
.player-bar { position: fixed; left: 0; right: 0; bottom: 0; z-index: 999; background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%); color: #fff; }
.player-content { max-width: 1400px; margin: 0 auto; padding: 12px 24px; display: flex; align-items: center; gap: 24px; }
.song-info { display: flex; align-items: center; gap: 12px; min-width: 240px; cursor: pointer; }
.cover { width: 48px; height: 48px; border-radius: 8px; overflow: hidden; }
.no-cover { display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%); font-size: 24px; }
.meta { display: flex; flex-direction: column; }
.title { font-weight: 600; font-size: 14px; }
.artist { font-size: 12px; opacity: 0.7; }
.controls { flex: 1; display: flex; flex-direction: column; gap: 8px; }
.buttons { display: flex; align-items: center; justify-content: center; gap: 8px; }
.progress { display: flex; align-items: center; gap: 8px; }
.progress-slider { flex: 1; }
.time { font-size: 12px; min-width: 44px; text-align: center; }
.volume { display: flex; align-items: center; gap: 8px; min-width: 160px; }
.volume-slider { width: 100px; }
.liked { color: #ef4444; }

.queue { max-width: 1400px; margin: 0 auto; padding: 0 24px 12px; }
.queue-header { display: flex; align-items: center; justify-content: space-between; padding: 8px 0; }
.queue-list { max-height: 220px; overflow-y: auto; }
.queue-item { display: grid; grid-template-columns: 40px 1fr 160px; gap: 8px; padding: 8px 12px; border-radius: 6px; cursor: pointer; }
.queue-item.active, .queue-item:hover { background: rgba(255,255,255,0.12); }
.queue-index { opacity: 0.6; text-align: center; }
.queue-title { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.queue-artist { opacity: 0.8; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.slide-enter-active, .slide-leave-active { transition: all .25s ease; }
.slide-enter-from, .slide-leave-to { max-height: 0; opacity: 0; }
</style>


