import { useEffect, useState } from 'react'
import { ThemeContext } from './ThemeContext.js'

export default function ThemeProvider({ children }) {
  const [theme, setTheme] = useState(() => {
    try {
      const saved = localStorage.getItem('campusconnect_theme')
      if (saved === 'dark' || saved === 'light') {
        return saved
      }
      return 'light'
    } catch {
      return 'light'
    }
  })

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme)
    try {
      localStorage.setItem('campusconnect_theme', theme)
    } catch {
      // Ignore storage errors in restricted environments
    }
  }, [theme])

  const toggleTheme = () => {
    setTheme((prev) => (prev === 'light' ? 'dark' : 'light'))
  }

  return (
    <ThemeContext.Provider value={{ theme, toggleTheme, setTheme }}>
      {children}
    </ThemeContext.Provider>
  )
}
