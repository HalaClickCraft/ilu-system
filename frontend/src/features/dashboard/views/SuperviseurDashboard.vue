<script setup>
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { fetchStructure } from '@/features/structure/services/structureService'
import { fetchAllOperators, assignPoste } from '@/features/dashboard/services/operateurService'
import StatisticsDashboard from '@/features/formations/components/StatisticsDashboard.vue'

const authStore = useAuthStore()

// Real states
const operators = ref([])
const structure = ref({ projects: [] })
const loading = ref(false)
const error = ref('')

const selectedOperator = ref('')
const selectedPoste = ref('')
const assignLoading = ref(false)
const assignMsg = ref('')

// Show formations stats view
const showFormations = ref(false)

// Flat list of all workstations in the system
const allPostes = computed(() => {
  const list = []
  if (!structure.value.projects) return list
  for (const project of structure.value.projects) {
    if (!project.zones) continue
    for (const zone of project.zones) {
      if (!zone.postes) continue
      for (const poste of zone.postes) {
        list.push({
          id: poste.idPoste,
          nom: `${project.nom} - ${zone.nom} - ${poste.nom}`,
        })
      }
    }
  }
  return list
})

// Operators who currently have a workstation assigned
const assignedOperators = computed(() => {
  return operators.value.filter((op) => op.posteAffecte)
})

async function loadData() {
  loading.value = true
  error.value = ''
  try {
    const [opsData, structData] = await Promise.all([
      fetchAllOperators(authStore.token),
      fetchStructure(authStore.token),
    ])
    operators.value = opsData
    structure.value = structData
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
}

async function handleAssign() {
  if (!selectedOperator.value) return
  assignLoading.value = true
  assignMsg.value = ''
  try {
    const pId = selectedPoste.value ? Number(selectedPoste.value) : null
    await assignPoste(authStore.token, selectedOperator.value, pId)
    assignMsg.value = 'Affectation mise à jour avec succès!'
    selectedOperator.value = ''
    selectedPoste.value = ''
    await loadData()
  } catch (err) {
    assignMsg.value = `Erreur: ${err.message}`
  } finally {
    assignLoading.value = false
  }
}

onMounted(loadData)
</script>


