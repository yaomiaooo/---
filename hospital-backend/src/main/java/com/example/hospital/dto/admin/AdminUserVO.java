package com.example.hospital.dto.admin;

import lombok.Data;

import java.time.LocalDateTime;

/** 返回给前端的用户信息（不返回密码） */
@Data
public class AdminUserVO {
    private String userId;
    private String userPhone;
    private String role;
    private LocalDateTime createdAt;
}
