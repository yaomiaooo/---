import request from '../utils/request';

// 创建评价
export function createReview(data) {
  return request({
    url: '/api/reviews',
    method: 'post',
    data: data
  });
}

// 根据预约ID获取评价
export function getReviewByAppointmentId(appointmentId) {
  return request({
    url: `/api/reviews/appointment/${appointmentId}`,
    method: 'get'
  });
}

// 获取当前用户的所有评价
export function getMyReviews() {
  return request({
    url: '/api/reviews',
    method: 'get'
  });
}

// 根据医生ID获取该医生的所有评价（公开接口，不需要登录）
export function getReviewsByDoctorId(doctorId) {
  return request({
    url: `/api/reviews/doctor/${doctorId}`,
    method: 'get'
  });
}

