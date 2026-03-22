<template>
  <div class="player">
    <div class="player-content">
      <div class="song-info">
        <el-image 
          v-if="song.album?.coverUrl" 
          :src="getImageUrl(song.album.coverUrl)" 
          style="width: 50px; height: 50px; border-radius: 4px;"
          fit="cover"
        >
          <template #error>
            <div class="no-cover">🎵</div>
          </template>
        </el-image>
        <div v-else class="no-cover">🎵</div>
        <div class="info">
          <div class="title">{{ song.title }}</div>
          <div class="artist">{{ song.artist?.name || '未知歌手' }}</div>
        </div>
      </div>

      <div class="controls">
        <el-button circle :icon="isPlaying ? 'VideoPause' : 'VideoPlay'" @click="togglePlay" />
        <div class="progress">
          <span class="time">{{ formatTime(currentTime) }}</span>
          <el-slider 
            v-model="progress" 
            :show-tooltip="false"
            @change="seek"
            style="flex: 1; margin: 0 12px;"
          />
          <span class="time">{{ formatTime(duration) }}</span>
        </div>
        <el-button circle icon="Volume" @click="toggleMute" />
        <el-slider 
          v-model="volume" 
          :show-tooltip="false"
          style="width: 100px;"
        />
      </div>

      <div class="actions">
        <el-button circle icon="Close" @click="$emit('close')" />
      </div>
    </div>

    <audio
      ref="audioRef"
      :src="getAudioUrl(song.audioUrl)"
      @timeupdate="onTimeUpdate"
      @loadedmetadata="onLoadedMetadata"
      @ended="onEnded"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'

interface Props {
  song: any
}

const props = defineProps<Props>()
const emit = defineEmits(['close'])

const audioRef = ref<HTMLAudioElement>()
const isPlaying = ref(false)
const currentTime = ref(0)
const duration = ref(0)
const volume = ref(80)
const isMuted = ref(false)

const progress = computed({
  get: () => duration.value > 0 ? (currentTime.value / duration.value) * 100 : 0,
  set: (val) => {
    if (audioRef.value && duration.value > 0) {
      audioRef.value.currentTime = (val / 100) * duration.value
    }
  }
})

watch(() => props.song, () => {
  if (audioRef.value) {
    audioRef.value.load()
    play()
  }
})

watch(volume, (val) => {
  if (audioRef.value) {
    audioRef.value.volume = val / 100
  }
})

function getImageUrl(url: string | undefined): string {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  return url.startsWith('/') ? url : `/${url}`
}

function getAudioUrl(url: string | undefined): string {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  return url.startsWith('/') ? url : `/${url}`
}

function togglePlay() {
  isPlaying.value ? pause() : play()
}

function play() {
  audioRef.value?.play()
  isPlaying.value = true
}

function pause() {
  audioRef.value?.pause()
  isPlaying.value = false
}

function toggleMute() {
  if (audioRef.value) {
    audioRef.value.muted = !audioRef.value.muted
    isMuted.value = audioRef.value.muted
  }
}

function seek(val: number) {
  if (audioRef.value && duration.value > 0) {
    audioRef.value.currentTime = (val / 100) * duration.value
  }
}

function onTimeUpdate() {
  if (audioRef.value) {
    currentTime.value = audioRef.value.currentTime
  }
}

function onLoadedMetadata() {
  if (audioRef.value) {
    duration.value = audioRef.value.duration
    audioRef.value.volume = volume.value / 100
  }
}

function onEnded() {
  isPlaying.value = false
  currentTime.value = 0
}

function formatTime(seconds: number): string {
  if (!seconds || isNaN(seconds)) return '0:00'
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins}:${secs.toString().padStart(2, '0')}`
}
</script>

<style scoped>
.player {
  width: 100%;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  color: white;
  padding: 16px 24px;
  box-shadow: 0 -4px 20px rgba(0,0,0,0.2);
  backdrop-filter: blur(20px);
}

.player-content {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 32px;
}

.song-info {
  display: flex;
  align-items: center;
  gap: 16px;
  min-width: 280px;
}

.no-cover {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.4);
}

.info {
  flex: 1;
}

.title {
  font-weight: 600;
  font-size: 15px;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.artist {
  font-size: 13px;
  opacity: 0.7;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.controls {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 20px;
}

.progress {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
}

.time {
  font-size: 13px;
  min-width: 45px;
  text-align: center;
  font-weight: 500;
  opacity: 0.8;
}

.actions {
  display: flex;
  gap: 8px;
}

:deep(.el-button) {
  color: white;
  border-color: rgba(255,255,255,0.2);
  background: rgba(255,255,255,0.08);
  transition: all 0.3s;
}

:deep(.el-button:hover) {
  background: rgba(255,255,255,0.15);
  border-color: rgba(255,255,255,0.3);
  transform: scale(1.05);
}

:deep(.el-button.is-circle) {
  width: 40px;
  height: 40px;
}

:deep(.el-slider__bar) {
  background: linear-gradient(90deg, #ec4899 0%, #8b5cf6 100%);
}

:deep(.el-slider__button) {
  border-color: white;
  background: white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.2);
}

:deep(.el-slider__runway) {
  background: rgba(255,255,255,0.2);
}
</style>

