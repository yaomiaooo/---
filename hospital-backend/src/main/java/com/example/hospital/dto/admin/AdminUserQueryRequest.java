package com.example.hospital.dto.admin;

import lombok.Data;

/**
 * 管理员-用户查询（分页 + 关键词 + 角色）
 */
@Data
public class AdminUserQueryRequest {
    private long page = 1;
    private long size = 10;

    /** 匹配手机号（user_phone） */
    private String keyword;

    /** user/doctor/admin */
    private String role;
}
