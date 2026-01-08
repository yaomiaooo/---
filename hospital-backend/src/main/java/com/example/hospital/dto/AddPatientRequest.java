package com.example.hospital.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 添加就诊人请求的数据传输对象
 */
@Data
public class AddPatientRequest {
    private String name;
    private String idCard;
    private String phone;
    private LocalDate dob; // Date of Birth
    private String gender;
    private String relation; // 与当前用户的关系, e.g., '本人', '父亲'
}

