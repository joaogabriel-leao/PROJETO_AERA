import { Archive, ChevronDown, Eye, ImagePlus, PackagePlus, Pencil, Plus, Save, Search, Trash2, Wrench, X, AlertCircle } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { Breadcrumb, Button, DataTable, EmptyState, Field, FormSection, PageHeader, StatusBadge } from '../../components/common'
import { StatCard } from '../../components/dashboard/StatCard'
import { assetService, ApiError } from '../../services'
import type { Asset, AssetCondition, AssetInput, AssetStatus, AssetSummary, StatusTone } from '../../types'

// --- Mapas de valor do enum (backend) -> rotulo legivel (PT-BR) para exibicao ---
const CONDITION_LABELS: Record<AssetCondition, string> = {
  NOVO: 'Novo',
  BOM: 'Bom',
  REGULAR: 'Regular',
  NECESSITA_MANUTENCAO: 'Necessita manutenção',
  DANIFICADO: 'Danificado',
  INUTILIZADO: 'Inutilizado',
}

const STATUS_LABELS: Record<AssetStatus, string> = {
  DISPONIVEL: 'Disponível',
  EM_USO: 'Em uso',
  EMPRESTADO: 'Emprestado',
  EM_MANUTENCAO: 'Em manutenção',
  BAIXADO: 'Baixado',
}

// Cor (tom) do selo de situacao, reaproveitando o componente StatusBadge.
const STATUS_TONES: Record<AssetStatus, StatusTone> = {
  DISPONIVEL: 'success',
  EM_USO: 'info',
  EMPRESTADO: 'warning',
  EM_MANUTENCAO: 'warning',
  BAIXADO: 'danger',
}

