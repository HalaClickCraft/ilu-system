<template>
  <div class="relative" ref="bellRef">
    <button @click.stop="isOpen = !isOpen" class="relative p-2.5 rounded-lg hover:bg-gray-100 transition group" title="Notifications">
      <svg class="w-6 h-6 text-gray-600 group-hover:text-gray-900 transition" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"/>
      </svg>
      <span v-if="unreadCount > 0" class="absolute -top-0.5 -right-0.5 bg-red-500 text-white text-[10px] font-bold rounded-full min-w-[18px] h-[18px] flex items-center justify-center px-1">
        {{ unreadCount > 9 ? '9+' : unreadCount }}
      </span>
    </button>

    <!-- Dropdown -->
    <div v-if="isOpen" class="absolute right-0 top-full mt-2 w-84 sm:w-96 bg-white rounded-xl shadow-xl border border-gray-200 z-50 max-h-[28rem] flex flex-col">
      <div class="px-4 py-3 border-b border-gray-100 flex items-center justify-between">
        <div class="flex items-center gap-2">
          <h3 class="text-sm font-bold text-gray-900">Notifications</h3>
          <span v-if="notifications.length > 0" class="text-xs px-2 py-0.5 rounded-full bg-gray-100 text-gray-600 font-semibold">
            {{ notifications.length }}
          </span>
        </div>
        <div class="flex items-center gap-2" v-if="notifications.length > 0">
          <button @click.stop="markAllRead" class="text-xs text-emerald-600 hover:text-emerald-800 font-medium hover:underline transition">
            Tout marquer lu
          </button>
          <span class="text-gray-300">•</span>
          <button @click.stop="clearAll" class="text-xs text-red-500 hover:text-red-700 font-medium hover:underline transition" title="Effacer toutes les notifications">
            Vider
          </button>
        </div>
      </div>
      <div class="overflow-y-auto flex-1">
        <div
          v-for="n in notifications"
          :key="n.id"
          @click.stop="handleClick(n)"
          :class="n.read ? 'bg-white' : 'bg-emerald-50/60'"
          class="px-4 py-3 border-b border-gray-50 cursor-pointer hover:bg-gray-50 transition group relative"
        >
          <div class="flex items-start gap-3">
            <div :class="iconBg(n.type)" class="w-8 h-8 rounded-full flex items-center justify-center shrink-0 mt-0.5">
              <svg v-if="isRecyclageNotif(n.type)" class="w-4 h-4 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/></svg>
              <svg v-else-if="n.type === 'ABSENCE_REPRISE'" class="w-4 h-4 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/></svg>
              <svg v-else class="w-4 h-4 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4.5c-.77-.833-2.694-.833-3.464 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z"/></svg>
            </div>
            <div class="flex-1 min-w-0 pr-6">
              <p class="text-xs sm:text-sm text-gray-800 leading-snug" :class="n.read ? 'font-normal' : 'font-semibold'">{{ n.message }}</p>
              <p class="text-[11px] text-gray-400 mt-1 flex items-center gap-1">
                <span>{{ timeAgo(n.createdAt) }}</span>
                <span v-if="!n.read" class="inline-block w-1.5 h-1.5 bg-emerald-500 rounded-full"></span>
              </p>
            </div>
            
            <!-- Delete single notification button -->
            <button
              @click.stop="deleteNotif(n)"
              class="opacity-0 group-hover:opacity-100 p-1 text-gray-400 hover:text-red-600 rounded transition absolute top-3 right-3"
              title="Supprimer cette notification"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </button>
          </div>
        </div>
        <div v-if="notifications.length === 0" class="px-4 py-10 text-center text-sm text-gray-400 flex flex-col items-center justify-center gap-2">
          <svg class="w-8 h-8 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"/>
          </svg>
          <span>Aucune notification pour le moment</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { notificationApi } from '@/services/notificationApi'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
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
  if (diff < 60) return "A l'instant"
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
    const count = countRes.data?.unreadCount
    unreadCount.value = typeof count === 'number' ? count : (parseInt(count, 10) || 0)
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

async function deleteNotif(n) {
  if (!authStore.isAuthenticated) return
  try {
    await notificationApi.delete(n.id)
    notifications.value = notifications.value.filter(item => item.id !== n.id)
    if (!n.read && unreadCount.value > 0) {
      unreadCount.value--
    }
  } catch (e) {
    console.error('Error deleting notification:', e)
  }
}

async function clearAll() {
  if (!authStore.isAuthenticated) return
  try {
    await notificationApi.clearAll()
    notifications.value = []
    unreadCount.value = 0
  } catch (e) {
    console.error('Error clearing notifications:', e)
  }
}

async function handleClick(n) {
  if (!n.read) {
    try {
      await notificationApi.markAsRead(n.id)
      await loadNotifications()
    } catch (e) { console.error(e) }
  }
  // Navigate to the screen the notification is about
  isOpen.value = false
  if (isRecyclageNotif(n.type)) {
    router.push('/recyclage')
  } else if (n.type === 'ABSENCE_REPRISE' || n.type === 'ABSENCE_DEBUT') {
    router.push('/absences')
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
