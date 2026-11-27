import type { PageResult, Student, StudentDetail, StudentInput, StudentStatus, StudentSummary, StudentUpdateInput } from '../types'
import { endpoints } from './endpoints'
import { request } from './http'

const studentPath = (id: string) => `${endpoints.students}/${encodeURIComponent(id)}`

export const studentService = {
  summary: () => request<StudentSummary>(`${endpoints.students}/resumo`),
  list: (params: Record<string, string | number | undefined> = {}) => {
    const query = new URLSearchParams(Object.entries(params).filter(([, value]) => value !== undefined).map(([key, value]) => [key, String(value)]))
    return request<PageResult<Student>>(`${endpoints.students}${query.size ? `?${query}` : ''}`)
  },
  getById: (id: string) => request<StudentDetail>(studentPath(id)),
  create: (input: StudentInput) => request<StudentDetail>(endpoints.students, { method: 'POST', body: input }),
  update: (id: string, input: StudentUpdateInput) => request<StudentDetail>(studentPath(id), { method: 'PATCH', body: input }),
  updateStatus: (id: string, status: StudentStatus) => request<StudentDetail>(`${studentPath(id)}/situacao`, { method: 'PATCH', body: { status } }),
  delete: (id: string) => request<void>(studentPath(id), { method: 'DELETE' }),
}
