import type { PageResult, Student, StudentInput } from '../types'
import { endpoints } from './endpoints'
import { request } from './http'

export const studentService = {
  list: (params: Record<string, string | number | undefined> = {}) => {
    const query = new URLSearchParams(Object.entries(params).filter(([, value]) => value !== undefined).map(([key, value]) => [key, String(value)]))
    return request<PageResult<Student>>(`${endpoints.students}${query.size ? `?${query}` : ''}`)
  },
  getById: (id: string) => request<Student>(`${endpoints.students}/${encodeURIComponent(id)}`),
  create: (input: StudentInput) => request<Student>(endpoints.students, { method: 'POST', body: input }),
  update: (id: string, input: Partial<StudentInput>) => request<Student>(`${endpoints.students}/${encodeURIComponent(id)}`, { method: 'PATCH', body: input }),
  updateStatus: (id: string, status: Student['status']) => request<Student>(`${endpoints.students}/${encodeURIComponent(id)}/situacao`, { method: 'PATCH', body: { status } }),
}
