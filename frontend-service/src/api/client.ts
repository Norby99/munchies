// Axios instance shared by every api/*.ts module. The gateway wraps every
// response — success or error — in the same envelope: { result, code }
// (see ui_specification.md §3). This module unwraps it once so callers work
// with plain typed payloads, and normalizes failures into ApiError.
//
// The session cookie is httpOnly and Secure/SameSite=Lax with a 1h maxAge
// (gateway-service/src/main/ts/infrastructure/adapter/middleware/auth.ts) —
// the client never reads it, just sends it via withCredentials.

import axios, { type AxiosError, type AxiosInstance, type AxiosResponse } from 'axios'

import type { ApiEnvelope } from '@/types'

export class ApiError extends Error {
  readonly code: number

  constructor(message: string, code: number) {
    super(message)
    this.name = 'ApiError'
    this.code = code
  }
}

type UnauthorizedHandler = () => void

let unauthorizedHandler: UnauthorizedHandler | null = null

export function onUnauthorized(handler: UnauthorizedHandler): void {
  unauthorizedHandler = handler
}

export const http: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '',
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' },
})

http.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiEnvelope<string>>) => {
    const code = error.response?.data?.code ?? error.response?.status ?? 0
    const message = error.response?.data?.result ?? error.message

    if (code === 401) unauthorizedHandler?.()

    return Promise.reject(new ApiError(message, code))
  },
)

export async function unwrap<T>(response: Promise<AxiosResponse<ApiEnvelope<T>>>): Promise<T> {
  const { data } = await response
  return data.result
}
