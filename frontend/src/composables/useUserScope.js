import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { structureApi } from '@/api/endpoints'

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

  // Helper to filter operators list strictly by user assigned projects
  const filterOperators = (operatorList) => {
    if (!isRestrictedRole.value) return operatorList || []
    if (authStore.primaryRole === 'CHEF_EQUIPE') {
      return (operatorList || []).filter(op => {
        return op.team && op.team.teamLeaderEmployeeId === empId.value
      })
    }
    if (myProjectIds.value.size === 0) return [] // 0 assigned projects = 0 operators!

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
    filterFormations,
    filterWorkstations,
    filterProjects
  }
}
