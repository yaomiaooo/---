package com.example.hospital.dto;

import lombok.Data;

/**
 * 医生个人资料响应对象
 */
@Data
public class DoctorProfileResponse {
    private String doctorId;        // 医生编号
    private String doctorName;      // 医生姓名
    private String doctorGender;    // 医生性别
    private String doctorPhone;     // 医生电话
    private String doctorEmail;     // 医生邮箱
    private String title;           // 职称
    private String doctorIntro;     // 医生简介
    private String hospitalId;      // 所属医院ID
    private String hospitalName;    // 所属医院名称
    private String departmentId;    // 所属科室ID
    private String departmentName;  // 所属科室名称
    private String specialty;       // 擅长领域（暂时为空，后续可扩展）
}

