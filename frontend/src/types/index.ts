export type NavigationItem = { label: string; href: string; icon: React.ComponentType<{ size?: number; strokeWidth?: number }> }
export type StatusTone = 'success' | 'warning' | 'danger' | 'neutral' | 'info'

export type PageResult<T> = {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export type StudentStatus = 'ATIVO' | 'INATIVO'
export type AssetCondition = 'NOVO' | 'BOM' | 'REGULAR' | 'NECESSITA_MANUTENCAO' | 'DANIFICADO' | 'INUTILIZADO'
export type AssetStatus = 'DISPONIVEL' | 'EM_USO' | 'EMPRESTADO' | 'EM_MANUTENCAO' | 'BAIXADO'
export type TransactionType = 'ENTRADA' | 'SAIDA'
export type TransactionStatus = 'PENDENTE' | 'CONFIRMADA' | 'CANCELADA'

export type Student = {
  id: string
  code: string
  fullName: string
  birthDate: string
  phone?: string
  whatsapp?: string
  email?: string
  guardianName?: string
  modality?: string
  status: StudentStatus
  createdAt: string
}

export type StudentInput = Omit<Student, 'id' | 'code' | 'createdAt'> & {
  cpf?: string
  rg?: string
  address?: string
  neighborhood?: string
  city?: string
  zipCode?: string
  guardianCpf?: string
  guardianPhone?: string
  guardianWhatsapp?: string
  relationship?: string
  level?: string
  classGroup?: string
  startDate?: string
  notes?: string
  importantInformation?: string
}

export type Asset = {
  id: string
  code: string
  name: string
  category: string
  quantity: number
  unit: string
  location: string
  condition: AssetCondition
  status: AssetStatus
  acquisitionDate?: string
  acquisitionValue?: number
  supplier?: string
  notes?: string
}

export type AssetInput = Omit<Asset, 'id'> & { description?: string; photo?: File }

export type FinancialTransaction = {
  id: string
  date: string
  description: string
  category?: string
  type: TransactionType
  paymentMethod?: string
  value: number
  status: TransactionStatus
  responsible?: string
  notes?: string
}

export type FinancialTransactionInput = Omit<FinancialTransaction, 'id' | 'status'> & { attachment?: File }

export type DashboardSummary = {
  students: { total: number; active: number; inactive: number; new: number }
  assets: { total: number; available: number; inUse: number; maintenance: number; damaged: number }
  financial: { income: number; expenses: number; balance: number }
}
