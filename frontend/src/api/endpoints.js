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
  getFormationDetail: (id) => api.get(`/training/formations/${id}`),
  createFormations: (workstationId, operatorIds) => api.post('/training/formations', null, { params: { workstationId, operatorIds: operatorIds.join(',') } }),
  getAvailableStructure: () => api.get('/training/available-structure'),
  getTracking: (formationId) => api.get(`/training/formations/${formationId}/tracking`),
  addTracking: (formationId, data) => api.post(`/training/formations/${formationId}/tracking`, data),
  batchSave: (formationId, days) => api.post(`/training/formations/${formationId}/batch-save`, { days }),
  saveDailyBatch: (entries) => api.post('/training/daily-batch', { entries }),
  autoEvaluate: (formationId) => api.post(`/training/formations/${formationId}/auto-evaluate`),
  getChartData: (formationId) => api.get(`/training/formations/${formationId}/chart-data`),
  resetFormation: (formationId) => api.post(`/training/formations/${formationId}/reset`),
  setQualityObjective: (formationId, qualityObjective) => api.put(`/training/formations/${formationId}/quality-objective`, { qualityObjective }),
  setWorkstationQualityObjective: (workstationId, qualityObjective) => api.put(`/training/workstations/${workstationId}/quality-objective`, { qualityObjective }),
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
}

export const evaluationApi = {
  // Templates
  getTemplates: () => api.get('/evaluation/templates'),
  getTemplateDetail: (id) => api.get(`/evaluation/templates/${id}`),
  createTemplate: (data) => api.post('/evaluation/templates', data),
  validateTemplate: (id) => api.post(`/evaluation/templates/${id}/validate`),

  // Sections
  addSection: (templateId, data) => api.post(`/evaluation/templates/${templateId}/sections`, data),

  // Questions
  addQuestion: (templateId, data) => api.post(`/evaluation/templates/${templateId}/questions`, data),
  updateQuestion: (questionId, data) => api.put(`/evaluation/questions/${questionId}`, data),
  deleteQuestion: (questionId, templateId) => api.delete(`/evaluation/questions/${questionId}`, { params: { templateId } }),

  // Question Validation (Responsable)
  getPendingQuestions: () => api.get('/evaluation/questions/pending'),
  validateQuestion: (id) => api.post(`/evaluation/questions/${id}/validate`),
  rejectQuestion: (id, reason) => api.post(`/evaluation/questions/${id}/reject`, { reason }),

  // Sessions
  startEvaluation: (data) => api.post('/evaluation/sessions/start', data),
  submitAnswers: (sessionId, answers) => api.post(`/evaluation/sessions/${sessionId}/answers`, { answers }),
  completeEvaluation: (sessionId) => api.post(`/evaluation/sessions/${sessionId}/complete`),
  getSessionDetail: (id) => api.get(`/evaluation/sessions/${id}`),

  // Auto-trigger: operators who passed 12j suivi
  getPendingForOperator: (operatorId) => api.get(`/evaluation/pending/operator/${operatorId}`),
  getAllPendingEvaluations: () => api.get('/evaluation/pending/all'),
    resolveTemplates: (operatorId, formationId) => api.get('/evaluation/initial/resolve-templates', { params: { operatorId, formationId } }),

  // Polyvalence Matrix
  getMatrix: () => api.get('/evaluation/matrix'),
  getDoubleFailures: () => api.get('/evaluation/double-failures'),

    // Evaluation History
  getHistory: () => api.get('/evaluation/history'),
}