import { Archive, ArrowDownRight, ArrowUpRight, GraduationCap, Landmark, WalletCards } from 'lucide-react'
import { ChartCard, EmptyState, PageHeader } from '../../components/common'
import { StatCard } from '../../components/dashboard/StatCard'

const EmptyChart = ({ label }: { label: string }) => <EmptyState icon={<div className="chart-placeholder"><i/><i/><i/><i/><i/></div>} title={label} description="Os dados estarão disponíveis após o início dos registros." />

export default function Dashboard() {
  return <><PageHeader eyebrow="AERA Gestão" title="Visão geral" description="Acompanhe a organização da AERA em um só lugar."/>
    <section className="stats-grid"><StatCard label="Alunos cadastrados" value="--" icon={<GraduationCap size={22}/>}/><StatCard label="Itens no patrimônio" value="--" icon={<Archive size={22}/>} accent="teal"/><StatCard label="Receitas" value="R$ --" icon={<ArrowUpRight size={22}/>} accent="orange"/><StatCard label="Despesas" value="R$ --" icon={<ArrowDownRight size={22}/>} accent="navy"/><StatCard label="Saldo" value="R$ --" icon={<WalletCards size={22}/>}/></section>
    <section className="dashboard-grid"><ChartCard title="Resumo financeiro" subtitle="Receitas x despesas"><EmptyChart label="Nenhuma movimentação financeira registrada."/></ChartCard><section className="card heritage-card"><div className="card-heading"><div><h2>Situação do patrimônio</h2><p>Acompanhe seus itens quando houver registros.</p></div><Landmark size={21}/></div><div className="heritage-list">{['Disponíveis', 'Em uso', 'Em manutenção', 'Danificados'].map((item, index) => <div key={item}><span className={`dot dot-${index}`}/><p>{item}</p><strong>--</strong></div>)}</div></section></section>
    <section className="dashboard-grid bottom-grid"><section className="card"><div className="card-heading"><div><h2>Movimentações recentes</h2><p>Últimas entradas e saídas registradas.</p></div></div><EmptyState icon={<WalletCards size={24}/>} title="Nenhuma movimentação registrada." description="As movimentações financeiras aparecerão aqui."/></section><section className="card students-summary"><div className="card-heading"><div><h2>Alunos</h2><p>Resumo do cadastro de alunos.</p></div><GraduationCap size={21}/></div><div className="student-metrics">{['Total', 'Ativos', 'Inativos', 'Novos alunos'].map(item => <div key={item}><span>{item}</span><strong>--</strong></div>)}</div><p className="muted-note">Os indicadores serão atualizados conforme os alunos forem cadastrados.</p></section></section>
  </>
}
