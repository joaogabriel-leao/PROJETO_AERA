export class ApiError extends Error {
  constructor(public status: number, message: string, public details?: unknown) {
    super(message)
    this.name = 'ApiError'
  }
}

const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || '/api').replace(/\/$/, '')

type RequestOptions = Omit<RequestInit, 'body'> & { body?: unknown }

export async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { body, headers, ...init } = options
  const isFormData = body instanceof FormData
  const response = await fetch(`${apiBaseUrl}${path}`, {
    ...init,
    headers: { Accept: 'application/json', ...(isFormData ? {} : { 'Content-Type': 'application/json' }), ...headers },
    body: body === undefined ? undefined : isFormData ? body : JSON.stringify(body),
  })

  if (response.status === 204) return undefined as T

  const contentType = response.headers.get('content-type') || ''
  const payload = contentType.includes('application/json') ? await response.json() : await response.text()

  if (!response.ok) {
    const message = typeof payload === 'object' && payload && 'message' in payload ? String(payload.message) : 'Não foi possível concluir a solicitação.'
    throw new ApiError(response.status, message, payload)
  }

  return payload as T
}
