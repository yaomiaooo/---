package com.example.hospital.dto.admin;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 管理员 - 更新用户状态
 */
@Data
public class AdminUserStatusUpdateRequest {
    @NotNull(message = "status不能为空")
    private Integer status; // 1启用/0禁用
}
