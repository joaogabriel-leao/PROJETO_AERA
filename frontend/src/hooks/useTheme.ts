import { useEffect, useState } from 'react'

export type Theme = 'light' | 'dark'

const getInitialTheme = (): Theme => {
  const savedTheme = window.localStorage.getItem('aera-theme')
  if (savedTheme === 'dark' || savedTheme === 'light') return savedTheme
  return 'light'
}

export function useTheme() {
  const [theme, setTheme] = useState<Theme>(getInitialTheme)

  useEffect(() => {
    document.documentElement.dataset.theme = theme
    window.localStorage.setItem('aera-theme', theme)
  }, [theme])

  return { theme, toggleTheme: () => setTheme(current => current === 'light' ? 'dark' : 'light') }
}
