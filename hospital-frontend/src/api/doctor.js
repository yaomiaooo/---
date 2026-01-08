// src/api/doctor.js
import request from '../utils/request';

// 获取医生列表（支持按院区、科室、关键词搜索）
export function getDoctors(hospitalId, departmentId, keyword) {
  const params = {};
  if (hospitalId) params.hospitalId = hospitalId;
  if (departmentId) params.departmentId = departmentId;
  if (keyword) params.keyword = keyword;
  
  return request({
    url: '/api/doctors',
    method: 'get',
    params: params
  });
}

// 获取医生详情
export function getDoctorById(doctorId) {
  return request({
    url: `/api/doctors/${doctorId}`,
    method: 'get'
  });
}

// 获取当前登录医生的个人资料
export function getCurrentDoctorProfile() {
  return request({
    url: '/api/doctor/profile',
    method: 'get'
  });
}

