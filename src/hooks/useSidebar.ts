import { useState } from 'react'

export function useSidebar() {
  const [collapsed, setCollapsed] = useState(false)
  const [mobileOpen, setMobileOpen] = useState(false)
  return { collapsed, setCollapsed, mobileOpen, setMobileOpen }
}
