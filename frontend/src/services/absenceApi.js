import api from '@/api/index'

export const absenceApi = {
  getAll: () => api.get('/absence'),
  getActive: () => api.get('/absence/active'),
  markAbsent: (data) => api.post('/absence/mark-absent', data),
  markReturn: (data) => api.post('/absence/mark-return', data),
  markDeparture: (data) => api.post('/absence/mark-departure', data),
  checkOperator: (operatorId) => api.get(`/absence/check/${operatorId}`),
}
