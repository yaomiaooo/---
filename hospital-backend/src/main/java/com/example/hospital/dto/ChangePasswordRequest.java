package com.example.hospital.dto;

import lombok.Data;

/**
 * 修改密码请求DTO
 */
@Data
public class ChangePasswordRequest {
    private String oldPassword;  // 旧密码
    private String newPassword;  // 新密码
}

