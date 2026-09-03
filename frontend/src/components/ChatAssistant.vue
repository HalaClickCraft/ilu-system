<template>
  <div class="relative z-50">
    <!-- Floating Chat Toggle Button -->
    <button
      @click="toggleChat"
      class="fixed bottom-6 right-6 p-4 bg-sky-600 hover:bg-sky-700 text-white rounded-full shadow-2xl transition-all duration-300 hover:scale-105 active:scale-95 focus:outline-none flex items-center justify-center border border-sky-500"
      title="Assistant IA ILU"
    >
      <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 10h.01M12 10h.01M16 10h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"></path>
      </svg>
      <span v-if="unreadCount > 0" class="absolute -top-1 -right-1 bg-red-500 text-white text-xs font-bold rounded-full w-5 h-5 flex items-center justify-center animate-bounce">
        {{ unreadCount }}
      </span>
    </button>

    <!-- Chat Drawer Panel -->
    <div
      v-if="isOpen"
      class="fixed inset-0 bg-slate-900/30 backdrop-blur-xs transition-opacity duration-300"
      @click="closeChat"
    ></div>

    <div
      :class="[isOpen ? 'translate-x-0' : 'translate-x-full']"
      class="fixed top-0 right-0 h-full w-96 bg-white shadow-2xl flex flex-col transition-transform duration-300 transform border-l border-gray-200"
    >
      <!-- Chat Header -->
      <div class="p-4 bg-slate-900 text-white flex items-center justify-between shrink-0">
        <div class="flex items-center gap-2">
          <div class="w-8 h-8 bg-sky-600 rounded-full flex items-center justify-center font-bold text-sm">
            AI
          </div>
          <div>
            <h3 class="font-semibold text-sm leading-tight">Assistant IA ILU</h3>
            <span class="text-[10px] text-emerald-400 flex items-center gap-1 font-medium">
              <span class="w-1.5 h-1.5 bg-emerald-400 rounded-full inline-block animate-ping"></span>
              Ollama ({{ modelName }})
            </span>
          </div>
        </div>
        <div class="flex items-center gap-2">
          <button @click="clearHistory" class="p-1 hover:bg-slate-800 rounded transition text-gray-400 hover:text-white" title="Effacer l'historique">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
            </svg>
          </button>
          <button @click="closeChat" class="p-1 hover:bg-slate-800 rounded transition text-gray-400 hover:text-white">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </button>
        </div>
      </div>

      <!-- Messages Area -->
      <div ref="messagesContainer" class="flex-1 overflow-y-auto p-4 space-y-4 bg-gray-50">
        <!-- Welcome Message -->
        <div class="flex gap-2">
          <div class="w-7 h-7 bg-slate-900 rounded-full flex items-center justify-center text-white text-xs font-bold shrink-0">
            AI
          </div>
          <div class="bg-white border border-gray-200 rounded-lg p-3 max-w-[85%] text-xs shadow-xs text-gray-700">
            <p class="font-medium mb-1">Bonjour ! Je suis votre assistant IA ILU.</p>
            <p class="mb-2">Je peux vous aider à analyser la base de données de l'application et vous fournir des statistiques en temps réel.</p>
            <p class="text-[10px] text-gray-500 font-medium">Exemples de questions à me poser :</p>
            <div class="mt-2 flex flex-col gap-1.5">
              <button
                v-for="(suggestion, idx) in suggestions"
                :key="idx"
                @click="sendSuggestion(suggestion)"
                class="text-left text-xs bg-slate-50 hover:bg-slate-100 border border-gray-200 hover:border-sky-500 rounded p-1.5 transition text-slate-700 hover:text-sky-700 cursor-pointer font-medium"
              >
                {{ suggestion }}
              </button>
            </div>
          </div>
        </div>

        <!-- Chat History -->
        <div
          v-for="(msg, index) in messages"
          :key="index"
          :class="[msg.role === 'user' ? 'justify-end' : 'justify-start']"
          class="flex gap-2"
        >
          <!-- Bot Avatar -->
          <div
            v-if="msg.role !== 'user'"
            class="w-7 h-7 bg-slate-900 rounded-full flex items-center justify-center text-white text-xs font-bold shrink-0"
          >
            AI
          </div>

          <!-- Message Body -->
          <div
            :class="[
              msg.role === 'user'
                ? 'bg-sky-600 text-white rounded-br-none'
                : 'bg-white text-gray-800 border border-gray-200 rounded-bl-none shadow-xs'
            ]"
            class="rounded-lg p-3 max-w-[85%] text-xs"
          >
            <div v-html="renderContent(msg.content)" class="space-y-1 font-normal break-words markdown-body"></div>
          </div>

          <!-- User Avatar -->
          <div
            v-if="msg.role === 'user'"
            class="w-7 h-7 bg-sky-100 border border-sky-400 rounded-full flex items-center justify-center text-sky-700 text-xs font-semibold shrink-0"
          >
            U
          </div>
        </div>

        <!-- Loading Indicator -->
        <div v-if="isLoading" class="flex gap-2">
          <div class="w-7 h-7 bg-slate-900 rounded-full flex items-center justify-center text-white text-xs font-bold shrink-0">
            AI
          </div>
          <div class="bg-white border border-gray-200 rounded-lg p-3 max-w-[85%] text-xs shadow-xs text-gray-500 flex items-center gap-2">
            <span class="font-medium">L'assistant réfléchit</span>
            <span class="flex gap-1">
              <span class="w-1.5 h-1.5 bg-gray-400 rounded-full animate-bounce delay-75"></span>
              <span class="w-1.5 h-1.5 bg-gray-400 rounded-full animate-bounce delay-150"></span>
              <span class="w-1.5 h-1.5 bg-gray-400 rounded-full animate-bounce delay-220"></span>
            </span>
          </div>
        </div>
      </div>

      <!-- Quick Action Buttons (above input box) -->
      <div v-if="messages.length > 0 && !isLoading" class="px-3 py-1.5 bg-gray-50 border-t border-gray-200 flex gap-1.5 overflow-x-auto whitespace-nowrap scrollbar-thin shrink-0">
        <button
          v-for="(sug, idx) in quickSuggestions"
          :key="idx"
          @click="sendSuggestion(sug)"
          class="text-xs bg-white border border-gray-200 hover:border-sky-500 text-gray-600 hover:text-sky-600 rounded px-2 py-1 transition cursor-pointer font-medium"
        >
          {{ sug }}
        </button>
      </div>

      <!-- Input Footer -->
      <div class="p-3 border-t border-gray-200 bg-white flex items-center gap-2 shrink-0">
        <input
          v-model="inputMessage"
          @keydown.enter="sendMessage"
          type="text"
          placeholder="Posez votre question sur les statistiques..."
          class="flex-1 bg-gray-50 focus:bg-white text-xs border border-gray-300 focus:border-sky-500 rounded-lg px-3 py-2.5 focus:outline-none transition"
          :disabled="isLoading"
        />
        <button
          @click="sendMessage"
          class="p-2.5 bg-sky-600 hover:bg-sky-700 disabled:bg-gray-300 text-white rounded-lg transition active:scale-95 cursor-pointer"
          :disabled="isLoading || !inputMessage.trim()"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"></path>
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, watch } from 'vue'
import { chatbotApi } from '@/api/endpoints'

