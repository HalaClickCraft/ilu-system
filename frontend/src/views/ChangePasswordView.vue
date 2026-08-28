<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50">
    <div class="w-full max-w-md px-4">
      <div class="bg-white rounded-2xl shadow-lg p-8">
        <div class="text-center mb-8">
          <div class="w-16 h-16 bg-sky-600 rounded-xl flex items-center justify-center mx-auto mb-4">
            <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path></svg>
          </div>
          <h1 class="text-2xl font-bold text-gray-900">Changer le mot de passe</h1>
          <p class="text-gray-500 text-sm mt-1">Veuillez définir un nouveau mot de passe.</p>
        </div>
        <form @submit.prevent="handleSubmit" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Mot de passe actuel</label>
            <input v-model="form.currentPassword" type="password" required class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-sky-500 focus:border-sky-500 outline-none" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Nouveau mot de passe</label>
            <input v-model="form.newPassword" type="password" required class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-sky-500 focus:border-sky-500 outline-none" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Confirmer le mot de passe</label>
            <input v-model="form.confirmPassword" type="password" required class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-sky-500 focus:border-sky-500 outline-none" />
          </div>
          <div v-if="authStore.error" class="bg-red-50 text-red-600 text-sm p-3 rounded-lg">{{ authStore.error }}</div>
          <div v-if="successMessage" class="bg-emerald-50 text-emerald-600 text-sm p-3 rounded-lg">{{ successMessage }}</div>
          <button type="submit" :disabled="authStore.loading" class="w-full bg-sky-600 hover:bg-sky-700 text-white font-medium py-2.5 rounded-lg transition-colors">
            <span v-if="authStore.loading">Chargement...</span>
            <span v-else>Enregistrer</span>
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const form = ref({ currentPassword: '', newPassword: '', confirmPassword: '' })
const successMessage = ref('')

const handleSubmit = async () => {
  if (form.value.newPassword !== form.value.confirmPassword) {
    authStore.error = 'Les mots de passe ne correspondent pas'
    return
  }
  const success = await authStore.changePassword(form.value.currentPassword, form.value.newPassword, form.value.confirmPassword)
  if (success) {
    successMessage.value = 'Mot de passe modifie avec succes'
    setTimeout(() => router.push('/'), 1500)
  }
}
</script>