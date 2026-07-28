<script setup>
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const changePasswordMsg = ref('')
const changePasswordSuccess = ref(false)
const changePasswordLoading = ref(false)

async function handlePasswordChange() {
  if (newPassword.value !== confirmPassword.value) {
    changePasswordMsg.value = 'Les mots de passe ne correspondent pas.'
    return
  }
  changePasswordLoading.value = true
  changePasswordMsg.value = ''
  try {
    await authStore.changePassword(oldPassword.value, newPassword.value)
    changePasswordSuccess.value = true
    changePasswordMsg.value = 'Mot de passe mis à jour avec succès! Redirection...'
    setTimeout(() => {
      changePasswordSuccess.value = false
      oldPassword.value = ''
      newPassword.value = ''
      confirmPassword.value = ''
    }, 1500)
  } catch (error) {
    changePasswordMsg.value = error.message || 'Impossible de changer le mot de passe.'
  } finally {
    changePasswordLoading.value = false
  }
}
</script>

<template>
  <div class="password-change-overlay">
    <div class="password-card">
      <div class="card-header">
        <span class="lock-icon">🔒</span>
        <h2>Changement de mot de passe obligatoire</h2>
        <p class="desc">
          C'est votre première connexion. Veuillez définir un nouveau mot de passe pour des
          raisons de sécurité.
        </p>
      </div>

      <form @submit.prevent="handlePasswordChange" class="pwd-form">
        <div class="input-group">
          <label>Mot de passe actuel</label>
          <input v-model="oldPassword" type="password" required placeholder="Saisir votre mot de passe de test" />
        </div>
        <div class="input-group">
          <label>Nouveau mot de passe</label>
          <input v-model="newPassword" type="password" required placeholder="Minimum 6 caractères" />
        </div>
        <div class="input-group">
          <label>Confirmer le nouveau mot de passe</label>
          <input v-model="confirmPassword" type="password" required placeholder="Confirmer le mot de passe" />
        </div>
        <button type="submit" :disabled="changePasswordLoading" class="action-btn">
          {{ changePasswordLoading ? 'Modification...' : 'Modifier et continuer' }}
        </button>
        <div v-if="changePasswordMsg" :class="['message-box', changePasswordSuccess ? 'success' : 'error']">
          {{ changePasswordMsg }}
        </div>
      </form>
    </div>
  </div>
</template>