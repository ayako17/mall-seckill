import request from '../utils/request'

// 1. 用户注册接口
export function register(data) {
  return request({
    url: '/api/users/register',
    method: 'post',
    data: data // 这是传 JSON body (对应后端 @RequestBody)
  })
}

// 2. 用户登录接口
export function login(username, password) {
  return request({
    url: '/api/users/login',
    method: 'post',
    params: { username, password } // 这是传 URL 参数 (对应后端 @RequestParam)
  })
}