import request from "../utils/request";

// 分页查询（keyword: 手机号关键词；role: user/doctor/admin）
export function pageUsers(params) {
    return request({
        url: "/api/admin/users",
        method: "get",
        params,
    });
}

// 详情
export function getUserDetail(userId) {
    return request({
        url: `/api/admin/users/${userId}`,
        method: "get",
    });
}

// 新增
export function createUser(data) {
    // data: { userPhone, role, userPassword? }
    return request({
        url: "/api/admin/users",
        method: "post",
        data,
    });
}

// 更新
export function updateUser(userId, data) {
    // data: { userPhone, role }
    return request({
        url: `/api/admin/users/${userId}`,
        method: "put",
        data,
    });
}

// 重置密码（后端默认重置为 123456）
export function resetPassword(userId) {
    return request({
        url: `/api/admin/users/${userId}/reset-password`,
        method: "patch",
    });
}

export function deleteUser(userId) {
    return request({
        url: `/api/admin/users/${userId}`,
        method: "delete",
    });
}

