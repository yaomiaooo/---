import request from '../utils/request'

export function fetchAuditLogs(params) {
    return request.get('/api/admin/audit-logs', { params })
}

