package com.example.hospital.dto;

import lombok.Data;

/**
 * 创建预约请求DTO
 */
@Data
public class CreateAppointmentRequest {
    private String patientId;    // 就诊人ID
    private String scheduleId;   // 排班ID
    private String hospitalId;   // 院区ID
}

