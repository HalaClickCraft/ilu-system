<template>
  <Teleport to="body">
    <Transition name="confirm">
      <div v-if="visible" class="fixed inset-0 bg-black/40 z-[60] flex items-center justify-center" @click.self="$emit('cancel')">
        <div class="bg-white rounded-xl p-6 max-w-sm w-full mx-4 shadow-2xl">
          <div class="flex items-start gap-3">
            <div class="w-10 h-10 rounded-full flex items-center justify-center shrink-0" :class="iconBgClass">
              <svg v-if="type === 'danger'" class="w-5 h-5 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4.5c-.77-.833-2.694-.833-3.464 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z" /></svg>
              <svg v-else class="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
            </div>
            <div>
              <h3 class="text-lg font-bold text-gray-900">{{ title }}</h3>
              <p class="text-sm text-gray-600 mt-1">{{ message }}</p>
            </div>
          </div>
          <div class="flex gap-3 justify-end mt-6">
            <button @click="$emit('cancel')" class="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg text-sm hover:bg-gray-200 transition font-medium">Annuler</button>
            <button @click="$emit('confirm')" class="px-4 py-2 rounded-lg text-sm text-white font-medium transition" :class="type === 'danger' ? 'bg-red-600 hover:bg-red-700' : 'bg-blue-600 hover:bg-blue-700'">Confirmer</button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>
<script setup>
import { computed } from 'vue'
const props = defineProps({ visible: Boolean, title: { type: String, default: 'Confirmation' }, message: { type: String, default: '' }, type: { type: String, default: 'danger' } })
const iconBgClass = computed(() => props.type === 'danger' ? 'bg-red-100' : 'bg-blue-100')
</script>
<style scoped>
.confirm-enter-active { transition: all 0.2s ease-out; }
.confirm-leave-active { transition: all 0.15s ease-in; }
.confirm-enter-from, .confirm-leave-to { opacity: 0; }
.confirm-enter-from div:last-child, .confirm-leave-to div:last-child { transform: scale(0.95); }
</style>
