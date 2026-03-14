// src/api/user.js
import request from '../utils/request'

// 1. 用户注册接口
export function register(data) {
  return request({
    url: '/api/users/register',
    method: 'post',
    data: data 
  })
}

// 2. 用户登录接口 (修复了传参方式和字段名错位)
export function login(username, password) {
  return request({
    url: '/api/users/login',
    method: 'post',
    // 必须用 data 传 JSON，并将 username 映射给后端的 account
    data: { 
      account: username, 
      password: password 
    } 
  })
}