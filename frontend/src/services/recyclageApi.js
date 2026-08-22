import api from '@/api/index'

export const recyclageApi = {
  getPlanning: (params) => api.get('/recyclage/planning', { params }),
  generateAnnual: (year) => api.post('/recyclage/planning/generate-annual', null, { params: { year } }),
  newHirePlanning: (operatorId) => api.post(`/recyclage/planning/new-hire/${operatorId}`),
  returnFromAbsence: (operatorId) => api.post(`/recyclage/planning/return-from-absence/${operatorId}`),
  createManual: (data) => api.post('/recyclage/planning/manual', data),
  startEvaluation: (id) => api.post(`/recyclage/planning/${id}/start-evaluation`),
  completePlanning: (id, data) => api.put(`/recyclage/planning/${id}/complete`, data),
  cancelPlanning: (id) => api.put(`/recyclage/planning/${id}/cancel`),
  getCalendar: (params) => api.get('/recyclage/calendar', { params }),
  getUpcoming: (params) => api.get('/recyclage/upcoming', { params }),
}
