<script setup>
import LoginForm from '../components/LoginForm.vue'
</script>

<template>
  <main class="login-page">
    <LoginForm />
  </main>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  width: 100vw;
  display: grid;
  place-items: center;
  padding: 2rem;
  background: linear-gradient(135deg, #0f172a 0%, #1e3a8a 50%, #2563eb 100%);
  position: absolute;
  top: 0;
  left: 0;
  overflow: hidden;
  box-sizing: border-box;
}
</style>