const isOpen = ref(false)
const isLoading = ref(false)
const inputMessage = ref('')
const unreadCount = ref(0)
const sessionId = ref('')
const modelName = ref('llama3')

const messages = ref([])

const suggestions = [
  "Donne-moi un résumé général des effectifs",
  "Combien d'opérateurs sont certifiés par poste ?",
  "Quelles sont les absences en cours ?",
  "Quels sont les recyclages planifiés ?"
]

const quickSuggestions = [
  "Résumé global",
  "Postes et certifications",
  "Absences actives",
  "Recyclages en cours"
]

const messagesContainer = ref(null)

const toggleChat = () => {
  isOpen.value = !isOpen.value
  if (isOpen.value) {
    unreadCount.value = 0
    scrollToBottom()
  }
}

const closeChat = () => {
  isOpen.value = false
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const sendSuggestion = (text) => {
  inputMessage.value = text
  sendMessage()
}

const sendMessage = async () => {
  const query = inputMessage.value.trim()
  if (!query || isLoading.value) return

  messages.value.push({
    role: 'user',
    content: query
  })
  inputMessage.value = ''
  isLoading.value = true
  scrollToBottom()

  try {
    const response = await chatbotApi.chat(query, sessionId.value)
    if (response.data) {
      messages.value.push({
        role: 'assistant',
        content: response.data.response
      })
      if (response.data.sessionId) {
        sessionId.value = response.data.sessionId
        localStorage.setItem('ilu_chat_session_id', response.data.sessionId)
      }
    }
  } catch (error) {
    console.error('Chatbot error:', error)
    messages.value.push({
      role: 'assistant',
      content: "Une erreur s'est produite lors de la connexion à l'assistant. Veuillez vérifier que Ollama est en cours d'exécution."
    })
  } finally {
    isLoading.value = false
    scrollToBottom()
  }
}

const clearHistory = () => {
  messages.value = []
  sessionId.value = ''
  localStorage.removeItem('ilu_chat_session_id')
}

// Simple local markdown-to-html renderer
const renderContent = (content) => {
  if (!content) return ''
  let html = content
    // Escape HTML to prevent injection
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    
  // Headers (### Header)
  html = html.replace(/^### (.*$)/gim, '<h4 class="font-bold text-slate-800 text-xs mt-2 mb-1 border-b border-gray-100 pb-0.5">$1</h4>')
  html = html.replace(/^## (.*$)/gim, '<h3 class="font-bold text-slate-900 text-sm mt-3 mb-1">$1</h3>')
  html = html.replace(/^# (.*$)/gim, '<h2 class="font-bold text-slate-900 text-base mt-4 mb-2">$1</h2>')
  
  // Bold (**text**)
  html = html.replace(/\*\*(.*?)\*\*/g, '<strong class="font-bold text-slate-900">$1</strong>')
  
  // Lists (- Item or * Item)
  html = html.replace(/^\s*[-*]\s+(.*$)/gim, '<li class="ml-4 list-disc text-gray-700 py-0.5">$1</li>')
  
  // Wrap sequential lists in ul tags
  html = html.replace(/(<li.*<\/li>)/gs, '<ul class="my-1.5">$1</ul>')
  
  // Paragraph line breaks
  html = html.replace(/\n/g, '<br />')

  return html
}

onMounted(() => {
  const cachedSessionId = localStorage.getItem('ilu_chat_session_id')
  if (cachedSessionId) {
    sessionId.value = cachedSessionId
  }
})
</script>

<style>
/* Style scope for Markdown content inside the chat bubbles */
.markdown-body ul {
  padding-left: 1rem;
}
.markdown-body li {
  margin-top: 0.125rem;
  margin-bottom: 0.125rem;
}
.markdown-body h4 {
  font-weight: 700;
  margin-top: 0.5rem;
}
</style>
