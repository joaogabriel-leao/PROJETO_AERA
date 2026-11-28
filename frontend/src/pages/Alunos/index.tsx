import { GraduationCap, Eye, Pencil, Plus, Power, Save, Search, Trash2, UserRound, UserRoundPlus, X, ChevronDown } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import type { FormEvent, ReactNode } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { Breadcrumb, Button, DataTable, EmptyState, Field, FormSection, PageHeader, StatusBadge } from '../../components/common'
import { studentService, ApiError } from '../../services'
import type { Student, StudentDetail, StudentInput, StudentStatus, StudentUpdateInput } from '../../types'

const STATUS_LABELS: Record<StudentStatus, string> = { ATIVO: 'Ativo', INATIVO: 'Inativo' }
const SEX_OPTIONS = ['Feminino', 'Masculino', 'Prefiro não informar']
const RELATIONSHIP_OPTIONS = ['Mãe', 'Pai', 'Responsável legal', 'Outro']
const MODALITY_OPTIONS = ['Surf', 'Bodyboard', 'Stand-up paddle', 'Kitesurf']
const LEVEL_OPTIONS = ['Iniciante', 'Intermediário', 'Avançado']
const PAGE_SIZE = 20

// Campos de texto opcionais do cadastro (nomes iguais aos do backend).
const TEXT_FIELDS = [
  'sex', 'cpf', 'rg', 'address', 'neighborhood', 'city', 'zipCode', 'phone', 'whatsapp', 'email',
  'guardianName', 'guardianCpf', 'guardianPhone', 'guardianWhatsapp', 'relationship',
  'modality', 'level', 'classGroup', 'notes', 'importantInformation',
] as const
type TextField = (typeof TEXT_FIELDS)[number]

type FormState = Record<TextField, string> & { fullName: string; birthDate: string; startDate: string; status: StudentStatus }

const emptyForm: FormState = {
  fullName: '', birthDate: '', startDate: '', status: 'ATIVO',
  ...(Object.fromEntries(TEXT_FIELDS.map((field) => [field, ''])) as Record<TextField, string>),
}

const todayIso = () => new Date().toISOString().slice(0, 10)

// Datas civis chegam como yyyy-MM-dd; formatar por texto evita deslocamento de fuso.
function formatDate(value?: string | null) {
  if (!value) return '—'
  const [year, month, day] = value.slice(0, 10).split('-')
  return `${day}/${month}/${year}`
}

function calculateAge(birthDate: string) {
  const [year, month, day] = birthDate.split('-').map(Number)
  const now = new Date()
  let age = now.getFullYear() - year
  if (now.getMonth() + 1 < month || (now.getMonth() + 1 === month && now.getDate() < day)) age -= 1
  return age
}

const errorMessage = (err: unknown, fallback: string) => (err instanceof ApiError ? err.message : fallback)

function toForm(student: StudentDetail): FormState {
  const form: FormState = {
    ...emptyForm,
    fullName: student.fullName,
    birthDate: student.birthDate,
    startDate: student.startDate ?? '',
    status: student.status,
  }
  for (const field of TEXT_FIELDS) form[field] = student[field] ?? ''
  return form
}

function toCreatePayload(form: FormState): StudentInput {
  const payload: StudentInput = { fullName: form.fullName.trim(), birthDate: form.birthDate, status: form.status }
  if (form.startDate) payload.startDate = form.startDate
  for (const field of TEXT_FIELDS) {
    const value = form[field].trim()
    if (value) payload[field] = value
  }
  return payload
}

// No PATCH, texto vazio limpa o campo; datas precisam de clearFields.
function toUpdatePayload(form: FormState, original: StudentDetail): StudentUpdateInput {
  const payload: StudentUpdateInput = { fullName: form.fullName.trim(), birthDate: form.birthDate, status: form.status }
  for (const field of TEXT_FIELDS) payload[field] = form[field].trim()
  if (form.startDate) payload.startDate = form.startDate
  else if (original.startDate) payload.clearFields = ['startDate']
  return payload
}

