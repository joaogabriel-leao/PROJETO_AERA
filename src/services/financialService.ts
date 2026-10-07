import type { FinancialTransaction, FinancialTransactionInput, PageResult } from '../types'
import { endpoints } from './endpoints'
import { request } from './http'

export const financialService = {
  getSummary: () => request<{ income: number; expenses: number; balance: number }>(`${endpoints.financial}/resumo`),
  listTransactions: (params: Record<string, string | number | undefined> = {}) => {
    const query = new URLSearchParams(Object.entries(params).filter(([, value]) => value !== undefined).map(([key, value]) => [key, String(value)]))
    return request<PageResult<FinancialTransaction>>(`${endpoints.financialTransactions}${query.size ? `?${query}` : ''}`)
  },
  createTransaction: ({ attachment, ...input }: FinancialTransactionInput) => {
    if (!attachment) return request<FinancialTransaction>(endpoints.financialTransactions, { method: 'POST', body: input })
    const formData = new FormData()
    formData.append('data', new Blob([JSON.stringify(input)], { type: 'application/json' }))
    formData.append('attachment', attachment)
    return request<FinancialTransaction>(endpoints.financialTransactions, { method: 'POST', body: formData })
  },
}
