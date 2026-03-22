<template>
  <transition name="lyric-fade">
    <div v-if="visible" class="fullscreen-lyric" @click.self="close">
      <div class="lyric-container">
        <!-- 关闭按钮 -->
        <button class="close-btn" @click="close">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
        </button>

        <div class="content-wrapper">
          <!-- 左侧：封面和信息 -->
          <div class="left-panel">
            <div class="album-cover">
              <img v-if="song.album?.coverUrl" :src="getImageUrl(song.album.coverUrl)" alt="" />
              <div v-else class="cover-placeholder">
                <svg width="120" height="120" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z"/>
                </svg>
              </div>
            </div>

            <div class="song-info">
              <h2 class="song-title">{{ song.title }}</h2>
              <p class="song-artist">{{ song.artist?.name || '未知歌手' }}</p>
              <p class="song-album" v-if="song.album">专辑：{{ song.album.title }}</p>
            </div>
          </div>

          <!-- 右侧：歌词 -->
          <div class="right-panel">
            <div v-if="!lyrics || lyrics.length === 0" class="no-lyric">
              <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M9 18V5l12-2v13M9 18c0 1.657-1.343 3-3 3s-3-1.343-3-3 1.343-3 3-3 3 1.343 3 3zm12-2c0 1.657-1.343 3-3 3s-3-1.343-3-3 1.343-3 3-3 3 1.343 3 3z"/>
              </svg>
              <p>暂无歌词</p>
            </div>
            <div v-else class="lyric-scroll" ref="lyricScrollRef">
              <div class="lyric-spacer"></div>
              <div
                v-for="(line, index) in lyrics"
                :key="index"
                class="lyric-line"
                :class="{ active: index === currentLineIndex }"
                :ref="el => { if (index === currentLineIndex) activeLyricRef = el as HTMLElement }"
                @click="seekTo(line.time)"
              >
                {{ line.text }}
              </div>
              <div class="lyric-spacer"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, onMounted } from 'vue'
import http from '../api/http'

interface LyricLine {
  time: number
  text: string
}

interface Props {
  visible: boolean
  song: any
  currentTime: number
}

const props = defineProps<Props>()
const emit = defineEmits(['close', 'seek'])

const lyrics = ref<LyricLine[]>([])
const currentLineIndex = ref(0)
const lyricScrollRef = ref<HTMLElement>()
const activeLyricRef = ref<HTMLElement>()

// 解析LRC格式歌词
function parseLRC(lrc: string): LyricLine[] {
  if (!lrc) return []
  
  const lines: LyricLine[] = []
  const lrcLines = lrc.split('\n')
  
  for (const line of lrcLines) {
    const match = line.match(/\[(\d{2}):(\d{2})\.(\d{2,3})\](.*)/)
    if (match) {
      const minutes = parseInt(match[1])
      const seconds = parseInt(match[2])
      const milliseconds = parseInt(match[3].padEnd(3, '0'))
      const time = minutes * 60 + seconds + milliseconds / 1000
      const text = match[4].trim()
      
      if (text) {
        lines.push({ time, text })
      }
    }
  }
  
  return lines.sort((a, b) => a.time - b.time)
}

// 加载歌词
async function loadLyric() {
  if (!props.song?.id) return
  
  try {
    const { data } = await http.get(`/lyrics/song/${props.song.id}`)
    lyrics.value = parseLRC(data?.content || '')
  } catch {
    lyrics.value = []
  }
}

// 监听歌曲变化
watch(() => props.song?.id, () => {
  if (props.visible) {
    loadLyric()
  }
}, { immediate: true })

// 监听显示状态
watch(() => props.visible, (visible) => {
  if (visible) {
    loadLyric()
  }
})

