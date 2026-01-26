// Token management utilities
const TokenKey = 'Admin-Token'

export const getToken = (): string | null => {
  return localStorage.getItem(TokenKey)
}

export const setToken = (token: string): void => {
  localStorage.setItem(TokenKey, token)
}

export const removeToken = (): void => {
  localStorage.removeItem(TokenKey)
}
