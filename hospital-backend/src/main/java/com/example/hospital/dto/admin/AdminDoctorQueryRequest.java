package com.example.hospital.dto.admin;

import lombok.Data;

@Data
public class AdminDoctorQueryRequest {
    private long page = 1;
    private long size = 10;

    private String keyword;       // 医生姓名/医生ID/用户ID/手机号等
    private String hospitalId;    // 建议尽量传，减少广播
    private String departmentId;  // 可选
}