function ErrorText({ children }: { children: ReactNode }) {
  return <p role="alert" style={{ color: '#c0392b', margin: '0 0 1rem' }}>{children}</p>
}

function DeleteCard({ student, deleting, onCancel, onConfirm }: { student: Pick<Student, 'fullName' | 'code'>; deleting: boolean; onCancel: () => void; onConfirm: () => void }) {
  return (
    <section className="card detail-card" style={{ marginBottom: '1rem', minHeight: 0 }}>
      <h2>Excluir aluno</h2>
      <p>Tem certeza que deseja excluir <strong>{student.fullName}</strong> ({student.code})? Esta ação não pode ser desfeita.</p>
      <div className="form-actions">
        <Button variant="secondary" onClick={onCancel} disabled={deleting}><X size={18} />Cancelar</Button>
        <Button variant="ghost" onClick={onConfirm} disabled={deleting}><Trash2 size={18} />{deleting ? 'Excluindo...' : 'Confirmar exclusão'}</Button>
      </div>
    </section>
  )
}

export function StudentsList() {
  const [students, setStudents] = useState<Student[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)

  const [searchInput, setSearchInput] = useState('')
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState<'' | StudentStatus>('')
  const [modalityFilter, setModalityFilter] = useState('')
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)

  const [toDelete, setToDelete] = useState<Student | null>(null)
  const [deleting, setDeleting] = useState(false)

  const fetchStudents = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const result = await studentService.list({
        search: search || undefined,
        status: statusFilter || undefined,
        modality: modalityFilter || undefined,
        page,
        size: PAGE_SIZE,
      })
      setStudents(result.content)
      setTotalPages(result.totalPages)
      setTotalElements(result.totalElements)
    } catch (err) {
      setError(errorMessage(err, 'Não foi possível carregar os alunos.'))
      setStudents([])
    } finally {
      setLoading(false)
    }
  }, [search, statusFilter, modalityFilter, page])

  useEffect(() => {
    fetchStudents()
  }, [fetchStudents])

  function handleSearchSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setPage(0)
    setSearch(searchInput.trim())
  }

  async function handleDelete() {
    if (!toDelete) return
    setDeleting(true)
    setActionError(null)
    try {
      await studentService.delete(toDelete.id)
      setToDelete(null)
      // Se era o ultimo item da pagina, volta uma pagina; senao recarrega a atual.
      if (students.length === 1 && page > 0) setPage(page - 1)
      else await fetchStudents()
    } catch (err) {
      setActionError(errorMessage(err, 'Não foi possível excluir o aluno.'))
      setToDelete(null)
    } finally {
      setDeleting(false)
    }
  }

  const newStudentLink = (label: string) => <Link className="button button-primary" to="/alunos/novo"><Plus size={17} />{label}</Link>
  const hasFilters = Boolean(search || statusFilter || modalityFilter)

  return (
    <>
      <PageHeader title="Alunos" description="Gerencie os alunos cadastrados na AERA." action={newStudentLink('Novo aluno')} />

      {actionError && <ErrorText>{actionError}</ErrorText>}
      {toDelete && <DeleteCard student={toDelete} deleting={deleting} onCancel={() => setToDelete(null)} onConfirm={handleDelete} />}

      <section className="card list-card">
        <div className="toolbar">
          <form className="search-input" onSubmit={handleSearchSubmit} style={{ display: 'flex', alignItems: 'center' }}>
            <Search size={18} />
            <input
              aria-label="Buscar por nome, código ou responsável"
              placeholder="Buscar por nome, código ou responsável"
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
            />
          </form>
          <div className="toolbar-filters">
            <label className="compact-select">
              <select aria-label="Situação" value={statusFilter} onChange={(e) => { setStatusFilter(e.target.value as '' | StudentStatus); setPage(0) }}>
                <option value="">Situação (todas)</option>
                {(Object.keys(STATUS_LABELS) as StudentStatus[]).map((value) => <option key={value} value={value}>{STATUS_LABELS[value]}</option>)}
              </select>
              <ChevronDown size={15} />
            </label>
            <label className="compact-select">
              <select aria-label="Modalidade" value={modalityFilter} onChange={(e) => { setModalityFilter(e.target.value); setPage(0) }}>
                <option value="">Modalidade (todas)</option>
                {MODALITY_OPTIONS.map((option) => <option key={option} value={option}>{option}</option>)}
              </select>
              <ChevronDown size={15} />
            </label>
          </div>
        </div>

        <DataTable
          columns={['Código', 'Nome', 'Idade', 'Responsável', 'Telefone', 'Modalidade', 'Situação', 'Cadastro', 'Ações']}
          empty={
            loading ? (
              <EmptyState icon={<GraduationCap size={25} />} title="Carregando alunos..." />
            ) : error ? (
              <EmptyState icon={<GraduationCap size={25} />} title="Erro ao carregar." description={error} action={<Button variant="secondary" onClick={() => fetchStudents()}>Tentar novamente</Button>} />
            ) : hasFilters ? (
              <EmptyState icon={<GraduationCap size={25} />} title="Nenhum aluno encontrado." description="Ajuste a busca ou os filtros." />
            ) : (
              <EmptyState icon={<GraduationCap size={25} />} title="Nenhum aluno cadastrado." description="Cadastre o primeiro aluno para começar a organizar as informações." action={newStudentLink('Cadastrar primeiro aluno')} />
            )
          }
        >
          {!loading && !error && students.length > 0
            ? students.map((student) => (
                <tr key={student.id}>
                  <td>{student.code}</td>
                  <td>{student.fullName}</td>
                  <td>{calculateAge(student.birthDate)}</td>
                  <td>{student.guardianName || '—'}</td>
                  <td>{student.phone || '—'}</td>
                  <td>{student.modality || '—'}</td>
                  <td><StatusBadge tone={student.status === 'ATIVO' ? 'success' : 'neutral'}>{STATUS_LABELS[student.status]}</StatusBadge></td>
                  <td>{formatDate(student.createdAt)}</td>
                  <td>
                    <Link className="icon-button" to={`/alunos/${student.id}`} aria-label={`Ver detalhes de ${student.fullName}`}><Eye size={18} /></Link>
                    <Link className="icon-button" to={`/alunos/${student.id}/editar`} aria-label={`Editar ${student.fullName}`}><Pencil size={18} /></Link>
                    <button className="icon-button" type="button" aria-label={`Excluir ${student.fullName}`} onClick={() => setToDelete(student)}><Trash2 size={18} /></button>
                  </td>
                </tr>
              ))
            : null}
        </DataTable>

        <div className="pagination">
          <span>{totalElements === 0 ? 'Mostrando 0 registros' : `Página ${page + 1} de ${Math.max(totalPages, 1)} — ${totalElements} registro(s)`}</span>
          <div>
            <button disabled={page <= 0 || loading} onClick={() => setPage((p) => Math.max(0, p - 1))}>Anterior</button>
            <button disabled={page >= totalPages - 1 || loading} onClick={() => setPage((p) => p + 1)}>Próximo</button>
          </div>
        </div>
      </section>
    </>
  )
}