export function AssetsList() {
  const [assets, setAssets] = useState<Asset[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  // Filtros e paginacao controlados.
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState<'' | AssetStatus>('')
  const [conditionFilter, setConditionFilter] = useState<'' | AssetCondition>('')
  const [categoryFilter, setCategoryFilter] = useState('')
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)

  // Resumo real (do backend) para os cards de estatistica.
  const [summary, setSummary] = useState<AssetSummary | null>(null)

  const size = 20

  // Busca os itens no backend com os filtros atuais.
  const fetchAssets = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const result = await assetService.list({
        search: search.trim() || undefined,
        status: statusFilter || undefined,
        condition: conditionFilter || undefined,
        category: categoryFilter || undefined,
        page,
        size,
      })
      setAssets(result.content)
      setTotalPages(result.totalPages)
      setTotalElements(result.totalElements)
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Não foi possível carregar os itens.'
      setError(message)
      setAssets([])
    } finally {
      setLoading(false)
    }
  }, [search, statusFilter, conditionFilter, categoryFilter, page])

  // Busca o resumo real (totais globais, nao so da pagina atual).
  const fetchSummary = useCallback(async () => {
    try {
      setSummary(await assetService.summary())
    } catch {
      setSummary(null)
    }
  }, [])

  // Recarrega itens quando filtro/pagina mudam; e o resumo junto.
  useEffect(() => {
    fetchAssets()
  }, [fetchAssets])

  useEffect(() => {
    fetchSummary()
  }, [fetchSummary])

  // Estatisticas dos cards: usam o resumo REAL do backend (totais globais).
  // Enquanto o resumo nao chega, mostra 0.
  const stats = {
    total: summary?.total ?? totalElements,
    available: summary?.available ?? 0,
    inUse: summary?.inUse ?? 0,
    maintenance: summary?.maintenance ?? 0,
    damaged: summary?.damaged ?? 0,
  }

  // Dispara a busca ao enviar o formulario de pesquisa, voltando para a pagina 0.
  function handleSearchSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setPage(0)
    fetchAssets()
  }

  return (
    <>
      <PageHeader
        title="Patrimônio"
        description="Controle equipamentos e bens da AERA."
        action={<Link className="button button-primary" to="/patrimonio/novo"><Plus size={18} />Cadastrar item</Link>}
      />

      <section className="stats-grid asset-stats">
        <StatCard label="Total de itens" value={String(stats.total)} icon={<Archive size={22} />} />
        <StatCard label="Disponíveis" value={String(stats.available)} icon={<PackagePlus size={22} />} accent="teal" />
        <StatCard label="Em uso" value={String(stats.inUse)} icon={<Wrench size={22} />} accent="orange" />
        <StatCard label="Em manutenção" value={String(stats.maintenance)} icon={<Wrench size={22} />} accent="navy" />
        <StatCard label="Danificados" value={String(stats.damaged)} icon={<Wrench size={22} />} accent="navy" />
      </section>

      <section className="card list-card">
        <div className="toolbar">
          <form className="search-input" onSubmit={handleSearchSubmit} style={{ display: 'flex', alignItems: 'center' }}>
            <Search size={18} />
            <input
              aria-label="Buscar por código ou nome do item"
              placeholder="Buscar por código ou nome do item"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </form>
          <div className="toolbar-filters">
            <label className="compact-select">
              <select value={categoryFilter} onChange={(e) => { setCategoryFilter(e.target.value); setPage(0) }}>
                <option value="">Categoria (todas)</option>
                {CATEGORY_OPTIONS.map((option) => (
                  <option key={option} value={option}>{option}</option>
                ))}
              </select>
              <ChevronDown size={15} />
            </label>
            <label className="compact-select">
              <select value={statusFilter} onChange={(e) => { setStatusFilter(e.target.value as '' | AssetStatus); setPage(0) }}>
                <option value="">Situação (todas)</option>
                {(Object.keys(STATUS_LABELS) as AssetStatus[]).map((value) => (
                  <option key={value} value={value}>{STATUS_LABELS[value]}</option>
                ))}
              </select>
              <ChevronDown size={15} />
            </label>
            <label className="compact-select">
              <select value={conditionFilter} onChange={(e) => { setConditionFilter(e.target.value as '' | AssetCondition); setPage(0) }}>
                <option value="">Estado (todos)</option>
                {(Object.keys(CONDITION_LABELS) as AssetCondition[]).map((value) => (
                  <option key={value} value={value}>{CONDITION_LABELS[value]}</option>
                ))}
              </select>
              <ChevronDown size={15} />
            </label>
          </div>
        </div>

        <DataTable
          columns={['Código', 'Item', 'Categoria', 'Quantidade', 'Localização', 'Estado', 'Situação', 'Ações']}
          empty={
            loading ? (
              <EmptyState icon={<Archive size={25} />} title="Carregando itens..." />
            ) : error ? (
              <EmptyState icon={<Archive size={25} />} title="Erro ao carregar." description={error} action={<Button variant="secondary" onClick={() => fetchAssets()}>Tentar novamente</Button>} />
            ) : (
              <EmptyState icon={<Archive size={25} />} title="Nenhum item cadastrado." description="Cadastre o primeiro item para acompanhar o patrimônio." action={<Link className="button button-primary" to="/patrimonio/novo"><Plus size={17} />Cadastrar primeiro item</Link>} />
            )
          }
        >
          {!loading && !error && assets.length > 0
            ? assets.map((asset) => (
                <tr key={asset.id}>
                  <td>{asset.code}</td>
                  <td>{asset.name}</td>
                  <td>{asset.category}</td>
                  <td>{asset.quantity} {asset.unit}</td>
                  <td>{asset.location}</td>
                  <td>{CONDITION_LABELS[asset.condition]}</td>
                  <td><StatusBadge tone={STATUS_TONES[asset.status]}>{STATUS_LABELS[asset.status]}</StatusBadge></td>
                  <td>
                    <Link className="icon-button" to={`/patrimonio/${asset.id}`} aria-label="Ver detalhes">
                      <Eye size={18} />
                    </Link>
                  </td>
                </tr>
              ))
            : null}
        </DataTable>

        <div className="pagination">
          <span>
            {totalElements === 0
              ? 'Mostrando 0 registros'
              : `Página ${page + 1} de ${Math.max(totalPages, 1)} — ${totalElements} registro(s)`}
          </span>
          <div>
            <button disabled={page <= 0 || loading} onClick={() => setPage((p) => Math.max(0, p - 1))}>Anterior</button>
            <button disabled={page >= totalPages - 1 || loading} onClick={() => setPage((p) => p + 1)}>Próximo</button>
          </div>
        </div>
      </section>
    </>
  )
}

