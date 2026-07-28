const jsonHeaders = (token) => ({
  'Content-Type': 'application/json',
  Authorization: `Bearer ${token}`,
})

async function request(url, token, options = {}) {
  const response = await fetch(url, {
    ...options,
    headers: (options.method && options.method !== 'GET') ? jsonHeaders(token) : { Authorization: `Bearer ${token}` },
  })

  const data = await response.json().catch(() => ({}))
  if (!response.ok) {
    throw new Error(data.message || 'Une erreur est survenue.')
  }
  return data
}

export function fetchAllOperators(token) {
  return request('/api/operateurs', token)
}

export function fetchTeamOperators(token) {
  return request('/api/operateurs/mon-equipe', token)
}

export function fetchMyTeams(token) {
  return request('/api/operateurs/mes-equipes', token)
}

export function fetchAllTeams(token) {
  return request('/api/operateurs/equipes', token)
}

export function createOperator(token, operatorData) {
  return request('/api/operateurs', token, {
    method: 'POST',
    body: JSON.stringify(operatorData),
  })
}

export function assignPoste(token, matricule, posteId) {
  return request(`/api/operateurs/${matricule}/affecter-poste`, token, {
    method: 'PUT',
    body: JSON.stringify({ posteId }),
  })
}

export function updateOperatorStatus(token, matricule, statut) {
  return request(`/api/operateurs/${matricule}/statut`, token, {
    method: 'PUT',
    body: JSON.stringify({ statut }),
  })
}

export function markOperatorAbsence(token, matricule, motif) {
  return request(`/api/operateurs/${matricule}/absence`, token, {
    method: 'PUT',
    body: JSON.stringify({ motif }),
  })
}

export function markOperatorReprise(token, matricule) {
  return request(`/api/operateurs/${matricule}/reprise`, token, {
    method: 'PUT',
  })
}

export function markOperatorDepart(token, matricule) {
  return request(`/api/operateurs/${matricule}/depart`, token, {
    method: 'PUT',
  })
}