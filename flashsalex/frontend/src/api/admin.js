import request from '../utils/request'

// ===== 商品管理 =====
export function getGoodsList(params) {
  return request.get('/api/admin/goods', { params })
}

export function getGoodsDetail(id) {
  return request.get(`/api/admin/goods/${id}`)
}

export function createGoods(data) {
  return request.post('/api/admin/goods', data)
}

export function updateGoods(id, data) {
  return request.put(`/api/admin/goods/${id}`, data)
}

export function deleteGoods(id) {
  return request.delete(`/api/admin/goods/${id}`)
}

// ===== 活动管理 =====
export function getActivityList(params) {
  return request.get('/api/admin/seckill/activities', { params })
}

export function createActivity(data) {
  return request.post('/api/admin/seckill/activities', data)
}

export function onlineActivity(activityId) {
  return request.post(`/api/admin/seckill/activities/${activityId}/online`)
}

export function offlineActivity(activityId) {
  return request.post(`/api/admin/seckill/activities/${activityId}/offline`)
}

export function preheatActivity(activityId) {
  return request.post(`/api/admin/seckill/activities/${activityId}/preheat`)
}

export function verifyCache(activityId) {
  return request.get(`/api/admin/seckill/activities/${activityId}/cache-verify`)
}

// ===== 黑名单 =====
export function addBlacklist(data) {
  return request.post('/api/admin/risk/blacklist', data)
}

export function removeBlacklist(targetType, targetValue) {
  return request.delete('/api/admin/risk/blacklist', { params: { targetType, targetValue } })
}
