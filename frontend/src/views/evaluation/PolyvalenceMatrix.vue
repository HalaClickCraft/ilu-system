<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">MATRICE DE POLYVALENCE KJ92</h1>
        <p class="text-sm text-gray-500 mt-1">Indicateur de polyvalence: Minimum 6 personnes formées par poste => 6 personnes en L</p>
      </div>
      
      <!-- Legend -->
      <div class="flex items-center gap-3 text-xs">
        <span class="flex items-center gap-1"><span class="w-4 h-4 rounded bg-gray-100 border inline-block"></span> Non formé</span>
        <span class="flex items-center gap-1"><span class="w-4 h-4 rounded bg-amber-400 inline-block text-white"></span> Niveau I</span>
        <span class="flex items-center gap-1"><span class="w-4 h-4 rounded bg-blue-500 inline-block text-white"></span> Niveau L</span>
        <span class="flex items-center gap-1"><span class="w-4 h-4 rounded bg-green-600 inline-block text-white"></span> Niveau U</span>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="text-center py-12 text-gray-400">
      Chargement de la matrice...
    </div>

    <!-- Matrix table -->
    <div v-else class="bg-white rounded-xl border overflow-x-auto shadow-sm">
      <table class="min-w-full border-collapse text-xs text-gray-700">
        <thead>
          <!-- ROW 1 (Zones) -->
          <tr class="bg-gray-100 border-b border-gray-200">
            <th rowspan="3" class="px-4 py-3 text-left font-bold text-gray-700 sticky left-0 bg-gray-100 z-20 border-r border-gray-200 min-w-[180px]">
              Opérateur
            </th>
            <th rowspan="3" class="px-3 py-3 text-center font-bold text-gray-700 border-r border-gray-200 min-w-[120px]">
              Mode / Détails
            </th>
            <th v-for="z in zones" :key="z.name" :colspan="z.workstations.length" class="px-3 py-2 text-center font-bold text-gray-850 border-r border-gray-200 bg-gray-50">
              {{ z.name }}
            </th>
            <th rowspan="3" class="px-4 py-3 text-center font-bold text-gray-700 border-l border-gray-200 bg-gray-50 max-w-[150px] whitespace-normal">
              Nombre des postes sur lesquels est formé un opérateur
            </th>
          </tr>
          
          <!-- ROW 2 (Postes) -->
          <tr class="bg-gray-100 border-b border-gray-200">
            <th v-for="col in allColumns" :key="col.id" class="px-3 py-2 text-center font-semibold text-gray-700 border-r border-gray-200 min-w-[90px]">
              {{ col.name }}
            </th>
          </tr>
          
          <!-- ROW 3 (Target) -->
          <tr class="bg-gray-50 border-b border-gray-300 text-gray-500">
            <th v-for="col in allColumns" :key="col.id + '-target'" class="px-3 py-1.5 text-center font-medium border-r border-gray-200">
              {{ col.targetLevel || 'L' }}
            </th>
          </tr>
        </thead>
        
        <tbody class="divide-y divide-gray-200">
          <template v-for="op in matrixData.operators" :key="op.operatorId">
            <!-- Row A (Upper: Date) -->
            <tr class="hover:bg-gray-50">
              <td rowspan="2" class="px-4 py-3 sticky left-0 bg-white z-10 font-bold text-gray-900 border-r border-gray-200">
                {{ op.operatorName }}
                <div class="text-[10px] text-gray-400 font-normal">{{ op.employeeId }} · {{ op.seniorityMonths }} mois</div>
              </td>
              <td class="px-3 py-2 font-medium text-gray-500 bg-gray-50 border-r border-gray-200 text-left">
                {{ getOperatorLabelRow1(op) }}
              </td>
              <!-- Dates for columns -->
              <td v-for="col in allColumns" :key="col.id + '-date'" class="px-2 py-2 text-center border-r border-gray-150 text-[10px] text-gray-600">
                {{ getColumnDate(op, col) }}
              </td>
              <!-- Spanned row for trained count -->
              <td rowspan="2" class="px-4 py-3 text-center border-l border-gray-200 font-bold text-lg text-slate-800 bg-slate-50">
                {{ getTrainedCount(op) }}
              </td>
            </tr>
            
            <!-- Row B (Lower: Competence Level) -->
            <tr class="hover:bg-gray-50 border-b border-gray-300 last:border-b-0">
              <td class="px-3 py-2 font-medium text-gray-500 bg-gray-50 border-r border-gray-200 text-left">
                {{ getOperatorLabelRow2(op) }}
              </td>
              <!-- Levels for columns -->
              <td v-for="col in allColumns" :key="col.id + '-level'" class="px-2 py-2 text-center border-r border-gray-150">
                <span :class="niveauBgClass(getColumnLevel(op, col))" class="inline-block w-8 h-8 leading-8 rounded-lg text-white font-bold text-xs shadow-sm">
                  {{ getColumnLevel(op, col) }}
                </span>
              </td>
            </tr>
          </template>
          
          <tr v-if="!matrixData.operators?.length">
            <td colspan="99" class="px-4 py-8 text-center text-gray-400">Aucun opérateur trouvé</td>
          </tr>
        </tbody>
        
        <!-- BOTTOM SUMMARY -->
        <tfoot v-if="matrixData.operators?.length" class="bg-gray-100 border-t-2 border-gray-300 font-medium">
          <!-- Summary I -->
          <tr class="border-b">
            <td colspan="2" class="px-4 py-3 text-left font-semibold text-amber-700 bg-amber-50/50">
              Nombres de personnes au niveau I
            </td>
            <td v-for="col in allColumns" :key="col.id + '-sumI'" class="px-2 py-3 text-center font-bold text-amber-700 bg-amber-50/30 border-r border-gray-200">
              {{ getCountPerNiveau(col, 'I') }}
            </td>
            <td class="bg-gray-100"></td>
          </tr>
          <!-- Summary L -->
          <tr class="border-b">
            <td colspan="2" class="px-4 py-3 text-left font-semibold text-blue-700 bg-blue-50/50">
              Nombres de personnes au niveau L
            </td>
            <td v-for="col in allColumns" :key="col.id + '-sumL'" class="px-2 py-3 text-center font-bold text-blue-700 bg-blue-50/30 border-r border-gray-200">
              {{ getCountPerNiveau(col, 'L') }}
            </td>
            <td class="bg-gray-100"></td>
          </tr>
          <!-- Summary U -->
          <tr>
            <td colspan="2" class="px-4 py-3 text-left font-semibold text-green-700 bg-green-50/50">
              Nombres de personnes au niveau U
            </td>
            <td v-for="col in allColumns" :key="col.id + '-sumU'" class="px-2 py-3 text-center font-bold text-green-700 bg-green-50/30 border-r border-gray-200">
              {{ getCountPerNiveau(col, 'U') }}
            </td>
            <td class="bg-gray-100"></td>
          </tr>
        </tfoot>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { evaluationApi } from '@/api/endpoints'

