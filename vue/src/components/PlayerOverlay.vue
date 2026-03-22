<template>
  <Teleport to="body" v-if="visible">
    <div class="player-overlay" @click.self="$emit('close')">
      <div class="overlay-container">
        <!-- 顶部标题条 -->
        <div class="overlay-header">
          <div class="overlay-title">
            <div class="title-main">{{ song?.title }}</div>
            <div class="title-sub">{{ song?.artist?.name || '未知歌手' }}<span v-if="song?.album"> · {{ song?.album?.title }}</span></div>
          </div>
          <button class="overlay-close" @click="$emit('close')" aria-label="关闭">✖</button>
        </div>

        <div class="expanded-content">
          <!-- 左侧：唱片机 -->
          <div class="expanded-left">
            <div class="turntable">
              <div class="disc" :class="{ spinning: isPlaying }">
                <div class="disc-ring outer"></div>
                <div class="disc-ring middle"></div>
                <div class="disc-ring inner"></div>
                <div class="disc-center">
                  <el-image 
                    v-if="song?.album?.coverUrl" 
                    :src="getImageUrl(song.album.coverUrl)" 
                    fit="cover"
                  >
                    <template #error>
                      <div class="cover-fallback">🎵</div>
                    </template>
                  </el-image>
                </div>
              </div>
              <!-- 唱臂 -->
              <svg class="tonearm" :class="{ play: isPlaying }" viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg">
                <g fill="none" stroke="#e5e7eb" stroke-width="6" stroke-linecap="round">
                  <path d="M40 40 L120 120" />
                  <circle cx="40" cy="40" r="12" fill="#f3f4f6" />
                  <path d="M120 120 L160 128" />
                  <circle cx="165" cy="130" r="10" fill="#9ca3af" />
                </g>
              </svg>
            </div>
          </div>
          
          <!-- 右侧：歌曲信息 + 歌词 -->
          <div class="expanded-right">
            <div class="song-details">
              <h2 class="song-title-large">{{ song?.title }}</h2>
              <p class="song-artist-large">{{ song?.artist?.name || '未知歌手' }}</p>
              <p class="song-album-large" v-if="song?.album">专辑：{{ song?.album?.title }}</p>
            </div>

            <div class="lyrics-preview overlay-lyrics" v-if="song?.lyric?.lrc">
              <LyricDisplay :lrc="song.lyric.lrc" :current-time="currentTime" @seek="$emit('seek', $event)" />
            </div>
            <div v-else class="no-lyrics"><p>暂无歌词</p></div>
          </div>
        </div>
      </div>

      <!-- 底部吸底控制区（仿网易云） -->
      <div v-if="song" class="overlay-footer">
        <!-- 进度条 -->
        <div class="progress-row">
          <span class="time">{{ formatTime(player.currentTime) }}</span>
          <el-slider v-model="progressLocal" :show-tooltip="false" @change="player.seek" class="overlay-progress" />
          <span class="time">{{ formatTime(player.duration) }}</span>
        </div>
        <!-- 控制按钮 -->
        <div class="overlay-controls">
          <div class="left">
            <el-button circle size="small" :class="{ liked: isFav }" @click="player.toggleFavorite" :title="isFav ? '取消喜欢' : '喜欢'">❤</el-button>
          </div>
          <div class="center">
            <el-button circle @click="player.playPrevious" :disabled="!player.hasPrevious">⏮</el-button>
            <el-button circle size="large" @click="player.togglePlay">{{ isPlaying ? '⏸' : '▶️' }}</el-button>
            <el-button circle @click="player.playNext" :disabled="!player.hasNext">⏭</el-button>
          </div>
          <div class="right">
            <el-button circle size="small" @click="cycleMode" :title="modeText">{{ modeIcon }}</el-button>
            <el-button circle size="small" @click="player.toggleMute">{{ player.isMuted ? '🔇' : '🔊' }}</el-button>
            <el-slider v-model="volumeLocal" :show-tooltip="false" class="vol-slider" />
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, watch } from 'vue'
import { usePlayerStore } from '../store/player'
import LyricDisplay from './LyricDisplay.vue'

