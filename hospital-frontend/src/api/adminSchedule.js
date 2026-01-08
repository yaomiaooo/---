// hospital-frontend/src/api/adminSchedule.js
import request from '../utils/request'

// 注意：request.js 已经把非 200 code 拦截并 reject；成功时返回 Result 对象

export function listHospitals() {
    return request.get('/api/admin/schedules/hospitals')
}

export function listDepartments(params) {
    // params: { hospitalId }
    return request.get('/api/admin/schedules/departments', { params })
}

export function listDoctors(params) {
    // params: { hospitalId, departmentId? }
    return request.get('/api/admin/schedules/doctors', { params })
}

// 兼容性函数：按院区获取科室列表
export function listDepartmentsByHospital(hospitalId) {
    return request.get('/api/admin/schedules/departments', { 
        params: { hospitalId } 
    })
}

// 兼容性函数：按院区和科室获取医生列表
export function listDoctorsByHospitalDepartment(params) {
    // params: { hospitalId, departmentId? }
    return request.get('/api/admin/schedules/doctors', { params })
}

export function pageSchedules(params) {
    // params: { hospitalId, departmentId?, doctorId?, workDateFrom?, workDateTo?, timeSlot?, page, size }
    return request.get('/api/admin/schedules', { params })
}

export function createSchedule(data) {
    // data: { hospitalId, doctorId, workDate, timeSlot, totalQuota }
    return request.post('/api/admin/schedules', data, {
        headers: {
            'Content-Type': 'application/json'
        }
    })
}

export function updateSchedule(scheduleId, data) {
    // data: { doctorId, workDate, timeSlot, totalQuota }
    return request.put(`/api/admin/schedules/${scheduleId}`, data, {
        headers: {
            'Content-Type': 'application/json'
        }
    })
}

export function deleteSchedule(scheduleId) {
    return request.delete(`/api/admin/schedules/${scheduleId}`)
}

export function closeSchedule(scheduleId, scheduleData) {
    // 关闭排班：通过将 totalQuota 设置为 bookedCount 来实现关闭（使剩余号源为 0）
    // scheduleData 应该包含：doctorId, workDate, timeSlot, capacity (totalQuota), bookedCount
    // 注意：前端使用 capacity 字段，后端使用 totalQuota 字段
    
    if (!scheduleData) {
        return Promise.reject(new Error('closeSchedule 需要传入完整的排班数据'))
    }
    
    // 获取已预约数量（前端可能使用不同的字段名）
    const bookedCount = scheduleData.bookedCount || scheduleData.booked_count || 0
    const capacity = scheduleData.capacity || scheduleData.totalQuota || scheduleData.total_quota || 0
    
    // 确保 bookedCount 不超过 capacity
    const finalQuota = Math.max(bookedCount, 0)
    
    const updateData = {
        doctorId: scheduleData.doctorId || scheduleData.doctor_id,
        workDate: scheduleData.workDate || scheduleData.work_date,
        timeSlot: scheduleData.timeSlot || scheduleData.time_slot,
        totalQuota: finalQuota  // 设置为已预约数，使剩余号源为 0
    }
    
    return updateSchedule(scheduleId, updateData)
}

