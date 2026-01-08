// src/api/appointment.js
import request from '../utils/request';

// 获取当前用户的所有预约记录
export function getMyAppointments() {
  return request({
    url: '/api/appointments',
    method: 'get'
  });
}

// 创建预约
export function createAppointment(data) {
  return request({
    url: '/api/appointments',
    method: 'post',
    data: data
  });
}

// 取消预约
export function cancelAppointment(appointmentId) {
  return request({
    url: `/api/appointments/${appointmentId}/cancel`,
    method: 'put'
  });
}

