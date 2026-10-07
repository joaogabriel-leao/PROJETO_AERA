import type { DashboardSummary } from '../types'
import { endpoints } from './endpoints'
import { request } from './http'

export const dashboardService = {
  getSummary: () => request<DashboardSummary>(endpoints.dashboard),
}
