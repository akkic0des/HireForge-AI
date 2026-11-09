const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'
const TOKEN_KEY = 'hireforge.token'
const USER_KEY = 'hireforge.user'

export class ApiRequestError extends Error {
  constructor(message, status, retryAfter) {
    super(message)
    this.name = 'ApiRequestError'
    this.status = status
    this.retryAfter = retryAfter
  }
}

export const authStore = {
  token: () => localStorage.getItem(TOKEN_KEY),
  user: () => { try { return JSON.parse(localStorage.getItem(USER_KEY) || 'null') } catch { return null } },
  save: (data) => { localStorage.setItem(TOKEN_KEY, data.token); localStorage.setItem(USER_KEY, JSON.stringify(data)) },
  clear: () => { localStorage.removeItem(TOKEN_KEY); localStorage.removeItem(USER_KEY) },
}

export async function apiRequest(path, options = {}) {
  const headers = new Headers(options.headers || {})
  if (options.body && !(options.body instanceof FormData)) headers.set('Content-Type', 'application/json')
  const token = authStore.token()
  if (token) headers.set('Authorization', `Bearer ${token}`)
  let response
  try { response = await fetch(`${API_BASE}${path}`, { ...options, headers }) }
  catch { throw new Error('Cannot reach HireForge. Check that the backend is running.') }
  if (!response.ok) {
    let message = `Request failed (${response.status})`
    try { const body = await response.json(); message = body.message || body.error || message } catch { /* response has no JSON body */ }
    if (response.status === 401 && token) {
      authStore.clear()
      window.dispatchEvent(new Event('hireforge:unauthorized'))
    }
    throw new ApiRequestError(message, response.status, response.headers.get('Retry-After'))
  }
  if (response.status === 204) return null
  return response.json()
}
