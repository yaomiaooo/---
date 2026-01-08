package com.example.hospital.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 病历档案DTO
 */
@Data
public class MedicalRecordDTO {
    private String recordId;           // 记录ID
    private LocalDate visitDate;       // 就诊日期
    private String timeSlot;           // 时间段
    private String patientName;        // 患者姓名
    private String patientPhone;       // 患者电话
    private String orderNo;            // 预约号
    private String recordNo;           // 就诊号
    private String departmentName;     // 科室名称
    private String summary;            // 诊断摘要
    private String advice;             // 医嘱
    private String status;             // 状态 (FINISHED, CANCELLED, NOSHOW等)
}

