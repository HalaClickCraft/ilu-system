import api from './index'

export const authApi = {
  login: (credentials) => api.post('/auth/login', credentials),
  changePassword: (data) => api.post('/auth/change-password', data),
}

export const usersApi = {
  getAll: () => api.get('/users'),
  create: (data) => api.post('/users', data),
  toggleStatus: (id) => api.put(`/users/${id}/toggle-status`),
  deleteUser: (id) => api.delete(`/users/${id}`),
}

export const operatorsApi = {
  getAll: () => api.get('/operators'),
  create: (data) => api.post('/operators', data),
  getById: (id) => api.get(`/operators/${id}`),
  update: (id, data) => api.put(`/operators/${id}`, data),
  getActive: () => api.get('/operators/active'),
  deactivate: (id) => api.put(`/operators/${id}/deactivate`),
  activate: (id) => api.put(`/operators/${id}/activate`),
  getFormations: (id) => api.get(`/operators/${id}/formations`),
  getAssignments: (id) => api.get(`/operators/${id}/assignments`),
}

export const trainingApi = {
  getFormations: () => api.get('/training/formations'),
  createFormation: (params) => api.post('/training/formations', null, { params }),
  getTracking: (formationId) => api.get(`/training/formations/${formationId}/tracking`),
  addTracking: (formationId, data) => api.post(`/training/formations/${formationId}/tracking`, data),
  addCadence: (formationId, data) => api.post(`/training/formations/${formationId}/tracking/cadence`, data),
  addDefauts: (formationId, data) => api.post(`/training/formations/${formationId}/tracking/defauts`, data),
  completeFormation: (formationId) => api.put(`/training/formations/${formationId}/complete`),
  assignOperator: (params) => api.post('/training/assignments', null, { params }),
  getStatistics: () => api.get('/training/statistics'),
}

export const structureApi = {
  getAll: () => api.get('/structure'),
  createProject: (data) => api.post('/structure/projects', data),
  getProject: (id) => api.get(`/structure/projects/${id}`),
  updateProject: (id, data) => api.put(`/structure/projects/${id}`, data),
  deleteProject: (id) => api.delete(`/structure/projects/${id}`),
  createZone: (projectId, data) => api.post(`/structure/projects/${projectId}/zones`, null, { params: { name: data.name } }),
  getZones: (projectId) => api.get(`/structure/projects/${projectId}/zones`),
  deleteZone: (id) => api.delete(`/structure/zones/${id}`),
  getWorkstations: () => api.get('/structure/workstations'),
  createWorkstation: (data) => api.post('/structure/workstations', data),
  updateWorkstation: (id, data) => api.put(`/structure/workstations/${id}`, data),
  deleteWorkstation: (id) => api.delete(`/structure/workstations/${id}`),
  addMember: (projectId, data) => api.post(`/structure/projects/${projectId}/members`, null, { params: data }),
  updateMember: (memberId, role) => api.put(`/structure/members/${memberId}`, null, { params: { role } }),
  deleteMember: (memberId) => api.delete(`/structure/members/${memberId}`),
    getAvailableUsers: () => api.get('/structure/users-available'),
  getTeams: () => api.get('/teams'),
}
