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
    // Only super admins, HR and other global departments are completely unrestricted by default
    return !authStore.hasAnyRole(['ADMIN', 'RH', 'DEPT_PROCESS', 'DEPT_MAINTENANCE', 'DEPT_DGT_MANUFACTURING'])
  })

  // Set of project IDs current user is assigned to
  const myProjectIds = computed(() => {
    if (!empId.value) return new Set()
    const set = new Set()
    for (const p of userProjects.value) {
      if (p.members?.some(m => m.employeeId === empId.value)) {
        set.add(p.id)
      }
    }
    return set
  })

  // Set of shifts assigned to current user (for CHEF_EQUIPE)
  const myShifts = computed(() => {
    if (!empId.value) return new Set()
    const set = new Set()
    for (const p of userProjects.value) {
      for (const m of (p.members || [])) {
        if (m.employeeId === empId.value && m.shift) {
          set.add(m.shift)
        }
      }
    }
    return set
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

  // Helper to filter workstations list strictly by user assigned projects
  const filterWorkstations = (workstationList) => {
    if (!isRestrictedRole.value || authStore.primaryRole === 'CHEF_EQUIPE') return workstationList || []
    if (myProjectIds.value.size === 0) return []
    return (workstationList || []).filter(ws => myWorkstationIds.value.has(ws.id))
  }

  // Helper to filter operators list strictly by user assigned projects/shifts
  const filterOperators = (operatorList) => {
    // 1. Chef d'Équipe -> restricted to their own team
    if (authStore.primaryRole === 'CHEF_EQUIPE') {
      const chefName = authStore.user?.name
      return (operatorList || []).filter(op => {
        if (!op.team) {
          return op.project?.id && myProjectIds.value.has(op.project.id)
        }
        return (
          op.team.teamLeaderEmployeeId === empId.value ||
          (chefName && op.team.teamLeader && op.team.teamLeader.trim() === chefName.trim()) ||
          (op.project?.id && myProjectIds.value.has(op.project.id))
        )
      })
    }

    // 2. Check dynamic shift configuration from database project member mappings
    const myChefIds = []
    for (const p of userProjects.value) {
      const myRecords = p.members?.filter(m => m.employeeId === empId.value) || []
      const myProjectShifts = new Set(myRecords.map(m => m.shift).filter(Boolean))
      if (myProjectShifts.size > 0) {
        const projectChefs = p.members?.filter(m => m.projectRole === 'TEAM_LEADER' && myProjectShifts.has(m.shift)) || []
        for (const chef of projectChefs) {
          if (chef.employeeId) {
            myChefIds.push(chef.employeeId)
          }
        }
      }
    }

    // Fallback: Check if the user is registered in the static SHIFT_TEAMS_CONFIG
    if (myChefIds.length === 0) {
      for (const [chefId, members] of Object.entries(SHIFT_TEAMS_CONFIG)) {
        if (members.includes(empId.value)) {
          myChefIds.push(chefId)
        }
      }
    }

    // If found in the configuration (dynamic or static), restrict them to those shifts
    if (myChefIds.length > 0) {
      return (operatorList || []).filter(op => op.team && myChefIds.includes(op.team.teamLeaderEmployeeId))
    }

    // 3. Fallback: if not registered in the configuration, allow global roles or fallback to project membership
    if (authStore.hasAnyRole(['ADMIN', 'RH', 'SUPERVISEUR', 'RESP_HSE', 'RESP_QUALITE', 'DEPT_PROCESS', 'DEPT_MAINTENANCE', 'DEPT_DGT_MANUFACTURING'])) {
      return operatorList || []
    }

    // 4. Fallback: filter by project membership
    if (myProjectIds.value.size === 0) return []
    return (operatorList || []).filter(op => {
      const opProjectId = op.project?.id || op.projectId
      return opProjectId && myProjectIds.value.has(opProjectId)
    })
  }

  // Helper to filter operators list strictly by user assigned projects (ignoring shift config)
  const filterOperatorsByProjectOnly = (operatorList) => {
    // If not restricted globally, return all
    if (!isRestrictedRole.value) return operatorList || []

    // If role is ADMIN, RH, SUPERVISEUR, RESP_HSE, RESP_QUALITE, DEPT_PROCESS, DEPT_MAINTENANCE, DEPT_DGT_MANUFACTURING
    if (authStore.hasAnyRole(['ADMIN', 'RH', 'SUPERVISEUR', 'RESP_HSE', 'RESP_QUALITE', 'DEPT_PROCESS', 'DEPT_MAINTENANCE', 'DEPT_DGT_MANUFACTURING'])) {
      return operatorList || []
    }

    // Otherwise fallback to project membership
    if (myProjectIds.value.size === 0) return []
    return (operatorList || []).filter(op => {
      const opProjectId = op.project?.id || op.projectId
      return opProjectId && myProjectIds.value.has(opProjectId)
    })
  }

  // Helper to filter formations by valid scoped operators
  const filterFormations = (formationList, operatorList) => {
    if (!isRestrictedRole.value) return formationList || []
    const validOpIds = new Set(filterOperators(operatorList).map(o => o.id))
    return (formationList || []).filter(f => {
      const fOpId = f.operator?.id || f.operatorId
      return fOpId && validOpIds.has(fOpId)
    })
  }

  // Helper to filter projects list strictly by user assigned projects
  const filterProjects = (projectList) => {
    if (!isRestrictedRole.value || authStore.primaryRole === 'CHEF_EQUIPE') return projectList || []
    if (myProjectIds.value.size === 0) return []
    return (projectList || []).filter(p => myProjectIds.value.has(p.id))
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