const loading = ref(true)
const matrixData = ref({ operators: [], workstations: [] })

const workstations = computed(() => matrixData.value.workstations || [])

const zones = computed(() => {
  const map = {}
  
  // 1. Add "Partie Générique" first
  map["Partie Générique"] = {
    name: "Partie Générique",
    workstations: [
      { id: "generic_security", name: "Securité/5s", isGeneric: true, targetLevel: "L" },
      { id: "generic_quality", name: "Qualité", isGeneric: true, targetLevel: "L" }
    ]
  }

  // 2. Add other workstations grouped by zone name
  workstations.value.forEach(ws => {
    const zoneName = ws.zoneName || "Autres"
    if (!map[zoneName]) {
      map[zoneName] = { name: zoneName, workstations: [] }
    }
    map[zoneName].workstations.push({
      ...ws,
      isGeneric: false
    })
  })

  return Object.values(map)
})

const allColumns = computed(() => {
  return zones.value.flatMap(z => z.workstations)
})

function getOperatorLabelRow1(op) {
  const hasRecyclage = Object.values(op.workstations || {}).some(w => w?.mode === 'RECYCLAGE') || op.genericMode === 'RECYCLAGE'
  return hasRecyclage ? "Date recyclage" : "Date d'évaluation"
}

function getOperatorLabelRow2(op) {
  const hasRecyclage = Object.values(op.workstations || {}).some(w => w?.mode === 'RECYCLAGE') || op.genericMode === 'RECYCLAGE'
  return hasRecyclage ? "Résultat recyclage" : "Niveau compétence"
}

function getColumnDate(op, col) {
  if (col.isGeneric) {
    return op.genericDate || '—'
  } else {
    return op.workstations?.[col.id]?.date || '—'
  }
}

function getColumnLevel(op, col) {
  if (col.isGeneric) {
    return op.genericPassed ? op.genericLevel || 'L' : ''
  } else {
    return op.workstations?.[col.id]?.level || ''
  }
}

function getTrainedCount(op) {
  if (!op.workstations) return 0
  return Object.values(op.workstations).filter(ws => ['I', 'L', 'U'].includes(ws.level)).length
}

function getCountPerNiveau(col, niveau) {
  return (matrixData.value.operators || []).filter(op => {
    const lvl = getColumnLevel(op, col)
    return lvl === niveau
  }).length
}

const niveauBgClass = (n) => ({
  I: 'bg-amber-400 text-white',
  L: 'bg-blue-500 text-white',
  U: 'bg-green-600 text-white'
}[n] || 'bg-gray-100 text-gray-400 border border-gray-200')

async function load() {
  loading.value = true
  try {
    const res = await evaluationApi.getMatrix()
    matrixData.value = res.data || { operators: [], workstations: [] }
  } catch (e) {
    console.error('Error loading matrix', e)
  }
  loading.value = false
}

onMounted(load)
</script>