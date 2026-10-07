import type { ButtonHTMLAttributes, ReactNode } from 'react'
import { ChevronDown, Download, Search, SlidersHorizontal } from 'lucide-react'
import { cn } from '../../utils/cn'
import type { StatusTone } from '../../types'

export function PageHeader({ eyebrow, title, description, action }: { eyebrow?: string; title: string; description?: string; action?: ReactNode }) {
  return <section className="page-header"><div><div className="eyebrow">{eyebrow}</div><h1>{title}</h1>{description && <p>{description}</p>}</div>{action && <div className="page-action">{action}</div>}</section>
}

export function Button({ children, className, variant = 'primary', ...props }: ButtonHTMLAttributes<HTMLButtonElement> & { variant?: 'primary' | 'secondary' | 'ghost' }) {
  return <button className={cn('button', `button-${variant}`, className)} {...props}>{children}</button>
}

export function SearchInput({ placeholder = 'Pesquisar...' }: { placeholder?: string }) {
  return <label className="search-input"><Search size={18}/><input aria-label={placeholder} placeholder={placeholder} /></label>
}

export function FilterButton({ children = 'Filtros' }: { children?: ReactNode }) { return <Button variant="secondary"><SlidersHorizontal size={17}/>{children}</Button> }
export function ExportButton() { return <Button variant="secondary"><Download size={17}/>Exportar</Button> }

export function EmptyState({ icon, title, description, action }: { icon: ReactNode; title: string; description?: string; action?: ReactNode }) {
  return <div className="empty-state"><div className="empty-icon">{icon}</div><h3>{title}</h3>{description && <p>{description}</p>}{action && <div className="empty-action">{action}</div>}</div>
}

export function StatusBadge({ children, tone = 'neutral' }: { children: ReactNode; tone?: StatusTone }) { return <span className={`status status-${tone}`}>{children}</span> }

export function DataTable({ columns, children, empty }: { columns: string[]; children?: ReactNode; empty: ReactNode }) {
  return <div className="table-wrap"><table><thead><tr>{columns.map(column => <th key={column}>{column}</th>)}</tr></thead><tbody>{children || <tr><td colSpan={columns.length}>{empty}</td></tr>}</tbody></table></div>
}

export function ChartCard({ title, subtitle, children }: { title: string; subtitle?: string; children: ReactNode }) { return <section className="card chart-card"><div className="card-heading"><div><h2>{title}</h2>{subtitle && <p>{subtitle}</p>}</div><button className="icon-button" aria-label="Mais opções"><ChevronDown size={18}/></button></div>{children}</section> }

export function FormSection({ title, description, children }: { title: string; description?: string; children: ReactNode }) { return <section className="form-section"><div className="form-section-intro"><h2>{title}</h2>{description && <p>{description}</p>}</div><div className="form-grid">{children}</div></section> }

export function Field({ label, required, children, wide = false }: { label: string; required?: boolean; children?: ReactNode; wide?: boolean }) { return <label className={cn('field', wide && 'field-wide')}><span>{label}{required && <b> *</b>}</span>{children || <input />}</label> }

export function SelectField({ label, options, wide = false }: { label: string; options: string[]; wide?: boolean }) { return <label className={cn('field', wide && 'field-wide')}><span>{label}</span><select defaultValue=""><option value="" disabled>Selecionar</option>{options.map(option => <option key={option}>{option}</option>)}</select></label> }

export function Breadcrumb({ items }: { items: string[] }) { return <nav className="breadcrumb" aria-label="Caminho da página">{items.map((item, index) => <span key={item}>{index > 0 && <i>/</i>}{item}</span>)}</nav> }

export function Pagination() { return <div className="pagination"><span>Mostrando 0 de 0 registros</span><div><button disabled>Anterior</button><button disabled>Próximo</button></div></div> }

export function Modal({ title, children }: { title: string; children: ReactNode }) { return <section className="modal-surface" role="dialog" aria-modal="true" aria-label={title}><header><h2>{title}</h2></header>{children}</section> }

export function ConfirmDialog({ title, description, children }: { title: string; description: string; children?: ReactNode }) { return <Modal title={title}><p className="dialog-description">{description}</p>{children && <div className="dialog-actions">{children}</div>}</Modal> }

export function DropdownMenu({ label, children }: { label: string; children: ReactNode }) { return <details className="dropdown-menu"><summary>{label}<ChevronDown size={15}/></summary><div>{children}</div></details> }
