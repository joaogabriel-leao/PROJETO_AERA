import { type ReactNode } from 'react'
import { Bell, CircleHelp, ChevronLeft, LogOut, Menu, Moon, Search, Settings, Sun } from 'lucide-react'
import { NavLink, useLocation } from 'react-router-dom'
import logo from '../../assets/aera-logo.png'
import { cn } from '../../utils/cn'
import type { NavigationItem } from '../../types'

const titles: Record<string, string> = {
  '/': 'Visão geral', '/alunos': 'Alunos', '/alunos/novo': 'Cadastrar aluno', '/alunos/aluno': 'Detalhes do aluno',
  '/patrimonio': 'Patrimônio', '/patrimonio/novo': 'Cadastrar item', '/patrimonio/item': 'Detalhes do patrimônio',
  '/financeiro': 'Financeiro', '/financeiro/nova-movimentacao': 'Nova movimentação', '/relatorios': 'Relatórios', '/configuracoes': 'Configurações',
}

function Sidebar({ items, collapsed, mobileOpen, close }: { items: NavigationItem[]; collapsed: boolean; mobileOpen: boolean; close: () => void }) {
  return <><div className={cn('sidebar-backdrop', mobileOpen && 'is-open')} onClick={close}/><aside className={cn('sidebar', collapsed && 'is-collapsed', mobileOpen && 'is-open')}>
    <div className="brand"><img src={logo} alt="AERA"/><div><strong>AERA</strong><span>Gestão</span></div></div>
    <div className="sidebar-label">Menu principal</div>
    <nav className="sidebar-nav">{items.map(({ href, label, icon: Icon }) => <NavLink key={href} to={href} end={href === '/'} onClick={close} title={collapsed ? label : undefined}><Icon size={20}/><span>{label}</span></NavLink>)}</nav>
    <div className="sidebar-bottom"><a href="#ajuda"><CircleHelp size={20}/><span>Ajuda</span></a><button className="profile-mini"><span className="avatar">AR</span><span><strong>Perfil</strong><small>Administrador</small></span><Settings size={17}/></button><button className="logout"><LogOut size={20}/><span>Sair</span></button></div>
  </aside></>
}

export function AppLayout({ children, items, collapsed, onToggle, mobileOpen, onMobileToggle, theme, onThemeToggle }: { children: ReactNode; items: NavigationItem[]; collapsed: boolean; onToggle: () => void; mobileOpen: boolean; onMobileToggle: () => void; theme: 'light' | 'dark'; onThemeToggle: () => void }) {
  const location = useLocation()
  const canonicalPath = location.pathname.replace(/\/(aluno|item)\/[^/]+$/, '/$1')
  const title = titles[canonicalPath] || titles[location.pathname] || 'AERA Gestão'
  return <div className={cn('app-shell', collapsed && 'sidebar-collapsed')}><Sidebar items={items} collapsed={collapsed} mobileOpen={mobileOpen} close={() => mobileOpen && onMobileToggle()}/>
    <main className="main-content"><header className="topbar"><div className="topbar-left"><button className="icon-button desktop-toggle" aria-label="Recolher menu" onClick={onToggle}><ChevronLeft size={20}/></button><button className="icon-button mobile-toggle" aria-label="Abrir menu" onClick={onMobileToggle}><Menu size={22}/></button><div className="topbar-title"><span>AERA Gestão</span><strong>{title}</strong></div></div><div className="topbar-actions"><label className="top-search"><Search size={18}/><input placeholder="Buscar no sistema" /></label><button className="theme-toggle" onClick={onThemeToggle} aria-label={theme === 'light' ? 'Ativar modo escuro' : 'Ativar modo claro'} title={theme === 'light' ? 'Ativar modo escuro' : 'Ativar modo claro'}><Sun size={16} className={theme === 'light' ? 'is-active' : ''}/><Moon size={16} className={theme === 'dark' ? 'is-active' : ''}/><span>{theme === 'light' ? 'Claro' : 'Escuro'}</span></button><button className="icon-button notification" aria-label="Notificações"><Bell size={20}/><i/></button><button className="user-button"><span className="avatar">AR</span><span><strong>Administrador</strong><small>AERA Escolinha</small></span></button></div></header><div className="content-container">{children}</div></main>
  </div>
}
