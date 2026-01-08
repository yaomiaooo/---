package com.example.hospital.dto;

import lombok.Data;

/**
 * 时间段排班信息
 */
@Data
public class TimeSlotSchedule {
    private String timeSlot;            // 时间段（如：08:00-10:00）
    private String period;              // 时段（am/pm）
    private Integer availableCount;      // 可预约数
    private Boolean isAvailable;         // 是否可预约
}

