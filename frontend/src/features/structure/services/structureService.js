const jsonHeaders = (token) => ({
  'Content-Type': 'application/json',
  Authorization: `Bearer ${token}`,
})

async function request(url, token, options = {}) {
  const response = await fetch(url, {
    ...options,
    headers: options.method ? jsonHeaders(token) : { Authorization: `Bearer ${token}` },
  })

  const data = await response.json().catch(() => ({}))
  if (!response.ok) {
    throw new Error(data.message || 'Une erreur est survenue avec la structure.')
  }
  return data
}

export function fetchStructure(token) {
  return request('/api/structure', token)
}

export function createProject(token, nom, membres = []) {
  return request('/api/structure/projects', token, {
    method: 'POST',
    body: JSON.stringify({ nom, membres }),
  })
}

export function addProjectMember(token, projectId, userId, roleProjet) {
  return request(`/api/structure/projects/${projectId}/members`, token, {
    method: 'POST',
    body: JSON.stringify({ userId, roleProjet }),
  })
}

export function updateProjectMemberRole(token, projectId, memberId, userId, roleProjet) {
  return request(`/api/structure/projects/${projectId}/members/${memberId}`, token, {
    method: 'PUT',
    body: JSON.stringify({ userId, roleProjet }),
  })
}

export function deleteProjectMember(token, projectId, memberId) {
  return request(`/api/structure/projects/${projectId}/members/${memberId}`, token, {
    method: 'DELETE',
  })
}

export function createZone(token, projectId, nom) {
  return request('/api/structure/zones', token, {
    method: 'POST',
    body: JSON.stringify({ projectId, nom }),
  })
}

export function createPoste(token, zoneId, nom) {
  return request('/api/structure/postes', token, {
    method: 'POST',
    body: JSON.stringify({ zoneId, nom }),
  })
}
