// src/api/product.js
import request from '../utils/request'

// 1. 获取普通商品列表 (基础商品库)
export function getNormalProductList() {
  return request({
    url: '/api/products/normal/list',
    method: 'get'
  })
}

// 2. 获取秒杀商品列表 (今日必抢/限时特惠)
export function getSeckillProductList() {
  return request({
    url: '/api/products/seckill/list',
    method: 'get'
  })
}