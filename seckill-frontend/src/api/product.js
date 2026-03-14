import request from '../utils/request'

// 获取秒杀商品列表
export function getProductList() {
  return request({
    url: '/api/products/list',
    method: 'get'
  })
}