package com.example.hospital.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 预约记录响应DTO
 */
@Data
public class AppointmentResponse {
    private String appointmentId;    // 预约ID
    private String userId;           // 用户ID
    private String patientId;          // 就诊人ID
    private String patientName;       // 就诊人姓名
    private String doctorId;          // 医生ID
    private String doctorName;         // 医生姓名
    private String doctorTitle;        // 医生职称
    private String hospitalId;        // 院区ID
    private String hospitalName;       // 院区名称
    private String scheduleId;         // 排班ID
    private String visitDate;          // 就诊日期（从 schedule 表获取）
    private String timeSlot;           // 时间段（从 schedule 表获取）
    private String status;             // 预约状态：'未就诊'/'已就诊'/'已取消'/'已过期'（已转换为中文显示）
    private LocalDateTime createdAt;  // 创建时间
    private String diagnosis;          // 诊断结果（暂时为空，后续可以扩展）
    private String departmentName;     // 科室名称（可选）
    private Double price;              // 挂号费（根据医生职称计算）
}

