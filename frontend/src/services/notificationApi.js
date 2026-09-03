import api from '@/api/index'

export const notificationApi = {
  getForUser: () => api.get('/notifications'),
  getUnreadCount: () => api.get('/notifications/unread-count'),
  markAsRead: (id) => api.put(`/notifications/${id}/read`),
  markAllAsRead: () => api.put('/notifications/read-all'),
  delete: (id) => api.delete(`/notifications/${id}`),
  clearAll: () => api.delete('/notifications/clear-all'),
  sendTestEmail: (email) => api.post('/notifications/test-email', null, { params: { email } }),
}
