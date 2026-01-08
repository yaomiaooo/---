import request from "../utils/request";

// 分页查询预约订单
export function pageAppointments(params) {
    return request({
        url: "/api/admin/appointments",
        method: "get",
        params,
    });
}

// 查询订单详情
export function getAppointmentDetail(appointmentId) {
    return request({
        url: `/api/admin/appointments/${appointmentId}`,
        method: "get",
    });
}

// 更新订单状态（单条）
export function updateAppointmentStatus(appointmentId, status) {
    return request({
        url: `/api/admin/appointments/${appointmentId}/status`,
        method: "patch",
        data: { status },
    });
}

// 批量更新订单状态
export function batchUpdateAppointmentStatus(appointmentIds, status) {
    return request({
        url: "/api/admin/appointments/batch-status",
        method: "patch",
        data: { appointmentIds, status },
    });
}

