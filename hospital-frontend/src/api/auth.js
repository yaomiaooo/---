// src/api/auth.js
import request from '../utils/request'; // 引入封装好的 axios 实例

// src/api/auth.js
import axios from 'axios';

// 创建 axios 实例
const service = axios.create({
  baseURL: 'http://localhost:8080/api', // 后端地址
  timeout: 5000 // 请求超时时间
});

/**
 * 登录接口
 * @param {object} data 包含登录所需信息的对象
 * @returns Promise
 */
export function loginApi(data) {
  /*
    data 的预期结构:
    {
      userPhone: '用户的手机号',
      userPassword: '用户的密码',
      role: '用户选择的角色'
    }
  */
  return request({
    url: '/api/auth/login',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'application/json'
    }
  });
}

/**
 * 注册接口
 * @param {object} data 包含注册所需信息的对象
 * @returns Promise
 */
export function registerApi(data) {
  /*
    data 的预期结构:
    {
      userPhone: '用户的手机号',
      userPassword: '用户的密码'
    }
  */
  return request({
    url: '/api/auth/register',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'application/json'
    }
  });
}