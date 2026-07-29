<template>
  <div class="space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div><h1 class="text-2xl font-bold text-gray-900">Equipes</h1><p class="text-gray-500 mt-1">Gestion des equipes et repartition des operateurs</p></div>
    </div>

    <div v-if="loading" class="flex items-center justify-center py-20"><div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div></div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <div v-for="team in teams" :key="team.id" class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
        <div class="flex items-center justify-between mb-3">
          <h3 class="font-semibold text-gray-900">{{ team.name }}</h3>
          <span class="text-sm text-gray-500">{{ team.operatorCount || 0 }} operateurs</span>
        </div>
        <p v-if="team.teamLeader" class="text-sm text-gray-500 mb-3">Chef d'equipe: <span class="font-medium text-gray-700">{{ team.teamLeader }}</span></p>
        <div v-if="team.operators?.length" class="space-y-1.5">
          <div v-for="op in team.operators.slice(0, 5)" :key="op.id" class="flex items-center justify-between py-1.5 px-2 rounded bg-gray-50 text-sm">
            <router-link :to="'/operators/' + op.id" class="text-emerald-600 hover:underline font-medium">{{ op.lastName }} {{ op.firstName }}</router-link>
            <span class="inline-flex items-center px-1.5 py-0.5 rounded text-xs" :class="op.active !== false ? 'bg-emerald-100 text-emerald-700' : 'bg-red-100 text-red-700'">{{ op.active !== false ? 'Actif' : 'Inactif' }}</span>
          </div>
          <p v-if="team.operators.length > 5" class="text-xs text-gray-400 pl-2">+ {{ team.operators.length - 5 }} autres...</p>
        </div>
        <div v-else class="text-sm text-gray-400">Aucun operateur dans cette equipe</div>
      </div>
    </div>

    <div v-if="!loading && teams.length === 0" class="bg-white rounded-xl shadow-sm border border-gray-200 p-12 text-center text-gray-400">Aucune equipe configuree</div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { operatorsApi } from '../../api/endpoints.js'

const loading = ref(true)
const teams = ref([])

const fetchTeams = async () => {
  loading.value = true
  try {
    const r = await operatorsApi.getAll()
    const operators = r.data
    const teamMap = {}
    operators.forEach(op => {
      const teamId = op.team?.id
      if (!teamId) return
      if (!teamMap[teamId]) teamMap[teamId] = { id: teamId, name: op.team.name, teamLeader: op.team.teamLeader, operators: [] }
      teamMap[teamId].operators.push(op)
    })
    teams.value = Object.values(teamMap).map(t => ({ ...t, operatorCount: t.operators.length }))
  } catch (e) { console.error(e) } finally { loading.value = false }
}

onMounted(fetchTeams)
</script>