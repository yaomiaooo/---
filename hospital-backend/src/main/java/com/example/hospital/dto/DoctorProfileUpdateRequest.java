package com.example.hospital.dto;

import lombok.Data;

/**
 * 医生个人资料更新请求的数据传输对象
 */
@Data
public class DoctorProfileUpdateRequest {
    private String doctorPhone;   // 医生电话
    private String doctorEmail;   // 医生邮箱
    private String doctorIntro;   // 医生简介
}

