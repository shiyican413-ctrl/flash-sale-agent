import request from '../utils/request'

export function getCaptcha(activityId) {
  return request.get(`/api/seckill/activities/${activityId}/captcha`)
}

export function getSeckillPath(activityId, data) {
  return request.post(`/api/seckill/activities/${activityId}/path`, data)
}

export function executeSeckill(activityId, path, data) {
  return request.post(`/api/seckill/activities/${activityId}/purchase/${path}`, data)
}

export function getSeckillResult(activityId) {
  return request.get(`/api/seckill/activities/${activityId}/result`)
}
