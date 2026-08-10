<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Matrice de Polyvalence</h1>
        <p class="text-sm text-gray-500 mt-1">Vue d'ensemble des niveaux par operateur et poste</p>
      </div>
      <div class="flex items-center gap-3 text-xs">
        <span class="flex items-center gap-1"><span class="w-4 h-4 rounded bg-gray-200 inline-block"></span> Non forme</span>
        <span class="flex items-center gap-1"><span class="w-4 h-4 rounded bg-amber-400 inline-block"></span> Niveau I</span>
        <span class="flex items-center gap-1"><span class="w-4 h-4 rounded bg-blue-500 inline-block"></span> Niveau L</span>
        <span class="flex items-center gap-1"><span class="w-4 h-4 rounded bg-green-600 inline-block"></span> Niveau U</span>
      </div>
    </div>

    <!-- Niveau legend -->
    <div class="bg-white rounded-xl border p-4">
      <h3 class="font-semibold text-sm mb-2">Regles de Niveau</h3>
      <div class="grid grid-cols-3 gap-4 text-sm">
        <div class="bg-amber-50 rounded-lg p-3">
          <p class="font-bold text-amber-700">Niveau I</p>
          <p class="text-amber-600">&lt; 6 mois d'anciennete, score 70%+</p>
        </div>
        <div class="bg-blue-50 rounded-lg p-3">
          <p class="font-bold text-blue-700">Niveau L</p>
          <p class="text-blue-600">6+ mois d'anciennete, score 81%+</p>
        </div>
        <div class="bg-green-50 rounded-lg p-3">
          <p class="font-bold text-green-700">Niveau U</p>
          <p class="text-green-600">12+ mois d'anciennete, score 91%+</p>
        </div>
      </div>
      <p class="text-xs text-red-600 mt-2 font-medium">HSE + Qualite generique doivent etre a 100% (obligatoire)</p>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="text-center py-12 text-gray-400">Chargement de la matrice...</div>

    <!-- Matrix table -->
    <div v-else class="bg-white rounded-xl border overflow-x-auto">
      <table class="w-full text-sm">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-4 py-3 text-left font-semibold text-gray-700 sticky left-0 bg-gray-50 z-10 min-w-[180px]">Operateur</th>
            <th class="px-3 py-3 text-center font-semibold text-gray-700 min-w-[50px]">HSE</th>
            <th class="px-3 py-3 text-center font-semibold text-gray-700 min-w-[50px]">Qualite</th>
            <th class="px-3 py-3 text-center font-semibold text-gray-700 min-w-[50px]">Anciennete</th>
            <th v-for="ws in workstations" :key="ws.id" class="px-3 py-3 text-center font-semibold text-gray-700 min-w-[80px]">
              <div class="truncate text-xs" :title="ws.name">{{ ws.name }}</div>
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="op in matrixData.operators" :key="op.operatorId" class="border-t hover:bg-gray-50">
            <td class="px-4 py-3 sticky left-0 bg-white z-10">
              <p class="font-medium text-gray-900 truncate">{{ op.operatorName }}</p>
              <p class="text-xs text-gray-400">{{ op.employeeId }}</p>
            </td>
            <td class="px-3 py-3 text-center">
              <span :class="op.hsePassed ? 'bg-green-500' : 'bg-gray-200 text-gray-500'" class="inline-block w-8 h-8 leading-8 rounded-lg text-white text-xs font-bold">
                {{ op.hsePassed ? 'OK' : '-' }}
              </span>
            </td>
            <td class="px-3 py-3 text-center">
              <span :class="op.qualityPassed ? 'bg-green-500' : 'bg-gray-200 text-gray-500'" class="inline-block w-8 h-8 leading-8 rounded-lg text-white text-xs font-bold">
                {{ op.qualityPassed ? 'OK' : '-' }}
              </span>
            </td>
            <td class="px-3 py-3 text-center text-xs text-gray-600">{{ op.seniorityMonths }} mois</td>
            <td v-for="ws in workstations" :key="ws.id" class="px-3 py-3 text-center">
              <span :class="niveauBgClass(op.workstations?.[ws.name])" class="inline-block w-10 h-10 leading-10 rounded-lg text-white font-bold text-sm">
                {{ op.workstations?.[ws.name] || '' }}
              </span>
            </td>
          </tr>
          <tr v-if="!matrixData.operators?.length">
            <td colspan="99" class="px-4 py-8 text-center text-gray-400">Aucun operateur trouve</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { evaluationApi } from '@/api/endpoints'

const loading = ref(true)
const matrixData = ref({ operators: [], workstations: [], niveauRules: [] })

const workstations = computed(() => matrixData.value.workstations || [])

const niveauBgClass = (n) => ({
  I: 'bg-amber-400',
  L: 'bg-blue-500',
  U: 'bg-green-600'
}[n] || 'bg-gray-100 text-gray-400')

async function load() {
  loading.value = true
  try {
    const res = await evaluationApi.getMatrix()
    matrixData.value = res.data || { operators: [], workstations: [], niveauRules: [] }
  } catch (e) {
    console.error('Error loading matrix', e)
  }
  loading.value = false
}

onMounted(load)
</script>