// --- Mapas de rotulo (PT-BR exibido na tela) -> valor do enum esperado pelo backend ---
const CONDITION_OPTIONS: { label: string; value: AssetCondition }[] = [
  { label: 'Novo', value: 'NOVO' },
  { label: 'Bom', value: 'BOM' },
  { label: 'Regular', value: 'REGULAR' },
  { label: 'Necessita manutenção', value: 'NECESSITA_MANUTENCAO' },
  { label: 'Danificado', value: 'DANIFICADO' },
  { label: 'Inutilizado', value: 'INUTILIZADO' },
]

const STATUS_OPTIONS: { label: string; value: AssetStatus }[] = [
  { label: 'Disponível', value: 'DISPONIVEL' },
  { label: 'Em uso', value: 'EM_USO' },
  { label: 'Emprestado', value: 'EMPRESTADO' },
  { label: 'Em manutenção', value: 'EM_MANUTENCAO' },
  { label: 'Baixado', value: 'BAIXADO' },
]

const CATEGORY_OPTIONS = ['Equipamento esportivo', 'Móvel', 'Eletrônico', 'Ferramenta', 'Outro']
const UNIT_OPTIONS = ['Unidade', 'Kit', 'Par', 'Conjunto']

// Estado inicial do formulario.
const initialForm = {
  code: '',
  name: '',
  category: CATEGORY_OPTIONS[0],
  description: '',
  quantity: '1',
  unit: UNIT_OPTIONS[0],
  location: '',
  acquisitionDate: '',
  condition: 'NOVO' as AssetCondition,
  status: 'DISPONIVEL' as AssetStatus,
  acquisitionValue: '',
  supplier: '',
  notes: '',
}

