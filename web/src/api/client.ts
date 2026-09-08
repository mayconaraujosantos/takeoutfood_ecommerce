import type { ApiResponse } from './types'

// Every service in this fleet sits behind the api-gateway path convention
// /<service-id>/api/v1/... (Spring Cloud Gateway's discovery-locator, lower-cased service id,
// no prefix stripping) -- callers pass that full path, e.g. "/auth-service/api/v1/auth/login".
const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? 'http://ifood.local'

export class ApiError extends Error {
  status: number

  constructor(message: string, status: number) {
    super(message)
    this.status = status
  }
}

function authHeader(): HeadersInit {
  const token = localStorage.getItem('accessToken')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const res = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...authHeader(),
      ...options.headers,
    },
  })

  const text = await res.text()
  const body = (text ? JSON.parse(text) : undefined) as ApiResponse<T> | undefined

  if (!res.ok || body?.success === false) {
    throw new ApiError(body?.error ?? body?.message ?? res.statusText, res.status)
  }

  return body?.data as T
}

export const api = {
  get: <T>(path: string) => request<T>(path),
  post: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: 'POST', body: body !== undefined ? JSON.stringify(body) : undefined }),
  patch: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: 'PATCH', body: body !== undefined ? JSON.stringify(body) : undefined }),
}