// 监听播放时间
watch(() => props.currentTime, (time) => {
  let index = 0
  for (let i = 0; i < lyrics.value.length; i++) {
    if (time >= lyrics.value[i].time) {
      index = i
    } else {
      break
    }
  }
  
  if (index !== currentLineIndex.value) {
    currentLineIndex.value = index
    scrollToActiveLine()
  }
})

// 滚动到当前歌词
function scrollToActiveLine() {
  nextTick(() => {
    if (activeLyricRef.value && lyricScrollRef.value) {
      const container = lyricScrollRef.value
      const line = activeLyricRef.value
      const containerHeight = container.clientHeight
      const lineTop = line.offsetTop
      const lineHeight = line.clientHeight
      
      container.scrollTo({
        top: lineTop - containerHeight / 2 + lineHeight / 2,
        behavior: 'smooth'
      })
    }
  })
}

function close() {
  emit('close')
}

function seekTo(time: number) {
  emit('seek', time)
}

function getImageUrl(url: string | undefined): string {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  return url.startsWith('/') ? url : `/${url}`
}
</script>

<style scoped>
.fullscreen-lyric {
  position: fixed;
  inset: 0;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f0f1e 100%);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(40px);
}

.lyric-container {
  width: 100%;
  height: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 60px 80px;
  position: relative;
}

.close-btn {
  position: absolute;
  top: 40px;
  right: 40px;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  z-index: 10;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: rotate(90deg);
}

.content-wrapper {
  display: grid;
  grid-template-columns: 400px 1fr;
  gap: 80px;
  height: 100%;
}

/* 左侧面板 */
.left-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 32px;
}

.album-cover {
  width: 320px;
  height: 320px;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
  position: relative;
  animation: float 6s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-10px) rotate(2deg);
  }
}

.album-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.6);
}

.song-info {
  text-align: center;
  color: white;
}

.song-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 12px 0;
  color: white;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.song-artist {
  font-size: 18px;
  margin: 0 0 8px 0;
  color: rgba(255, 255, 255, 0.7);
}

.song-album {
  font-size: 14px;
  margin: 0;
  color: rgba(255, 255, 255, 0.5);
}

/* 右侧面板 - 歌词 */
.right-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.no-lyric {
  text-align: center;
  color: rgba(255, 255, 255, 0.3);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.no-lyric p {
  font-size: 18px;
  margin: 0;
}

.lyric-scroll {
  width: 100%;
  height: 100%;
  overflow-y: auto;
  padding: 0 40px;
  scroll-behavior: smooth;
}

.lyric-scroll::-webkit-scrollbar {
  width: 6px;
}

.lyric-scroll::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
}

.lyric-scroll::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
}

.lyric-scroll::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.3);
}

.lyric-spacer {
  height: 40vh;
}

.lyric-line {
  padding: 16px 0;
  font-size: 28px;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.4);
  transition: all 0.5s ease;
  cursor: pointer;
  text-align: center;
  font-weight: 400;
}

.lyric-line:hover {
  color: rgba(255, 255, 255, 0.6);
}

.lyric-line.active {
  color: white;
  font-size: 36px;
  font-weight: 600;
  text-shadow: 0 2px 12px rgba(139, 92, 246, 0.6);
  transform: scale(1.05);
}

/* 进入/离开动画 */
.lyric-fade-enter-active,
.lyric-fade-leave-active {
  transition: all 0.4s ease;
}

.lyric-fade-enter-from {
  opacity: 0;
  transform: scale(0.95);
}

.lyric-fade-leave-to {
  opacity: 0;
  transform: scale(1.05);
}

/* 响应式 */
@media (max-width: 1024px) {
  .content-wrapper {
    grid-template-columns: 1fr;
    gap: 40px;
  }

  .left-panel {
    flex-direction: row;
    gap: 24px;
  }

  .album-cover {
    width: 200px;
    height: 200px;
  }

  .song-info {
    text-align: left;
  }

  .lyric-line {
    font-size: 24px;
  }

  .lyric-line.active {
    font-size: 30px;
  }
}
</style>

