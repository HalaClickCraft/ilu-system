<template>
  <div class="animate-pulse space-y-4">
    <div v-if="type === 'table'" class="space-y-3">
      <div v-for="i in rows" :key="i" class="flex gap-4">
        <div class="h-4 bg-gray-200 rounded w-1/4"></div>
        <div class="h-4 bg-gray-200 rounded w-1/6"></div>
        <div class="h-4 bg-gray-200 rounded w-1/5"></div>
        <div class="h-4 bg-gray-200 rounded w-1/6"></div>
        <div class="h-4 bg-gray-200 rounded w-1/5 ml-auto"></div>
      </div>
    </div>
    <div v-else-if="type === 'cards'" class="grid gap-4" :class="gridClass">
      <div v-for="i in rows" :key="i" class="bg-gray-100 rounded-xl p-5 space-y-3">
        <div class="h-5 bg-gray-200 rounded w-2/3"></div>
        <div class="h-4 bg-gray-200 rounded w-1/2"></div>
        <div class="h-4 bg-gray-200 rounded w-1/3"></div>
      </div>
    </div>
    <div v-else class="space-y-3">
      <div class="h-6 bg-gray-200 rounded w-1/3"></div>
      <div class="h-4 bg-gray-200 rounded w-2/3"></div>
      <div class="h-4 bg-gray-200 rounded w-1/2"></div>
    </div>
  </div>
</template>
<script setup>
import { computed } from 'vue'
const props = defineProps({ type: { type: String, default: 'default' }, rows: { type: Number, default: 5 }, cols: { type: Number, default: 3 } })
const gridClass = computed(() => `grid-cols-1 md:grid-cols-2 lg:grid-cols-${props.cols}`)
</script>
