import { useContext } from 'react'
import { ThemeContext } from './ThemeContext.js'

export function useTheme() {
  const context = useContext(ThemeContext)
  if (!context) {
    return {
      theme: 'light',
      toggleTheme: () => {},
      setTheme: () => {},
    }
  }
  return context
}