// Select controlado que preserva valores legados fora da lista de opcoes.
function Choice({ label, options, value, onChange }: { label: string; options: string[]; value: string; onChange: (value: string) => void }) {
  const all = value && !options.includes(value) ? [...options, value] : options
  return (
    <Field label={label}>
      <select value={value} onChange={(e) => onChange(e.target.value)}>
        <option value="">Selecionar</option>
        {all.map((option) => <option key={option} value={option}>{option}</option>)}
      </select>
    </Field>
  )
}

// Cadastro (/alunos/novo) e edicao (/alunos/:id/editar) compartilham o mesmo formulario.
export function StudentForm() {
  const { id } = useParams<{ id: string }>()
  const isEdit = Boolean(id)
  const navigate = useNavigate()

  const [form, setForm] = useState<FormState>(emptyForm)
  const [original, setOriginal] = useState<StudentDetail | null>(null)
  const [loading, setLoading] = useState(isEdit)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!id) return
    let active = true
    studentService.getById(id)
      .then((student) => {
        if (!active) return
        setOriginal(student)
        setForm(toForm(student))
      })
      .catch((err) => active && setLoadError(errorMessage(err, 'Não foi possível carregar o aluno.')))
      .finally(() => active && setLoading(false))
    return () => { active = false }
  }, [id])

  const set = <K extends keyof FormState>(key: K, value: FormState[K]) => setForm((prev) => ({ ...prev, [key]: value }))
  const text = (key: TextField) => ({ value: form[key], onChange: (e: { target: { value: string } }) => set(key, e.target.value) })

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)

    const missing: string[] = []
    if (!form.fullName.trim()) missing.push('Nome completo')
    if (!form.birthDate) missing.push('Data de nascimento')
    if (missing.length > 0) {
      setError(`Preencha corretamente: ${missing.join(', ')}.`)
      return
    }
    if (form.birthDate > todayIso()) {
      setError('A data de nascimento não pode estar no futuro.')
      return
    }

    setSaving(true)
    try {
      const saved = isEdit && id && original
        ? await studentService.update(id, toUpdatePayload(form, original))
        : await studentService.create(toCreatePayload(form))
      navigate(`/alunos/${saved.id}`)
    } catch (err) {
      setError(errorMessage(err, isEdit ? 'Não foi possível salvar as alterações.' : 'Não foi possível cadastrar o aluno.'))
      setSaving(false)
    }
  }

  const title = isEdit ? 'Editar aluno' : 'Cadastrar aluno'

  if (loading) {
    return <><Breadcrumb items={['Alunos', title]} /><EmptyState icon={<UserRound size={25} />} title="Carregando aluno..." /></>
  }
  if (loadError) {
    return (
      <>
        <Breadcrumb items={['Alunos', title]} />
        <EmptyState icon={<UserRound size={25} />} title="Aluno não encontrado." description={loadError} action={<Link className="button button-primary" to="/alunos">Voltar para a lista</Link>} />
      </>
    )
  }

  return (
    <>
      <Breadcrumb items={['Alunos', title]} />
      <PageHeader title={title} description={isEdit ? 'Altere as informações e salve.' : 'Preencha as informações para criar um novo cadastro.'} />
      {error && <ErrorText>{error}</ErrorText>}
      <form onSubmit={handleSubmit} className="form-card">
        <FormSection title="Dados pessoais" description="Informações de identificação do aluno.">
          <Field label="Nome completo" required wide><input required maxLength={255} value={form.fullName} onChange={(e) => set('fullName', e.target.value)} /></Field>
          <Field label="Data de nascimento" required><input type="date" required max={todayIso()} value={form.birthDate} onChange={(e) => set('birthDate', e.target.value)} /></Field>
          <Choice label="Sexo" options={SEX_OPTIONS} value={form.sex} onChange={(v) => set('sex', v)} />
          <Field label="CPF"><input maxLength={40} placeholder="000.000.000-00" {...text('cpf')} /></Field>
          <Field label="RG"><input maxLength={40} {...text('rg')} /></Field>
          <Field label="Endereço" wide><input maxLength={255} {...text('address')} /></Field>
          <Field label="Bairro"><input maxLength={255} {...text('neighborhood')} /></Field>
          <Field label="Cidade"><input maxLength={255} {...text('city')} /></Field>
          <Field label="CEP"><input maxLength={20} placeholder="00000-000" {...text('zipCode')} /></Field>
        </FormSection>

        <FormSection title="Contato" description="Canais de contato do aluno.">
          <Field label="Telefone"><input type="tel" maxLength={30} placeholder="(00) 00000-0000" {...text('phone')} /></Field>
          <Field label="WhatsApp"><input type="tel" maxLength={30} placeholder="(00) 00000-0000" {...text('whatsapp')} /></Field>
          <Field label="E-mail" wide><input type="email" maxLength={255} placeholder="nome@email.com" {...text('email')} /></Field>
        </FormSection>

        <FormSection title="Responsável" description="Preencha quando o aluno for menor de idade.">
          <Field label="Nome do responsável" wide><input maxLength={255} {...text('guardianName')} /></Field>
          <Field label="CPF do responsável"><input maxLength={40} placeholder="000.000.000-00" {...text('guardianCpf')} /></Field>
          <Field label="Telefone"><input type="tel" maxLength={30} placeholder="(00) 00000-0000" {...text('guardianPhone')} /></Field>
          <Field label="WhatsApp"><input type="tel" maxLength={30} placeholder="(00) 00000-0000" {...text('guardianWhatsapp')} /></Field>
          <Choice label="Parentesco" options={RELATIONSHIP_OPTIONS} value={form.relationship} onChange={(v) => set('relationship', v)} />
        </FormSection>

        <FormSection title="Informações esportivas" description="Vínculo do aluno com as atividades da AERA.">
          <Choice label="Modalidade" options={MODALITY_OPTIONS} value={form.modality} onChange={(v) => set('modality', v)} />
          <Choice label="Nível" options={LEVEL_OPTIONS} value={form.level} onChange={(v) => set('level', v)} />
          <Field label="Turma"><input maxLength={100} {...text('classGroup')} /></Field>
          <Field label="Data de entrada"><input type="date" value={form.startDate} onChange={(e) => set('startDate', e.target.value)} /></Field>
          <Field label="Situação" required>
            <select value={form.status} onChange={(e) => set('status', e.target.value as StudentStatus)}>
              {(Object.keys(STATUS_LABELS) as StudentStatus[]).map((value) => <option key={value} value={value}>{STATUS_LABELS[value]}</option>)}
            </select>
          </Field>
        </FormSection>

        <FormSection title="Informações adicionais">
          <Field label="Observações" wide><textarea rows={4} maxLength={2000} placeholder="Informações relevantes para a equipe." {...text('notes')} /></Field>
          <Field label="Informações importantes" wide><textarea rows={4} maxLength={2000} placeholder="Cuidados, restrições ou informações adicionais." {...text('importantInformation')} /></Field>
        </FormSection>

        <div className="form-actions">
          <Link className="button button-secondary" to={isEdit && id ? `/alunos/${id}` : '/alunos'}>Cancelar</Link>
          <Button type="submit" disabled={saving}>
            {isEdit ? <Save size={18} /> : <UserRoundPlus size={18} />}
            {saving ? 'Salvando...' : isEdit ? 'Salvar alterações' : 'Salvar aluno'}
          </Button>
        </div>
      </form>
    </>
  )
}

