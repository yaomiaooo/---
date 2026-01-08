// src/api/patient.js
import request from '../utils/request';

// 获取当前登录用户的所有就诊人
export function getMyPatients() {
  return request({
    url: '/api/patients',
    method: 'get'
  });
}

// 添加就诊人
export function addPatient(data) {
  return request({
    url: '/api/patients',
    method: 'post',
    data: data
  });
}

// 更新就诊人信息
export function updatePatient(patientId, data) {
  return request({
    url: `/api/patients/${patientId}`,
    method: 'put',
    data: data
  });
}

// 删除就诊人
export function deletePatient(patientId) {
  return request({
    url: `/api/patients/${patientId}`,
    method: 'delete'
  });
}