interface Props { visible: boolean; song: any; currentTime: number }
const props = defineProps<Props>()
const player = usePlayerStore()

const song = computed(() => props.song)
const currentTime = computed(() => player.currentTime)
const isPlaying = computed(() => player.isPlaying)
const progressLocal = computed({ get: () => player.progress, set: (v: number) => player.seek(v) })
const volumeLocal = computed({ get: () => Math.round(player.volume * 100), set: (v: number) => player.setVolume(v / 100) })
const modeText = computed(() => ['顺序播放', '随机播放', '单曲循环'][player.playMode])
const modeIcon = computed(() => player.playMode === 0 ? '🔁' : player.playMode === 1 ? '🔀' : '🔂')
function cycleMode() { player.playMode = ((player.playMode + 1) % 3) as any }
const isFav = computed(() => player.isFavorite(song.value?.id))

watch(() => props.visible, (v) => {
  const body = document.body
  if (v) body.classList.add('no-scroll')
  else body.classList.remove('no-scroll')
})

onUnmounted(() => document.body.classList.remove('no-scroll'))

function getImageUrl(url: string | undefined): string {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  return url.startsWith('/') ? url : `/${url}`
}
function onKeydown(e: KeyboardEvent) {
  if (e.code === 'Escape' && props.visible) {
    e.preventDefault()
    ;(emit as any)('close')
  }
}

const emit = defineEmits(['close', 'seek'])
onMounted(() => window.addEventListener('keydown', onKeydown))
onUnmounted(() => window.removeEventListener('keydown', onKeydown))

function formatTime(sec: number) {
  if (!sec || isNaN(sec)) return '0:00'
  const m = Math.floor(sec / 60)
  const s = Math.floor(sec % 60)
  return `${m}:${s.toString().padStart(2, '0')}`
}

// 跟随站点主题颜色，不再使用封面取色
</script>

