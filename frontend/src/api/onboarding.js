import api from './index'

const API_URL = '/onboarding'

export default {
  seedModules() {
    return api.post(`${API_URL}/seed`)
  },
  getAllModules() {
    return api.get(`${API_URL}/modules`)
  },
  getOperatorStatus(operatorId) {
    return api.get(`${API_URL}/operators/${operatorId}/status`)
  },
  validateModule(operatorId, moduleId, data) {
    return api.put(`${API_URL}/operators/${operatorId}/modules/${moduleId}`, data)
  },
  getOperatorProgress(operatorId) {
    return api.get(`${API_URL}/operators/${operatorId}/progress`)
  },
  isOperatorComplete(operatorId) {
    return api.get(`${API_URL}/operators/${operatorId}/complete`)
  },
  batchCheckComplete(operatorIds) {
    return api.post(`${API_URL}/operators/batch-complete`, operatorIds)
  },
  getOperatorsSummary() {
    return api.get(`${API_URL}/operators-summary`)
  },
  getHistory() {
    return api.get(`${API_URL}/history`)
  },
}