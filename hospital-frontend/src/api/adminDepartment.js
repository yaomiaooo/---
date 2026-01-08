import request from "../utils/request";

// 分页查询科室
export function pageDepartments(params) {
    return request({
        url: "/api/admin/departments",
        method: "get",
        params,
    });
}

// 新增科室
export function createDepartment(data) {
    return request({
        url: "/api/admin/departments",
        method: "post",
        data,
    });
}

// 编辑科室
export function updateDepartment(departmentId, data) {
    return request({
        url: `/api/admin/departments/${departmentId}`,
        method: "put",
        data,
    });
}

// 删除科室
export function deleteDepartment(departmentId) {
    return request({
        url: `/api/admin/departments/${departmentId}`,
        method: "delete",
    });
}

// 获取医院列表（用于下拉框筛选/选择所属院区）
// 使用管理员接口：/api/admin/departments/hospitals
export function listHospitals() {
    return request({
        url: "/api/admin/departments/hospitals",
        method: "get",
    });
}

