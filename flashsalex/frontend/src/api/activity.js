import request from '../utils/request'

export function getActivityDetail(activityId) {
  return request.get(`/api/seckill/activities/${activityId}`)
}

export function getActivityList(params) {
  return request.get('/api/seckill/activities', { params })
}
