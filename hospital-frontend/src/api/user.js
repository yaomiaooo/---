// src/api/user.js
import request from '../utils/request';

// 获取当前登录用户的个人资料
export function getUserProfile() {
  return request({
    url: '/api/user/profile',
    method: 'get'
  });
}

// 更新当前登录用户的个人资料
export function updateUserProfile(data) {
  return request({
    url: '/api/user/profile',
    method: 'put',
    data: data
  });
}

// 修改用户密码
export function changePassword(data) {
  return request({
    url: '/api/user/password',
    method: 'put',
    data: data
  });
}

