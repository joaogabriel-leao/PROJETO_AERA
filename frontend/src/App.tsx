import {
  Archive,
  BarChart3,
  GraduationCap,
  LayoutDashboard,
  Settings,
} from "lucide-react";
import { Routes, Route } from "react-router-dom";
import { AppLayout } from "./components/layout/AppLayout";
import { useSidebar } from "./hooks/useSidebar";
import { useTheme } from "./hooks/useTheme";
import Dashboard from "./pages/Dashboard";
import { StudentsList, StudentForm, StudentDetails } from "./pages/Alunos";
import { AssetsList, AssetForm, AssetDetails } from "./pages/Patrimonio";
import { FinancialPage, TransactionForm } from "./pages/Financeiro";
import Reports from "./pages/Relatorios";
import SettingsPage from "./pages/Configuracoes";

const navigation = [
  { label: "Visão Geral", href: "/", icon: LayoutDashboard },
  { label: "Alunos", href: "/alunos", icon: GraduationCap },
  { label: "Patrimônio", href: "/patrimonio", icon: Archive },
  { label: "Financeiro", href: "/financeiro", icon: BarChart3 },
  { label: "Relatórios", href: "/relatorios", icon: BarChart3 },
  { label: "Configurações", href: "/configuracoes", icon: Settings },
];

export default function App() {
  const sidebar = useSidebar();
  const { theme, toggleTheme } = useTheme();
  return (
    <AppLayout
      items={navigation}
      collapsed={sidebar.collapsed}
      onToggle={() => sidebar.setCollapsed((value) => !value)}
      mobileOpen={sidebar.mobileOpen}
      onMobileToggle={() => sidebar.setMobileOpen((value) => !value)}
      theme={theme}
      onThemeToggle={toggleTheme}
    >
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/alunos" element={<StudentsList />} />
        <Route path="/alunos/novo" element={<StudentForm />} />
        <Route path="/alunos/:id" element={<StudentDetails />} />
        <Route path="/patrimonio" element={<AssetsList />} />
        <Route path="/patrimonio/novo" element={<AssetForm />} />
        <Route path="/patrimonio/:id" element={<AssetDetails />} />
        <Route path="/financeiro" element={<FinancialPage />} />
        <Route
          path="/financeiro/nova-movimentacao"
          element={<TransactionForm />}
        />
        <Route path="/relatorios" element={<Reports />} />
        <Route path="/configuracoes" element={<SettingsPage />} />
      </Routes>
    </AppLayout>
  );
}
