import request from '../utils/request';

// 获取当前登录用户的所有就诊记录
export function getMyVisits() {
  return request({
    url: '/api/visits',
    method: 'get'
  });
}

