package com.example.hospital.dto;

import lombok.Data;

/**
 * 注册请求DTO
 */
@Data
public class RegisterRequest {
    private String userPhone;     // 用户手机号
    private String userPassword;   // 用户密码
}

