import type { Asset, AssetInput, AssetSummary, PageResult } from '../types'
import { endpoints } from './endpoints'
import { request } from './http'

export const assetService = {
  summary: () => request<AssetSummary>(`${endpoints.assets}/resumo`),
  list: (params: Record<string, string | number | undefined> = {}) => {
    const query = new URLSearchParams(Object.entries(params).filter(([, value]) => value !== undefined).map(([key, value]) => [key, String(value)]))
    return request<PageResult<Asset>>(`${endpoints.assets}${query.size ? `?${query}` : ''}`)
  },
  getById: (id: string) => request<Asset>(`${endpoints.assets}/${encodeURIComponent(id)}`),
  create: ({ photo, ...input }: AssetInput) => {
    if (!photo) return request<Asset>(endpoints.assets, { method: 'POST', body: input })
    const formData = new FormData()
    formData.append('data', new Blob([JSON.stringify(input)], { type: 'application/json' }))
    formData.append('photo', photo)
    return request<Asset>(endpoints.assets, { method: 'POST', body: formData })
  },
  update: (id: string, input: Partial<AssetInput>) => request<Asset>(`${endpoints.assets}/${encodeURIComponent(id)}`, { method: 'PATCH', body: input }),
  delete: (id: string) => request<void>(`${endpoints.assets}/${encodeURIComponent(id)}`, { method: 'DELETE' }),
}
