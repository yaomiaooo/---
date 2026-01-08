package com.example.hospital.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 排班响应DTO
 */
@Data
public class ScheduleResponse {
    private String scheduleId;      // 排班ID
    private String doctorId;        // 医生ID
    private String hospitalId;      // 院区ID
    private LocalDate workDate;     // 工作日期
    private String timeSlot;        // 时间段（如：08:00-10:00）
    private Integer totalQuota;     // 总号源数
    private Integer bookedCount;   // 已预约数
    private Integer availableCount; // 可预约数（totalQuota - bookedCount）
    private Boolean isAvailable;    // 是否可预约（availableCount > 0）
}