<style scoped>
.player-overlay {
  position: fixed;
  inset: 0;
  background: var(--header-bg);
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.overlay-container {
  width: min(1200px, 92vw);
  max-height: calc(100vh - 160px);
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding-bottom: 0;
}

.overlay-header {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding-top: 8px;
}

.overlay-title { text-align: center; }
.title-main { font-size: 32px; font-weight: 800; letter-spacing: .5px; }
.title-sub { margin-top: 6px; opacity: .85; font-weight: 500; }
.overlay-close { position: absolute; right: 0; top: 0; width: 36px; height: 36px; border-radius: 50%; border: 1px solid rgba(255,255,255,.25); background: rgba(255,255,255,.08); color: #fff; cursor: pointer; }
.overlay-close:hover { background: rgba(255,255,255,.18); }

.expanded-content {
  display: grid;
  grid-template-columns: 480px 1fr;
  gap: 48px;
  height: 100%;
}

.expanded-left { display: flex; align-items: center; justify-content: center; position: relative; }

/* 唱片机 */
.turntable { position: relative; width: 420px; height: 420px; }
.disc { position: absolute; inset: 0; border-radius: 50%; background: radial-gradient(circle at 50% 50%, #111 0%, #0b0b0b 40%, #000 60%); box-shadow: 0 24px 80px rgba(0,0,0,0.6); }
.disc::after { content: ''; position: absolute; inset: 0; border-radius: 50%; mix-blend-mode: overlay; opacity: .35; background:
  repeating-radial-gradient(circle at 50% 50%, rgba(255,255,255,0.05) 0px, rgba(255,255,255,0.05) 1px, rgba(0,0,0,0.06) 2px, rgba(0,0,0,0.06) 3px);
}
.disc.spinning { animation: spin 8s linear infinite; }
.disc-ring { position: absolute; border-radius: 50%; border: 2px solid rgba(255,255,255,0.08); }
.disc-ring.outer { inset: 10px; }
.disc-ring.middle { inset: 60px; }
.disc-ring.inner { inset: 110px; }
.disc-center { position: absolute; inset: 150px; border-radius: 50%; overflow: hidden; }
.disc-center .cover-fallback { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #ec4899, #8b5cf6); font-size: 40px; }

@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }

.tonearm { position: absolute; width: 260px; height: 260px; right: -10px; top: -10px; transform-origin: 45px 45px; transform: rotate(-30deg); transition: transform .4s ease; opacity: .9; }
.tonearm.play { transform: rotate(5deg); }

.expanded-right { display: flex; flex-direction: column; gap: 32px; overflow: hidden; }

.song-title-large {
  font-size: 36px; font-weight: 700; margin: 0 0 16px 0;
  background: linear-gradient(135deg, #ffffff 0%, #e0e0e0 100%);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent; line-height: 1.2;
}
.song-artist-large { font-size: 20px; margin: 0 0 12px 0; opacity: 0.9; font-weight: 500; }
.song-album-large { font-size: 16px; margin: 0; opacity: 0.7; }

.lyrics-preview {
  flex: 1; overflow-y: auto; padding: 24px; background: rgba(255,255,255,0.05); border-radius: 12px;
}
.overlay-lyrics { max-height: calc(100vh - 480px); }
.overlay-lyrics:deep(.lyric-container) { padding: 32px 12px; }
.overlay-lyrics:deep(.lyric-line) { font-size: 18px; color: rgba(255,255,255,.75); }
.overlay-lyrics:deep(.lyric-line.active) { font-size: 22px; color: #fff; font-weight: 700; text-shadow: 0 2px 20px rgba(0,0,0,.3); }
.overlay-lyrics:deep(.lyric-line:hover) { color: rgba(255,255,255,.95); }
.overlay-lyrics:deep(.lyric-container::-webkit-scrollbar-thumb) { background: rgba(255,255,255,.25); }
.no-lyrics { flex: 1; display: flex; align-items: center; justify-content: center; padding: 24px; background: rgba(255,255,255,0.05); border-radius: 12px; }
.no-lyrics p { font-size: 18px; opacity: 0.5; margin: 0; }

/* 底部吸底控制区（仿网易云） */
.overlay-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(40px) saturate(150%);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding: 12px 24px 16px;
  z-index: 2001;
}

.progress-row {
  display: grid;
  grid-template-columns: 56px 1fr 56px;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.overlay-controls {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  gap: 16px;
}

.overlay-controls .left,
.overlay-controls .right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.overlay-controls .center {
  display: flex;
  align-items: center;
  gap: 12px;
  justify-content: center;
}

.overlay-progress { height: 6px; }
.time { font-size: 12px; opacity: .85; text-align: center; }
.vol-slider { width: 120px; }
.liked { color: #ef4444; }

/* 主题色联动：进度条/按钮使用站点主渐变 */
:deep(.overlay-progress .el-slider__bar) { background: var(--gradient-primary); }
:deep(.overlay-progress .el-slider__button) { border-color: transparent; background: #fff; }
:deep(.overlay-progress .el-slider__runway) { background: rgba(255,255,255,.22); height: 6px; border-radius: 999px; }
/* 迷你元素 hover 更亮一些 */
.overlay-header .overlay-title { color: rgba(255,255,255,.95); }

@media (max-width: 1024px) {
  .expanded-content { grid-template-columns: 300px 1fr; gap: 32px; }
  .turntable { width: 320px; height: 320px; }
  .song-title-large { font-size: 28px; }
  .overlay-footer { padding: 10px 16px 12px; }
  .progress-row { grid-template-columns: 48px 1fr 48px; gap: 8px; }
}

@media (max-width: 768px) {
  .expanded-content { grid-template-columns: 1fr; gap: 24px; }
  .turntable { width: 280px; height: 280px; }
  .overlay-controls { grid-template-columns: 1fr; gap: 12px; }
  .overlay-controls .left,
  .overlay-controls .right { justify-content: center; }
  .vol-slider { width: 100px; }
}
</style>


