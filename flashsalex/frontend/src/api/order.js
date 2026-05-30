import request from '../utils/request'

export function getOrderList(params) {
  return request.get('/api/orders', { params })
}

export function getOrderDetail(orderNo) {
  return request.get(`/api/orders/${orderNo}`)
}

export function payOrder(orderNo) {
  return request.post(`/api/orders/${orderNo}/pay`)
}

export function cancelOrder(orderNo) {
  return request.post(`/api/orders/${orderNo}/cancel`)
}
