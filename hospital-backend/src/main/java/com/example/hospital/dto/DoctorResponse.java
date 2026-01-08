package com.example.hospital.dto;

import lombok.Data;

/**
 * 返回给前端的医生信息
 */
@Data
public class DoctorResponse {
    private String doctorId;
    private String doctorName;
    private String title;
    private String doctorGender;
    private String doctorPhone;
    private String doctorEmail;
    private String doctorIntro;  // 医生介绍/擅长
    private String avatarUrl;
    private String hospitalId;
    private String departmentId;
    private String departmentName;
}

