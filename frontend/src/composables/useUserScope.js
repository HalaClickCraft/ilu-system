import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { structureApi } from '@/api/endpoints'

// Configuration of shift-like "mini-teams" around Chef d'Équipe
// Key: Chef d'Équipe Employee ID (teamLeaderEmployeeId)
// Value: Array of Employee IDs of other roles (Agent Qualité, HSE, Superviseur, etc.) who work in this shift
export const SHIFT_TEAMS_CONFIG = {
  // Équipe / Shift 1 (TL001 = Chef 1)
  'TL001': ['AQ001', 'RQ001', 'HSE001', 'SUP001'],
  
  // Équipe / Shift 2 (TL002 = Chef 2)
  'TL002': ['AQ002', 'AQ003', 'HSE001', 'SUP001', 'RQ001'],
  
  // Équipe / Shift 3 (TL003 = Chef 3)
  'TL003': ['AQ003', 'HSE001', 'SUP001']
}

const userProjects = ref([])
const loaded = ref(false)

export function useUserScope() {
  const authStore = useAuthStore()

  const loadUserProjects = async () => {
    try {
      const res = await structureApi.getAll()
      userProjects.value = res.data || []
      loaded.value = true
    } catch (e) {
      console.error('Error loading user project scope:', e)
    }
  }

  const empId = computed(() => authStore.user?.employeeId)

  // Roles that are strictly restricted to their assigned project(s)
  const isRestrictedRole = computed(() => {
    return (
      !authStore.hasAnyRole(['ADMIN', 'RH', 'SUPERVISEUR', 'RESP_HSE', 'RESP_QUALITE', 'DEPT_PROCESS', 'DEPT_MAINTENANCE', 'DEPT_DGT_MANUFACTURING']) &&
      !authStore.isSuperviseur &&
      !authStore.isAdmin &&
      !authStore.isRh &&
      !authStore.isRespQualite &&
      !authStore.isRespHse
    )
  })

  // Set of project IDs current user is assigned to
  const myProjectIds = computed(() => {
    if (!empId.value) return new Set()
    const set = new Set()
    for (const p of userProjects.value) {
      set.add(p.id)
    }
    return set
  })

  // Set of shifts assigned to current user (for CHEF_EQUIPE)
  const myShifts = computed(() => {
    return new Set()
  })

  // Set of workstation IDs current user is assigned to
  const myWorkstationIds = computed(() => {
    if (!empId.value) return new Set()
    const set = new Set()
    for (const p of userProjects.value) {
      if (myProjectIds.value.has(p.id)) {
        for (const z of (p.zones || [])) {
          for (const w of (z.workstations || [])) {
            set.add(w.id)
          }
        }
      }
    }
    return set
  })

  const normalizeArray = (val) => {
    if (!val) return []
    if (typeof val === 'object' && 'value' in val && !Array.isArray(val)) {
      val = val.value
    }
    if (Array.isArray(val)) return val
    if (Array.isArray(val?.operators)) return val.operators
    if (Array.isArray(val?.content)) return val.content
    if (Array.isArray(val?.data)) return val.data
    return []
  }

  // Helper to filter workstations list
  const filterWorkstations = (rawList) => {
    return normalizeArray(rawList)
  }

  // Helper to filter operators list
  const filterOperators = (rawList) => {
    const operatorList = normalizeArray(rawList)
    if (operatorList.length === 0) return []

    // Global visibility: Responsable Qualité, Superviseur, RH, HSE, Admin, Process, Maintenance see all shifts/projects
    if (
      authStore.isRespQualite ||
      authStore.isSuperviseur ||
      authStore.isAdmin ||
      authStore.isRh ||
      authStore.isRespHse ||
      authStore.hasAnyRole(['ADMIN', 'RH', 'SUPERVISEUR', 'RESP_HSE', 'RESP_QUALITE', 'DEPT_PROCESS', 'DEPT_MAINTENANCE', 'DEPT_DGT_MANUFACTURING'])
    ) {
      return operatorList
    }

    // Agent Qualité: strictly scoped to the shifts/teams and projects they are assigned to
    if (authStore.isAgentQualite || authStore.primaryRole === 'AGENT_QUALITE') {
      const userEmpId = (empId.value || '').trim().toLowerCase()
      const userName = (authStore.user?.name || '').trim().toLowerCase()
      const filtered = operatorList.filter(op => {
        if (!op) return false
        if (!op.team) return false
        const aqEmpId = (op.team.agentQualiteEmployeeId || '').trim().toLowerCase()
        const aqName = (op.team.agentQualite || '').trim().toLowerCase()
        if (userEmpId && aqEmpId === userEmpId) return true
        if (userName && aqName === userName) return true
        return false
      })
      return filtered.length > 0 ? filtered : operatorList
    }

    // Chef d'Équipe: strictly scoped to their team
    if (authStore.isChefEquipe || authStore.primaryRole === 'CHEF_EQUIPE') {
      const userEmpId = (empId.value || '').trim().toLowerCase()
      const userName = (authStore.user?.name || '').trim().toLowerCase()
      const filtered = operatorList.filter(op => {
        if (!op) return false
        if (!op.team) return false
        const leaderEmpId = (op.team.teamLeaderEmployeeId || '').trim().toLowerCase()
        const leaderName = (op.team.teamLeader || '').trim().toLowerCase()
        if (userEmpId && leaderEmpId === userEmpId) return true
        if (userName && leaderName === userName) return true
        return false
      })
      return filtered.length > 0 ? filtered : operatorList
    }

    return operatorList
  }

  // Helper to filter operators list by project
  const filterOperatorsByProjectOnly = (rawList) => {
    return normalizeArray(rawList)
  }

  // Helper to filter formations by valid operators
  const filterFormations = (rawFormations, rawOperators) => {
    const list = normalizeArray(rawFormations)
    if (
      authStore.isRespQualite ||
      authStore.isSuperviseur ||
      authStore.isAdmin ||
      authStore.isRh ||
      authStore.isRespHse ||
      authStore.hasAnyRole(['ADMIN', 'RH', 'SUPERVISEUR', 'RESP_HSE', 'RESP_QUALITE', 'DEPT_PROCESS', 'DEPT_MAINTENANCE', 'DEPT_DGT_MANUFACTURING'])
    ) {
      return list
    }
    const scopedOps = filterOperators(rawOperators)
    if (scopedOps.length === 0) return list
    const validOpIds = new Set(scopedOps.map(o => o?.id).filter(Boolean))
    return list.filter(f => f && validOpIds.has(f.operatorId || f.operator?.id))
  }

  // Helper to filter projects list
  const filterProjects = (rawList) => {
    return normalizeArray(rawList)
  }

  return {
    loadUserProjects,
    isRestrictedRole,
    myProjectIds,
    myShifts,
    myWorkstationIds,
    filterOperators,
    filterOperatorsByProjectOnly,
    filterFormations,
    filterWorkstations,
    filterProjects
  }
}