export function AssetForm() {
  const navigate = useNavigate()
  const [form, setForm] = useState(initialForm)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  // Atualiza um campo do formulario de forma generica.
  function updateField<K extends keyof typeof initialForm>(key: K, value: (typeof initialForm)[K]) {
    setForm((prev) => ({ ...prev, [key]: value }))
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)

    // Validacao dos campos obrigatorios ANTES de enviar ao backend.
    // Monta uma lista com o que estiver faltando para exibir uma mensagem clara.
    const faltando: string[] = []
    if (!form.name.trim()) faltando.push('Nome do item')
    if (!form.location.trim()) faltando.push('Localização')
    const quantidade = Number(form.quantity)
    if (form.quantity === '' || Number.isNaN(quantidade) || quantidade < 0) faltando.push('Quantidade (número igual ou maior que zero)')

    if (faltando.length > 0) {
      setError(`Não foi possível cadastrar. Preencha corretamente: ${faltando.join(', ')}.`)
      return // interrompe: nao envia ao backend
    }

    setSaving(true)

    // Monta o payload no formato que o backend espera (AssetInput).
    // Campos opcionais vazios viram undefined para nao enviar strings vazias.
    const payload: AssetInput = {
      code: form.code.trim(),
      name: form.name.trim(),
      category: form.category,
      quantity: quantidade,
      unit: form.unit,
      location: form.location.trim(),
      condition: form.condition,
      status: form.status,
      acquisitionDate: form.acquisitionDate || undefined,
      acquisitionValue: form.acquisitionValue ? Number(form.acquisitionValue) : undefined,
      supplier: form.supplier.trim() || undefined,
      notes: form.notes.trim() || undefined,
      description: form.description.trim() || undefined,
    }

    try {
      await assetService.create(payload)
      // Deu certo: volta para a lista de patrimonio.
      navigate('/patrimonio')
    } catch (err) {
      // Mostra a mensagem amigavel vinda do backend (campo "message").
      const message = err instanceof ApiError ? err.message : 'Não foi possível salvar o item. Tente novamente.'
      setError(message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <>
      <Breadcrumb items={['Patrimônio', 'Cadastrar item']} />
      <PageHeader title="Cadastrar item" description="Adicione um equipamento ou bem ao patrimônio da AERA." />
      <form onSubmit={handleSubmit} className="form-card">
        <FormSection title="Identificação" description="Informações básicas para identificar o item.">
          <Field label="Código do patrimônio">
            <input
              placeholder="Ex.: PAT-0001 (Se deixar vazio o código sera geradoa automaticamente)"
              value={form.code}
              onChange={(e) => updateField('code', e.target.value)}
            />
          </Field>
          <Field label="Nome do item" required>
            <input
              value={form.name}
              onChange={(e) => updateField('name', e.target.value)}
            />
          </Field>
          <label className="field">
            <span>Categoria</span>
            <select value={form.category} onChange={(e) => updateField('category', e.target.value)}>
              {CATEGORY_OPTIONS.map((option) => (
                <option key={option} value={option}>{option}</option>
              ))}
            </select>
          </label>
          <Field label="Descrição" wide>
            <textarea
              rows={4}
              placeholder="Descreva o item, suas características ou especificações."
              value={form.description}
              onChange={(e) => updateField('description', e.target.value)}
            />
          </Field>
        </FormSection>

        <FormSection title="Controle" description="Informações para acompanhamento do bem.">
          <Field label="Quantidade" required>
            <input
              type="number"
              min="0"
              value={form.quantity}
              onChange={(e) => updateField('quantity', e.target.value)}
            />
          </Field>
          <label className="field">
            <span>Unidade</span>
            <select value={form.unit} onChange={(e) => updateField('unit', e.target.value)}>
              {UNIT_OPTIONS.map((option) => (
                <option key={option} value={option}>{option}</option>
              ))}
            </select>
          </label>
          <Field label="Localização" required>
            <input
              value={form.location}
              onChange={(e) => updateField('location', e.target.value)}
            />
          </Field>
          <Field label="Data de aquisição">
            <input
              type="date"
              value={form.acquisitionDate}
              onChange={(e) => updateField('acquisitionDate', e.target.value)}
            />
          </Field>
        </FormSection>

        <FormSection title="Estado e situação">
          <label className="field">
            <span>Estado de conservação</span>
            <select value={form.condition} onChange={(e) => updateField('condition', e.target.value as AssetCondition)}>
              {CONDITION_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>{option.label}</option>
              ))}
            </select>
          </label>
          <label className="field">
            <span>Situação</span>
            <select value={form.status} onChange={(e) => updateField('status', e.target.value as AssetStatus)}>
              {STATUS_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>{option.label}</option>
              ))}
            </select>
          </label>
        </FormSection>

        <FormSection title="Informações adicionais">
          <Field label="Valor de aquisição">
            <input
              type="number"
              min="0"
              step="0.01"
              placeholder="0,00"
              value={form.acquisitionValue}
              onChange={(e) => updateField('acquisitionValue', e.target.value)}
            />
          </Field>
          <Field label="Fornecedor">
            <input
              value={form.supplier}
              onChange={(e) => updateField('supplier', e.target.value)}
            />
          </Field>
          <Field label="Observações" wide>
            <textarea
              rows={4}
              placeholder="Inclua observações relevantes sobre o item."
              value={form.notes}
              onChange={(e) => updateField('notes', e.target.value)}
            />
          </Field>
          {/* Upload de foto em stand-by: campo visual, ainda nao enviado ao backend. */}
          <label className="upload-box field-wide">
            <ImagePlus size={24} />
            <strong>Adicionar foto do item</strong>
            <span>Em breve — o envio de imagem ainda não está disponível.</span>
            <input type="file" accept="image/*" disabled />
          </label>
        </FormSection>

        {error && (
          <div
            role="alert"
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 10,
              margin: '4px 27px 20px',
              padding: '12px 16px',
              background: '#fdecea',
              border: '1px solid #f5b7b1',
              borderRadius: 10,
              color: '#a93226',
              fontWeight: 600,
              fontSize: 14,
            }}
          >
            <AlertCircle size={20} />
            <span>{error}</span>
          </div>
        )}

        <div className="form-actions">
          <Link className="button button-secondary" to="/patrimonio">Cancelar</Link>
          <Button type="submit" disabled={saving}>
            <PackagePlus size={18} />
            {saving ? 'Salvando...' : 'Salvar item'}
          </Button>
        </div>
      </form>
    </>
  )
}

