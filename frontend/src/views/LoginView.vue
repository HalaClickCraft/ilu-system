<script setup>
import { ref } from 'vue'

const matricule = ref('')
const motDePasse = ref('')
const message = ref('')
const loading = ref(false)

async function submitLogin() {
  loading.value = true
  message.value = ''

  try {
    const response = await fetch('/api/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        matricule: matricule.value,
        motDePasse: motDePasse.value,
      }),
    })

    const data = await response.json()

    if (!response.ok) {
      throw new Error(data.message || 'Connexion impossible')
    }

    message.value = `Connexion réussie. Token reçu : ${data.token}`
    localStorage.setItem('authToken', data.token)
  } catch (error) {
    message.value = error.message || 'Erreur inattendue'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="login-page">
    <form class="login-card" @submit.prevent="submitLogin">
      <h1>Connexion</h1>
      <label>
        Matricule
        <input v-model="matricule" required />
      </label>
      <label>
        Mot de passe
        <input v-model="motDePasse" type="password" required />
      </label>
      <button type="submit" :disabled="loading">
        {{ loading ? 'Connexion...' : 'Se connecter' }}
      </button>
      <p v-if="message" class="message">{{ message }}</p>
    </form>
  </main>
</template>

<style scoped>
.login-page {
  min-height: 80vh;
  display: grid;
  place-items: center;
  padding: 2rem;
}
.login-card {
  width: min(100%, 420px);
  display: grid;
  gap: 1rem;
  padding: 2rem;
  border: 1px solid #d9d9d9;
  border-radius: 12px;
  background: white;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}
label {
  display: grid;
  gap: 0.35rem;
  font-weight: 600;
}
input {
  padding: 0.75rem;
  border: 1px solid #c7c7c7;
  border-radius: 8px;
}
button {
  padding: 0.8rem;
  border: 0;
  border-radius: 8px;
  background: #2563eb;
  color: white;
  font-weight: 700;
  cursor: pointer;
}
button:disabled {
  opacity: 0.7;
  cursor: wait;
}
.message {
  margin: 0;
  font-size: 0.95rem;
  color: #0f766e;
}
</style>