function DetailList({ items }: { items: [string, ReactNode, boolean?][] }) {
  return (
    <dl className="detail-list">
      {items.map(([label, value, wide]) => (
        <div key={label} className={wide ? 'detail-list-wide' : undefined}>
          <dt>{label}</dt>
          <dd>{value || '—'}</dd>
        </div>
      ))}
    </dl>
  )
}

export function StudentDetails() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()

  const [student, setStudent] = useState<StudentDetail | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)
  const [changingStatus, setChangingStatus] = useState(false)
  const [confirmingDelete, setConfirmingDelete] = useState(false)
  const [deleting, setDeleting] = useState(false)

  useEffect(() => {
    if (!id) return
    let active = true
    studentService.getById(id)
      .then((data) => active && setStudent(data))
      .catch((err) => active && setError(errorMessage(err, 'Não foi possível carregar o aluno.')))
      .finally(() => active && setLoading(false))
    return () => { active = false }
  }, [id])

  async function handleToggleStatus() {
    if (!id || !student) return
    setActionError(null)
    setChangingStatus(true)
    try {
      setStudent(await studentService.updateStatus(id, student.status === 'ATIVO' ? 'INATIVO' : 'ATIVO'))
    } catch (err) {
      setActionError(errorMessage(err, 'Não foi possível alterar a situação.'))
    } finally {
      setChangingStatus(false)
    }
  }

  async function handleDelete() {
    if (!id) return
    setActionError(null)
    setDeleting(true)
    try {
      await studentService.delete(id)
      navigate('/alunos')
    } catch (err) {
      setActionError(errorMessage(err, 'Não foi possível excluir o aluno.'))
      setDeleting(false)
      setConfirmingDelete(false)
    }
  }

  if (loading) {
    return <><Breadcrumb items={['Alunos', 'Detalhes do aluno']} /><EmptyState icon={<UserRound size={25} />} title="Carregando aluno..." /></>
  }
  if (error || !student) {
    return (
      <>
        <Breadcrumb items={['Alunos', 'Detalhes do aluno']} />
        <EmptyState icon={<UserRound size={25} />} title="Aluno não encontrado." description={error ?? 'O aluno solicitado não existe ou foi removido.'} action={<Link className="button button-primary" to="/alunos">Voltar para a lista</Link>} />
      </>
    )
  }

  return (
    <>
      <Breadcrumb items={['Alunos', student.fullName]} />

      <section className="detail-head">
        <div className="student-avatar-large"><UserRound size={27} /></div>
        <div>
          <span className="eyebrow">Aluno · {student.code}</span>
          <h1>{student.fullName}</h1>
          <p><StatusBadge tone={student.status === 'ATIVO' ? 'success' : 'neutral'}>{STATUS_LABELS[student.status]}</StatusBadge></p>
        </div>
        <div className="detail-actions">
          <Button variant="secondary" onClick={handleToggleStatus} disabled={changingStatus}>
            <Power size={18} />{changingStatus ? 'Salvando...' : student.status === 'ATIVO' ? 'Inativar' : 'Reativar'}
          </Button>
          <Link className="button button-secondary" to={`/alunos/${student.id}/editar`}><Pencil size={18} />Editar</Link>
          <Button variant="ghost" onClick={() => setConfirmingDelete(true)}><Trash2 size={18} />Excluir</Button>
        </div>
      </section>

      {actionError && <ErrorText>{actionError}</ErrorText>}
      {confirmingDelete && <DeleteCard student={student} deleting={deleting} onCancel={() => setConfirmingDelete(false)} onConfirm={handleDelete} />}

      <section className="detail-grid">
        <section className="card detail-card">
          <h2>Dados gerais</h2>
          <DetailList items={[
            ['Data de nascimento', `${formatDate(student.birthDate)} (${calculateAge(student.birthDate)} anos)`],
            ['Sexo', student.sex],
            ['CPF', student.cpf],
            ['RG', student.rg],
            ['Endereço', student.address, true],
            ['Bairro', student.neighborhood],
            ['Cidade', student.city],
            ['CEP', student.zipCode],
            ['Telefone', student.phone],
            ['WhatsApp', student.whatsapp],
            ['E-mail', student.email, true],
          ]} />
        </section>

        <section className="card detail-card">
          <h2>Responsável</h2>
          <DetailList items={[
            ['Nome', student.guardianName, true],
            ['CPF', student.guardianCpf],
            ['Parentesco', student.relationship],
            ['Telefone', student.guardianPhone],
            ['WhatsApp', student.guardianWhatsapp],
          ]} />
        </section>

        <section className="card detail-card">
          <h2>Informações esportivas</h2>
          <DetailList items={[
            ['Modalidade', student.modality],
            ['Nível', student.level],
            ['Turma', student.classGroup],
            ['Data de entrada', formatDate(student.startDate)],
            ['Cadastrado em', formatDate(student.createdAt)],
          ]} />
        </section>

        <section className="card detail-card">
          <h2>Observações</h2>
          <DetailList items={[
            ['Observações', student.notes, true],
            ['Informações importantes', student.importantInformation, true],
          ]} />
        </section>
      </section>
    </>
  )
}