export function AssetDetails() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()

  const [asset, setAsset] = useState<Asset | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  // Modos da tela e estados das acoes.
  const [editing, setEditing] = useState(false)
  const [editForm, setEditForm] = useState<Partial<AssetInput>>({})
  const [saving, setSaving] = useState(false)
  const [actionError, setActionError] = useState<string | null>(null)

  const [changingStatus, setChangingStatus] = useState(false)
  const [newStatus, setNewStatus] = useState<AssetStatus>('DISPONIVEL')

  const [confirmingDelete, setConfirmingDelete] = useState(false)
  const [deleting, setDeleting] = useState(false)

  // Busca o item pelo id da URL.
  const fetchAsset = useCallback(async () => {
    if (!id) return
    setLoading(true)
    setError(null)
    try {
      const data = await assetService.getById(id)
      setAsset(data)
      setNewStatus(data.status)
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Não foi possível carregar o item.'
      setError(message)
      setAsset(null)
    } finally {
      setLoading(false)
    }
  }, [id])

  useEffect(() => {
    fetchAsset()
  }, [fetchAsset])

  // Abre o modo de edicao pre-preenchendo o formulario com os dados atuais.
  function startEditing() {
    if (!asset) return
    setActionError(null)
    setEditForm({
      name: asset.name,
      category: asset.category,
      quantity: asset.quantity,
      unit: asset.unit,
      location: asset.location,
      condition: asset.condition,
      status: asset.status,
      acquisitionValue: asset.acquisitionValue,
      supplier: asset.supplier,
      notes: asset.notes,
    })
    setEditing(true)
  }

  function updateEditField<K extends keyof AssetInput>(key: K, value: AssetInput[K]) {
    setEditForm((prev) => ({ ...prev, [key]: value }))
  }

  // Salva a edicao (PATCH).
  async function handleEditSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!id) return
    setActionError(null)
    setSaving(true)
    try {
      const updated = await assetService.update(id, editForm)
      setAsset(updated)
      setEditing(false)
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Não foi possível salvar as alterações.'
      setActionError(message)
    } finally {
      setSaving(false)
    }
  }

  // Altera apenas a situacao (PATCH so com o campo status).
  async function handleStatusChange() {
    if (!id) return
    setActionError(null)
    setSaving(true)
    try {
      const updated = await assetService.update(id, { status: newStatus })
      setAsset(updated)
      setChangingStatus(false)
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Não foi possível alterar a situação.'
      setActionError(message)
    } finally {
      setSaving(false)
    }
  }

  // Exclui o item e volta para a lista.
  async function handleDelete() {
    if (!id) return
    setActionError(null)
    setDeleting(true)
    try {
      await assetService.delete(id)
      navigate('/patrimonio')
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Não foi possível excluir o item.'
      setActionError(message)
      setDeleting(false)
      setConfirmingDelete(false)
    }
  }

  if (loading) {
    return (
      <>
        <Breadcrumb items={['Patrimônio', 'Detalhes do item']} />
        <EmptyState icon={<Archive size={25} />} title="Carregando item..." />
      </>
    )
  }

  if (error || !asset) {
    return (
      <>
        <Breadcrumb items={['Patrimônio', 'Detalhes do item']} />
        <EmptyState
          icon={<Archive size={25} />}
          title="Item não encontrado."
          description={error ?? 'O item solicitado não existe ou foi removido.'}
          action={<Link className="button button-primary" to="/patrimonio">Voltar para a lista</Link>}
        />
      </>
    )
  }

  return (
    <>
      <Breadcrumb items={['Patrimônio', asset.name]} />

      <section className="detail-head">
        <div className="student-avatar-large"><Archive size={27} /></div>
        <div>
          <span className="eyebrow">Patrimônio · {asset.code}</span>
          <h1>{asset.name}</h1>
          <p><StatusBadge tone={STATUS_TONES[asset.status]}>{STATUS_LABELS[asset.status]}</StatusBadge></p>
        </div>
        {!editing && (
          <div className="detail-actions">
            <Button variant="secondary" onClick={() => { setChangingStatus(true); setNewStatus(asset.status) }}>
              <ChevronDown size={18} />Alterar situação
            </Button>
            <Button variant="secondary" onClick={startEditing}>
              <Pencil size={18} />Editar item
            </Button>
            <Button variant="ghost" onClick={() => setConfirmingDelete(true)}>
              <Trash2 size={18} />Excluir
            </Button>
          </div>
        )}
      </section>

      {actionError && <p role="alert" style={{ color: '#c0392b', margin: '0 0 1rem' }}>{actionError}</p>}

      {/* Dialogo: alterar situacao */}
      {changingStatus && (
        <section className="card detail-card" style={{ marginBottom: '1rem' }}>
          <h2>Alterar situação</h2>
          <label className="field">
            <span>Nova situação</span>
            <select value={newStatus} onChange={(e) => setNewStatus(e.target.value as AssetStatus)}>
              {(Object.keys(STATUS_LABELS) as AssetStatus[]).map((value) => (
                <option key={value} value={value}>{STATUS_LABELS[value]}</option>
              ))}
            </select>
          </label>
          <div className="form-actions">
            <Button variant="secondary" onClick={() => setChangingStatus(false)} disabled={saving}><X size={18} />Cancelar</Button>
            <Button onClick={handleStatusChange} disabled={saving}><Save size={18} />{saving ? 'Salvando...' : 'Salvar situação'}</Button>
          </div>
        </section>
      )}

      {/* Dialogo: confirmar exclusao */}
      {confirmingDelete && (
        <section className="card detail-card" style={{ marginBottom: '1rem' }}>
          <h2>Excluir item</h2>
          <p>Tem certeza que deseja excluir <strong>{asset.name}</strong> ({asset.code})? Esta ação não pode ser desfeita.</p>
          <div className="form-actions">
            <Button variant="secondary" onClick={() => setConfirmingDelete(false)} disabled={deleting}><X size={18} />Cancelar</Button>
            <Button variant="ghost" onClick={handleDelete} disabled={deleting}><Trash2 size={18} />{deleting ? 'Excluindo...' : 'Confirmar exclusão'}</Button>
          </div>
        </section>
      )}

      {editing ? (
        /* MODO EDICAO */
        <form onSubmit={handleEditSubmit} className="form-card">
          <FormSection title="Editar item" description="Altere as informações e salve.">
            <Field label="Nome do item" required>
              <input required value={editForm.name ?? ''} onChange={(e) => updateEditField('name', e.target.value)} />
            </Field>
            <label className="field">
              <span>Categoria</span>
              <select value={editForm.category ?? ''} onChange={(e) => updateEditField('category', e.target.value)}>
                {CATEGORY_OPTIONS.map((option) => (<option key={option} value={option}>{option}</option>))}
              </select>
            </label>
            <Field label="Quantidade" required>
              <input type="number" min="0" required value={editForm.quantity ?? 0} onChange={(e) => updateEditField('quantity', Number(e.target.value))} />
            </Field>
            <label className="field">
              <span>Unidade</span>
              <select value={editForm.unit ?? ''} onChange={(e) => updateEditField('unit', e.target.value)}>
                {UNIT_OPTIONS.map((option) => (<option key={option} value={option}>{option}</option>))}
              </select>
            </label>
            <Field label="Localização" required>
              <input required value={editForm.location ?? ''} onChange={(e) => updateEditField('location', e.target.value)} />
            </Field>
            <label className="field">
              <span>Estado de conservação</span>
              <select value={editForm.condition ?? 'NOVO'} onChange={(e) => updateEditField('condition', e.target.value as AssetCondition)}>
                {(Object.keys(CONDITION_LABELS) as AssetCondition[]).map((value) => (<option key={value} value={value}>{CONDITION_LABELS[value]}</option>))}
              </select>
            </label>
            <Field label="Valor de aquisição">
              <input type="number" min="0" step="0.01" value={editForm.acquisitionValue ?? ''} onChange={(e) => updateEditField('acquisitionValue', e.target.value ? Number(e.target.value) : undefined)} />
            </Field>
            <Field label="Fornecedor">
              <input value={editForm.supplier ?? ''} onChange={(e) => updateEditField('supplier', e.target.value)} />
            </Field>
            <Field label="Observações" wide>
              <textarea rows={4} value={editForm.notes ?? ''} onChange={(e) => updateEditField('notes', e.target.value)} />
            </Field>
          </FormSection>
          <div className="form-actions">
            <Button type="button" variant="secondary" onClick={() => setEditing(false)} disabled={saving}><X size={18} />Cancelar</Button>
            <Button type="submit" disabled={saving}><Save size={18} />{saving ? 'Salvando...' : 'Salvar alterações'}</Button>
          </div>
        </form>
      ) : (
        /* MODO VISUALIZACAO */
        <section className="detail-grid">
          <section className="card detail-card">
            <h2>Informações do item</h2>
            <dl className="detail-list">
              <div><dt>Código</dt><dd>{asset.code}</dd></div>
              <div><dt>Categoria</dt><dd>{asset.category}</dd></div>
              <div><dt>Quantidade</dt><dd>{asset.quantity} {asset.unit}</dd></div>
              <div><dt>Localização</dt><dd>{asset.location}</dd></div>
              <div><dt>Estado de conservação</dt><dd>{CONDITION_LABELS[asset.condition]}</dd></div>
              <div><dt>Situação</dt><dd><StatusBadge tone={STATUS_TONES[asset.status]}>{STATUS_LABELS[asset.status]}</StatusBadge></dd></div>
              <div><dt>Data de aquisição</dt><dd>{asset.acquisitionDate ?? '—'}</dd></div>
              <div><dt>Valor de aquisição</dt><dd>{asset.acquisitionValue != null ? asset.acquisitionValue.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' }) : '—'}</dd></div>
              <div><dt>Fornecedor</dt><dd>{asset.supplier ?? '—'}</dd></div>
              <div className="detail-list-wide"><dt>Observações</dt><dd>{asset.notes ?? '—'}</dd></div>
            </dl>
          </section>
          <section className="card detail-card">
            <h2>Histórico do item</h2>
            <EmptyState icon={<Wrench size={25} />} title="Nenhum evento registrado." description="Manutenções, empréstimos, devoluções e baixas serão exibidos aqui." />
          </section>
        </section>
      )}
    </>
  )
}
