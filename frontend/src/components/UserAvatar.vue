<template>
  <div class="inline-flex items-center justify-center rounded-full text-white font-bold shrink-0" :class="sizeClass" :style="{ backgroundColor: bgColor }">
    {{ initials }}
  </div>
</template>
<script setup>
import { computed } from 'vue'
const props = defineProps({ name: { type: String, default: '' }, size: { type: String, default: 'md' } })
const initials = computed(() => {
  if (!props.name) return '?'
  const parts = props.name.trim().split(/\s+/)
  if (parts.length >= 2) return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
  return parts[0].substring(0, 2).toUpperCase()
})
const sizeClass = computed(() => ({ sm: 'w-7 h-7 text-xs', md: 'w-9 h-9 text-sm', lg: 'w-12 h-12 text-base' }[props.size] || 'w-9 h-9 text-sm'))
const colors = ['#10b981', '#3b82f6', '#8b5cf6', '#f59e0b', '#ef4444', '#06b6d4', '#ec4899', '#6366f1']
const bgColor = computed(() => {
  if (!props.name) return '#6b7280'
  let hash = 0
  for (let i = 0; i < props.name.length; i++) hash = props.name.charCodeAt(i) + ((hash << 5) - hash)
  return colors[Math.abs(hash) % colors.length]
})
</script>
