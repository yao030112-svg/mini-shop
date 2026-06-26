import { post, get } from './request'

export function wxLogin(code, profile = {}) {
  return post('/api/auth/wx-login', {
    code,
    nickname: profile.nickName || '',
    avatarUrl: profile.avatarUrl || ''
  })
}

export function adminLogin(username, password) {
  return post('/api/auth/admin-login', { username, password })
}

export function getUserInfo() {
  return get('/api/user/info')
}
