package com.example.hospital.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 返回给前端的就诊人信息
 */
@Data
public class PatientResponse {
    private String patientId;
    private String name;
    private String idCard;
    private String phone;
    private LocalDate dob;
    private String gender;
    private String relation;
}

