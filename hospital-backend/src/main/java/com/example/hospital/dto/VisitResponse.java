package com.example.hospital.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 就诊记录响应DTO
 */
@Data
public class VisitResponse {
    private String visitId;           // 就诊记录ID
    private String appointmentId;     // 预约ID
    private String patientId;         // 就诊人ID
    private String patientName;       // 就诊人姓名
    private String doctorId;          // 医生ID
    private String doctorName;        // 医生姓名
    private String doctorTitle;       // 医生职称
    private String hospitalId;        // 院区ID
    private String hospitalName;      // 院区名称
    private String departmentName;    // 科室名称
    private LocalDateTime visitTime;  // 就诊时间
    private String diagnosis;         // 诊断结果
    private String timeSlot;          // 时间段
    private ReviewResponse review;    // 评价信息（如果有）
}

