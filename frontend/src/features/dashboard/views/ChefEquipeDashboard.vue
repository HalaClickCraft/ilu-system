<script setup>
import { ref } from 'vue'
const logs = ref([]); const day = ref(1); const cadence = ref(100); const note = ref('')
function addLog() { logs.value.unshift({ id: Date.now(), day: day.value, cadence: cadence.value, note: note.value }); day.value++; note.value = '' }
</script>
<template><section class="role-section"><div class="stats-grid"><div class="stat-card"><div><span class="stat-val">12</span><span class="stat-lbl">Opérateurs dans l'équipe</span></div></div></div><div class="admin-grid"><form class="panel-card panel-form" @submit.prevent="addLog"><h3>Suivi d'intégration</h3><input v-model="day" type="number" /><input v-model="cadence" type="number" /><textarea v-model="note" placeholder="Remarques"></textarea><button class="submit-btn">Enregistrer</button></form><div class="panel-card"><h3>Suivis récents</h3><p v-for="log in logs" :key="log.id">Jour {{ log.day }} — {{ log.cadence }}% — {{ log.note }}</p></div></div></section></template>
