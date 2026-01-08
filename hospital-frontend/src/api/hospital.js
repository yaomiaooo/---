// src/api/hospital.js
import request from '../utils/request'; // 引入刚才写的工具

// 定义一个获取科室列表的方法（支持按 hospital_id 查询和搜索）
export function getDepartmentList(hospitalId, keyword) {
  const params = {};
  if (hospitalId) params.hospitalId = hospitalId;
  if (keyword) params.keyword = keyword;
  return request({
    url: '/api/hospital/departments', // 对应后端的 @GetMapping("/departments")
    method: 'get',
    params: params
  });
}

// 定义一个提交预约的方法 (比如以后要用)
export function submitAppointment(data) {
  return request({
    url: '/api/hospital/appointment',
    method: 'post',
    data: data
  });
}

// 获取医院介绍信息（用于院区详情页）
export function getHospitalIntro(hospitalId) {
  return request({
    url: `/api/hospitals/intro/${hospitalId}`,
    method: 'get'
  });
}

// 获取科室详情
export function getDepartmentDetail(departmentId) {
  return request({
    url: `/api/hospital/departments/${departmentId}`,
    method: 'get'
  });
}

// 获取所有医院/院区列表
export function getHospitalList() {
  return request({
    url: '/api/hospitals',
    method: 'get'
  });
}

// 获取所有医院/院区列表（别名，与后端接口名称一致）
export function getAllHospitals() {
  return request({
    url: '/api/hospitals',
    method: 'get'
  });
}