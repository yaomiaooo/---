package com.example.hospital.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约详细信息DTO
 */
@Data
public class AppointmentDetailDTO {
    // 预约基本信息
    private String appointmentId;
    private String userId;
    private String patientId;
    private String scheduleId;
    private String hospitalId;
    private String status;
    private LocalDateTime createdAt;

    // 患者信息
    private String patientName;
    private String patientGender;
    private LocalDate patientBirthday;
    private String patientPhone;

    // 排班信息
    private String timeSlot;

    // 医生信息
    private String doctorName;
    private String title;
    private String doctorPhone;
    private String doctorEmail;
    private String doctorIntro;
}

