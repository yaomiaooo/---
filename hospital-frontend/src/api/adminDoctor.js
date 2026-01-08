import request from '../utils/request'

export function fetchDoctors(params) {
    return request.get('/api/admin/doctors', { params })
}

export function createDoctor(data) {
    return request.post('/api/admin/doctors', data, {
        headers: {
            'Content-Type': 'application/json'
        }
    })
}

/**
 * ✅ 更新医生：hospitalId 必须走 query 参数用于分片路由
 * PUT /api/admin/doctors/{doctorId}?hospitalId=xxx
 * body 不允许携带 hospitalId/departmentId/userId/doctorId
 */
export function updateDoctor(doctorId, hospitalId, data) {
    return request.put(`/api/admin/doctors/${encodeURIComponent(doctorId)}`, data, {
        params: { hospitalId }
    })
}

/**
 * ✅ 删除医生：建议携带 hospitalId 精准路由
 * DELETE /api/admin/doctors/{doctorId}?hospitalId=xxx
 */
export function deleteDoctor(doctorId, hospitalId) {
    return request.delete(`/api/admin/doctors/${encodeURIComponent(doctorId)}`, {
        params: { hospitalId }
    })
}

/**
 * 复用科室分页接口：按院区ID查询科室列表（用于下拉）
 * GET /api/admin/departments?hospitalId=xxx&page=1&size=200
 */
export function fetchDepartmentsByHospital(hospitalId, page = 1, size = 200) {
    return request.get('/api/admin/departments', {
        params: { hospitalId, page, size }
    })
}

