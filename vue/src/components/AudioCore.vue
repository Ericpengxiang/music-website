<template>
  <audio
    ref="audioRef"
    :src="src"
    @timeupdate="onTimeUpdate"
    @loadedmetadata="onLoadedMetadata"
    @ended="onEnded"
  />
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { usePlayerStore } from '../store/player'

const player = usePlayerStore()
const audioRef = ref<HTMLAudioElement>()

const src = computed(() => player.currentSong ? player.normalizeUrl(player.currentSong.audioUrl) : '')

onMounted(() => {
  if (audioRef.value) {
    player.attachAudio(audioRef.value)
    player.loadSavedVolume()
  }
})

function onTimeUpdate() {
  if (!audioRef.value) return
  player.currentTime = audioRef.value.currentTime
}

function onLoadedMetadata() {
  if (!audioRef.value) return
  player.duration = audioRef.value.duration
}

function onEnded() {
  if (player.playMode === 2) {
    if (audioRef.value) {
      audioRef.value.currentTime = 0
      audioRef.value.play()
      player.isPlaying = true
    }
  } else {
    player.playNext()
  }
}
</script>

<style scoped>
audio { display: none; }
</style>


