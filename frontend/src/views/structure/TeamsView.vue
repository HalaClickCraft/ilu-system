<template>
  <div class="space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Répartition par Projet</h1>
        <p class="text-gray-500 mt-1">
          Qui travaille sur quel projet — utile pour l'affectation des opérateurs.
        </p>
      </div>
    </div>

    <div v-if="loading" class="flex items-center justify-center py-20">
      <div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div>
    </div>

    <div v-else-if="groups.length === 0" class="bg-white rounded-xl shadow-sm border border-gray-200 p-12 text-center text-gray-400">
      Aucun opérateur affecté à un projet pour le moment.
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <div v-for="group in groups" :key="group.projectId || '_none'" class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
        <div class="flex items-center justify-between mb-3">
          <h3 class="font-semibold text-gray-900 flex items-center gap-2">
            <svg v-if="group.projectId" class="w-4 h-4 text-emerald-600 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z"></path></svg>
            {{ group.projectName }}
          </h3>
          <span class="text-xs text-gray-500 bg-gray-50 px-2 py-0.5 rounded-full border">{{ group.operators.length }} opérateur(s)</span>
        </div>
        <ul class="space-y-1.5">
          <li v-for="op in group.operators" :key="op.id" class="flex items-center justify-between py-1.5 px-2 rounded bg-gray-50 text-sm">
            <router-link :to="'/operators/' + op.id" class="text-emerald-600 hover:underline font-medium">
              — {{ op.lastName }} {{ op.firstName }}
            </router-link>
            <span class="inline-flex items-center px-1.5 py-0.5 rounded text-xs" :class="op.active !== false ? 'bg-emerald-100 text-emerald-700' : 'bg-red-100 text-red-700'">
              {{ op.active !== false ? 'Actif' : 'Inactif' }}
            </span>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { operatorsApi, structureApi } from '@/api/endpoints'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const loading = ref(true)
const operators = ref([])
const projects = ref([])

// RH / Superviseur / Admin need visibility across every project.
// Chef d'equipe only needs to see the project(s) he actually leads.
const seesAllProjects = computed(() => auth.hasAnyRole(['ADMIN', 'RH', 'SUPERVISEUR']))

// Projects the current chef d'equipe is a member of (used to scope the view)
const myProjectIds = computed(() => {
  if (seesAllProjects.value) return null // null = no restriction
  const empId = auth.user?.employeeId
  return new Set(
    projects.value
      .filter(p => p.members?.some(m => m.employeeId === empId))
      .map(p => p.id)
  )
})

const groups = computed(() => {
  const byProject = {}
  for (const op of operators.value) {
    const proj = op.project
    if (!seesAllProjects.value) {
      // Chef d'equipe: only his own project(s)
      if (!proj || !myProjectIds.value.has(proj.id)) continue
    }
    const key = proj ? proj.id : '_none'
    if (!byProject[key]) {
      byProject[key] = { projectId: proj ? proj.id : null, projectName: proj ? proj.name : 'Sans projet', operators: [] }
    }
    byProject[key].operators.push(op)
  }
  return Object.values(byProject).sort((a, b) => {
    if (!a.projectId) return 1
    if (!b.projectId) return -1
    return a.projectName.localeCompare(b.projectName)
  })
})

const fetchData = async () => {
  loading.value = true
  try {
    const [opsRes, projRes] = await Promise.all([operatorsApi.getAll(), structureApi.getAll()])
    operators.value = opsRes.data || []
    projects.value = projRes.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>
