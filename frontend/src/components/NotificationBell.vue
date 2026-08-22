<template>
  <div class="relative" ref="bellRef">
    <button @click="isOpen = !isOpen" class="relative p-2 rounded-lg hover:bg-slate-700 transition text-gray-300 hover:text-white">
      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"/>
      </svg>
      <span v-if="unreadCount > 0" class="absolute -top-0.5 -right-0.5 bg-red-500 text-white text-[10px] font-bold rounded-full w-4 h-4 flex items-center justify-center">
        {{ unreadCount > 9 ? '9+' : unreadCount }}
      </span>
    </button>

    <!-- Dropdown -->
    <div v-if="isOpen" class="absolute right-0 top-full mt-2 w-80 bg-white rounded-xl shadow-xl border border-gray-200 z-50 max-h-96 flex flex-col">
      <div class="px-4 py-3 border-b border-gray-100 flex items-center justify-between">
        <h3 class="text-sm font-semibold text-gray-900">Notifications</h3>
        <button v-if="notifications.length > 0" @click="markAllRead" class="text-xs text-emerald-600 hover:text-emerald-800 font-medium">Tout marquer lu</button>
      </div>
      <div class="overflow-y-auto flex-1">
        <div v-for="n in notifications" :key="n.id" @click="handleClick(n)" :class="n.read ? 'bg-white' : 'bg-emerald-50'" class="px-4 py-3 border-b border-gray-50 cursor-pointer hover:bg-gray-50 transition">
          <div class="flex items-start gap-3">
            <div :class="iconBg(n.type)" class="w-8 h-8 rounded-full flex items-center justify-center shrink-0">
              <svg v-if="isRecyclageNotif(n.type)" class="w-4 h-4 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/></svg>
              <svg v-else-if="n.type === 'ABSENCE_REPRISE'" class="w-4 h-4 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/></svg>
              <svg v-else class="w-4 h-4 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4.5c-.77-.833-2.694-.833-3.464 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z"/></svg>
            </div>
            <div class="flex-1 min-w-0">
              <p class="text-sm text-gray-800" :class="n.read ? 'font-normal' : 'font-medium'">{{ n.message }}</p>
              <p class="text-xs text-gray-400 mt-1">{{ timeAgo(n.createdAt) }}</p>
            </div>
            <span v-if="!n.read" class="w-2 h-2 bg-emerald-500 rounded-full shrink-0 mt-1.5"></span>
          </div>
        </div>
        <div v-if="notifications.length === 0" class="px-4 py-8 text-center text-sm text-gray-400">
          Aucune notification
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { notificationApi } from '@/services/notificationApi'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const isOpen = ref(false)
const notifications = ref([])
const unreadCount = ref(0)
const bellRef = ref(null)
let refreshInterval = null

function isRecyclageNotif(type) {
  return type && type.startsWith('RECYCLAGE')
}

function iconBg(type) {
  if (isRecyclageNotif(type)) return 'bg-orange-100'
  if (type === 'ABSENCE_REPRISE') return 'bg-green-100'
  if (type === 'ABSENCE_DEBUT') return 'bg-yellow-100'
  return 'bg-red-100'
}

function timeAgo(dateStr) {
  if (!dateStr) return ''
  const now = new Date()
  const d = new Date(dateStr)
  const diff = Math.floor((now - d) / 1000)
  if (diff < 60) return 'A l\'instant'
  if (diff < 3600) return Math.floor(diff / 60) + ' min'
  if (diff < 86400) return Math.floor(diff / 3600) + ' h'
  return Math.floor(diff / 86400) + ' j'
}

async function loadNotifications() {
  if (!authStore.isAuthenticated) return
  try {
    const [notifRes, countRes] = await Promise.all([
      notificationApi.getForUser(),
      notificationApi.getUnreadCount(),
    ])
    notifications.value = (notifRes.data || []).slice(0, 20)
    unreadCount.value = countRes.data || 0
  } catch (e) {
    console.error('Error loading notifications:', e)
  }
}

async function markAllRead() {
  if (!authStore.isAuthenticated) return
  try {
    await notificationApi.markAllAsRead()
    await loadNotifications()
  } catch (e) { console.error(e) }
}

async function handleClick(n) {
  if (!n.read) {
    try {
      await notificationApi.markAsRead(n.id)
      await loadNotifications()
    } catch (e) { console.error(e) }
  }
}

function handleClickOutside(e) {
  if (bellRef.value && !bellRef.value.contains(e.target)) {
    isOpen.value = false
  }
}

onMounted(() => {
  loadNotifications()
  refreshInterval = setInterval(loadNotifications, 60000)
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  if (refreshInterval) clearInterval(refreshInterval)
  document.removeEventListener('click', handleClickOutside)
})
</script>
